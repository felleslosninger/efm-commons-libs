package no.difi.commons.schematron;

import net.sf.saxon.TransformerFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.transform.*;
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

        transformerFactory.setURIResolver(new URIResolver() {
            @Override
            public Source resolve(String href, String base) throws TransformerException {
                return !"".equals(base) ? null : new StreamSource(getClass().getResourceAsStream(String.format("/iso-schematron-xslt2/%s", href)));
            }
        });
    }

    private Transformer step1, step2, step3;

    public SchematronTransformer() throws TransformerConfigurationException {
        step1 = transformerFactory.newTransformer(new StreamSource(getClass().getResourceAsStream("/iso-schematron-xslt2/iso_dsdl_include.xsl")));
        step2 = transformerFactory.newTransformer(new StreamSource(getClass().getResourceAsStream("/iso-schematron-xslt2/iso_abstract_expand.xsl")));
        step3 = transformerFactory.newTransformer(new StreamSource(getClass().getResourceAsStream("/iso-schematron-xslt2/iso_svrl_for_xslt2.xsl")));
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
