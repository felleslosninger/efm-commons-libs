package no.difi.commons.schematron;

import net.sf.saxon.s9api.*;

import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SchematronCompiler implements Closeable {

    public static final String FLAVOUR_ORIGINAL = "original";

    public static final String FLAVOUR_PEPPOL = "peppol";

    private List<XsltExecutable> steps;

    private Processor processor;

    public SchematronCompiler() throws SchematronException {
        this(SaxonUtils.PROCESSOR, FLAVOUR_ORIGINAL);
    }

    public SchematronCompiler(Processor processor) throws SchematronException {
        this(processor, FLAVOUR_ORIGINAL);
    }

    public SchematronCompiler(String flavour) throws SchematronException {
        this(SaxonUtils.PROCESSOR, flavour);
    }

    public SchematronCompiler(Processor processor, String flavour) throws SchematronException {
        this.processor = processor;

        XsltCompiler xsltCompiler = processor.newXsltCompiler();
        xsltCompiler.setURIResolver(new ClasspathURIResolver("/iso-schematron-xslt2"));

        this.steps = Arrays.asList(
                load("/iso-schematron-xslt2/iso_dsdl_include.xsl", xsltCompiler),
                load("/iso-schematron-xslt2/iso_abstract_expand.xsl", xsltCompiler),
                load(String.format("/iso-schematron-xslt2/iso_svrl_for_xslt2-%s.xsl", flavour), xsltCompiler)
        );
    }

    private XsltExecutable load(String file, XsltCompiler xsltCompiler) throws SchematronException {
        try (InputStream inputStream = getClass().getResourceAsStream(file)) {
            return xsltCompiler.compile(new StreamSource(inputStream));
        } catch (IOException | SaxonApiException e) {
            throw new SchematronException(String.format("Unable to load file '%s'.", file), e);
        }
    }

    @SuppressWarnings("unused")
    public void compile(File inputFile, File outputFile) throws SchematronException, IOException {
        compile(inputFile.toPath(), outputFile.toPath());
    }

    public void compile(Path inputFile, Path outputFile) throws SchematronException, IOException {
        try (OutputStream outputStream = Files.newOutputStream(outputFile)) {
            compile(inputFile, outputStream);
        }
    }

    public void compile(Path inputFile, OutputStream outputStream) throws SchematronException {
        compile(inputFile.toFile(), outputStream);
    }

    public void compile(File inputFile, OutputStream outputStream) throws SchematronException {
        try (BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream)) {
            XdmDestination destination = new XdmDestination();

            compile(inputFile, destination);

            bufferedOutputStream.write(SaxonUtils.xdmToBytes(destination));

        } catch (IOException e) {
            throw new SchematronException(String.format("Unable to compile '%s'.", inputFile), e);
        }
    }

    public SchematronValidator createValidator(File inputFile) throws SchematronException {
        XdmDestination destination = new XdmDestination();

        compile(inputFile, destination);

        return new SchematronValidator(SaxonUtils.xdmToInputStream(destination), processor);
    }

    @SuppressWarnings("all")
    public void compile(File inputFile, Destination destination) throws SchematronException {
        XsltTransformer firstTransformer = null;
        XsltTransformer lastTransformer = null;
        List<XsltTransformer> xsltTransformers = new ArrayList<>();

        for (XsltExecutable xsltExecutable : steps) {
            XsltTransformer xsltTransformer = xsltExecutable.load();

            if (lastTransformer != null)
                lastTransformer.setDestination(xsltTransformer);
            else
                firstTransformer = xsltTransformer;

            lastTransformer = xsltTransformer;
            xsltTransformers.add(xsltTransformer);
        }

        try {
            lastTransformer.setDestination(destination);

            firstTransformer.setSource(new StreamSource(inputFile));
            firstTransformer.transform();

            for (XsltTransformer xsltTransformer : xsltTransformers)
                xsltTransformer.close();
        } catch (SaxonApiException e) {
            throw new SchematronException(String.format("Unable to compile '%s'.", inputFile), e);
        }
    }

    @Override
    public void close() throws IOException {
        steps.clear();
        steps = null;

        processor = null;
    }
}
