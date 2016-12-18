package no.difi.commons.schematron;

import net.sf.saxon.s9api.*;

import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class SchematronCompiler implements Closeable {

    private URIResolver uriResolver = new ClasspathURIResolver("/iso-schematron-xslt2");

    private Processor processor = new Processor(false);

    private List<XsltExecutable> steps;

    public SchematronCompiler() throws SchematronException {
        steps = Arrays.asList(
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

    @SuppressWarnings("all")
    public synchronized void transform(File inputFile, OutputStream outputStream) throws SchematronException {
        XsltTransformer firstTransformer = null;
        XsltTransformer lastTransformer = null;

        for (XsltExecutable xsltExecutable : steps) {
            XsltTransformer xsltTransformer = xsltExecutable.load();

            if (lastTransformer != null)
                lastTransformer.setDestination(xsltTransformer);
            else
                firstTransformer = xsltTransformer;

            lastTransformer = xsltTransformer;
        }

        try (BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream)) {
            XdmDestination destination = new XdmDestination();
            lastTransformer.setDestination(destination);

            firstTransformer.setSource(new StreamSource(inputFile));
            firstTransformer.transform();

            bufferedOutputStream.write(destination.getXdmNode().toString().getBytes());
        } catch (SaxonApiException | IOException e) {
            throw new SchematronException(String.format("Unable to transform '%s'.", inputFile), e);
        }
    }

    @Override
    public void close() throws IOException {
        steps.clear();
        steps = null;

        processor = null;
        uriResolver = null;
    }
}
