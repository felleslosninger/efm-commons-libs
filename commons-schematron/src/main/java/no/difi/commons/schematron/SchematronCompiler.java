package no.difi.commons.schematron;

import net.sf.saxon.s9api.*;

import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SchematronCompiler implements Closeable {

    private static URIResolver uriResolver = new ClasspathURIResolver("/iso-schematron-xslt2");

    private List<XsltExecutable> steps;

    private Processor processor;

    public SchematronCompiler() throws SchematronException {
        this(SaxonUtils.PROCESSOR);
    }

    public SchematronCompiler(Processor processor) throws SchematronException {
        this.processor = processor;
        this.steps = Arrays.asList(
                load("/iso-schematron-xslt2/iso_dsdl_include.xsl"),
                load("/iso-schematron-xslt2/iso_abstract_expand.xsl"),
                load("/iso-schematron-xslt2/iso_svrl_for_xslt2.xsl")
        );
    }

    private XsltExecutable load(String file) throws SchematronException {
        try (InputStream inputStream = getClass().getResourceAsStream(file)) {
            XsltCompiler xsltCompiler = processor.newXsltCompiler();
            xsltCompiler.setURIResolver(uriResolver);
            return xsltCompiler.compile(new StreamSource(inputStream));
        } catch (IOException | SaxonApiException e) {
            throw new SchematronException(String.format("Unable to load file '%s'.", file), e);
        }
    }

    public void transform(File inputFile, File outputFile) throws SchematronException, IOException {
        transform(inputFile.toPath(), outputFile.toPath());
    }

    public void transform(Path inputFile, Path outputFile) throws SchematronException, IOException {
        try (OutputStream outputStream = Files.newOutputStream(outputFile)) {
            transform(inputFile, outputStream);
        }
    }

    public void transform(Path inputFile, OutputStream outputStream) throws SchematronException {
        transform(inputFile.toFile(), outputStream);
    }

    public void transform(File inputFile, OutputStream outputStream) throws SchematronException {
        try (BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream)) {
            XdmDestination destination = new XdmDestination();

            transform(inputFile, destination);

            bufferedOutputStream.write(SaxonUtils.xdmToBytes(destination));

        } catch (IOException e) {
            throw new SchematronException(String.format("Unable to transform '%s'.", inputFile), e);
        }
    }

    public SchematronValidator createValidator(File inputFile) throws SchematronException {
        XdmDestination destination = new XdmDestination();

        transform(inputFile, destination);

        return new SchematronValidator(SaxonUtils.xdmToInputStream(destination), processor);
    }

    @SuppressWarnings("all")
    public void transform(File inputFile, Destination destination) throws SchematronException {
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
            throw new SchematronException(String.format("Unable to transform '%s'.", inputFile), e);
        }
    }

    @Override
    public void close() throws IOException {
        steps.clear();
        steps = null;
    }
}
