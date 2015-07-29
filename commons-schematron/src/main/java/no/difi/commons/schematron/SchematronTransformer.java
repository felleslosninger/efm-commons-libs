package no.difi.commons.schematron;

import net.sf.saxon.TransformerFactoryImpl;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;

public class SchematronTransformer {

    private static TransformerFactory transformerFactory = new TransformerFactoryImpl();

    private Transformer step1, step2, step3;

    public SchematronTransformer() throws TransformerConfigurationException {
        step1 = transformerFactory.newTransformer(new StreamSource(getClass().getResource("/iso-schematron-xslt2/iso_dsdl_include.xsl").getFile()));
        step2 = transformerFactory.newTransformer(new StreamSource(getClass().getResource("/iso-schematron-xslt2/iso_abstract_expand.xsl").getFile()));
        step3 = transformerFactory.newTransformer(new StreamSource(getClass().getResource("/iso-schematron-xslt2/iso_svrl_for_xslt2.xsl").getFile()));
    }

    public synchronized void transform(File inputFile, File outputFile) throws TransformerException {
        ByteArrayInputStream byteArrayInputStream;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        step1.transform(new StreamSource(inputFile), new StreamResult(byteArrayOutputStream));

        byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        byteArrayOutputStream = new ByteArrayOutputStream();

        step2.transform(new StreamSource(byteArrayInputStream), new StreamResult(byteArrayOutputStream));

        byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());

        step3.transform(new StreamSource(byteArrayInputStream), new StreamResult(outputFile));
    }

}
