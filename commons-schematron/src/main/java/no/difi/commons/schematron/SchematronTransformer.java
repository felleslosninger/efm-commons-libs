package no.difi.commons.schematron;

import net.sf.saxon.TransformerFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class SchematronTransformer {

    private static Logger logger = LoggerFactory.getLogger(SchematronTransformer.class);

    private static TransformerFactory transformerFactory;

    static {
        try {
            // Try to load Saxon implementation before defaulting to default implementation.
            transformerFactory = (TransformerFactoryImpl) Class.forName("net.sf.saxon.TransformerFactoryImpl").getConstructor().newInstance();
            logger.info("Using Saxon transformer.");
        } catch (Exception e) {
            transformerFactory = TransformerFactory.newInstance();
            logger.info("Using default transformer.");
        }
    }

    private Transformer step1, step2, step3;

    public SchematronTransformer() throws TransformerConfigurationException {
        step1 = transformerFactory.newTransformer(new StreamSource(getClass().getResource("/iso-schematron-xslt2/iso_dsdl_include.xsl").getFile()));
        step2 = transformerFactory.newTransformer(new StreamSource(getClass().getResource("/iso-schematron-xslt2/iso_abstract_expand.xsl").getFile()));
        step3 = transformerFactory.newTransformer(new StreamSource(getClass().getResource("/iso-schematron-xslt2/iso_svrl_for_xslt2.xsl").getFile()));
    }

    public void transform(File inputFile, File outputFile) throws TransformerException, IOException {
        transform(inputFile.toPath(), outputFile.toPath());
    }

    public void transform(Path inputFile, Path outputFile) throws TransformerException, IOException {
        OutputStream outputStream = Files.newOutputStream(outputFile);
        transform(inputFile, outputStream);
        outputStream.close();
    }

    public void transform(Path inputFile, OutputStream outputStream) throws TransformerException {
        transform(inputFile.toFile(), outputStream);
    }

    public synchronized void transform(File inputFile, OutputStream outputStream) throws TransformerException {
        ByteArrayInputStream byteArrayInputStream;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        step1.transform(new StreamSource(inputFile), new StreamResult(byteArrayOutputStream));

        byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        byteArrayOutputStream = new ByteArrayOutputStream();

        step2.transform(new StreamSource(byteArrayInputStream), new StreamResult(byteArrayOutputStream));

        byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());

        step3.transform(new StreamSource(byteArrayInputStream), new StreamResult(outputStream));
    }
}
