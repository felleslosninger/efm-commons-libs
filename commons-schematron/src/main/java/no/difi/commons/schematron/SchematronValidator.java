package no.difi.commons.schematron;

import net.sf.saxon.s9api.*;
import no.difi.commons.schematron.jaxb.svrl.SchematronOutput;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;

public class SchematronValidator {

    private static JAXBContext jaxbContext;

    private XsltExecutable xsltExecutable;

    static {
        try {
            jaxbContext = JAXBContext.newInstance(SchematronOutput.class);
        } catch (JAXBException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    public SchematronValidator(InputStream inputStream) throws SchematronException {
        this(inputStream, SaxonUtils.PROCESSOR);
    }

    public SchematronValidator(InputStream inputStream, Processor processor) throws SchematronException {
        try {
            XsltCompiler xsltCompiler = processor.newXsltCompiler();
            xsltExecutable = xsltCompiler.compile(new StreamSource(inputStream));
        } catch (SaxonApiException e) {
            throw new SchematronException("Unable to load Schematron XSLT.", e);
        }
    }

    public SchematronOutput validate(InputStream inputStream) throws SchematronException {
        try {
            XdmDestination destination = new XdmDestination();
            validate(inputStream, destination);

            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            return unmarshaller.unmarshal(new StreamSource(SaxonUtils.xdmToInputStream(destination)), SchematronOutput.class).getValue();
        } catch (JAXBException e) {
            throw new SchematronException("Unable to parse validation result.", e);
        }
    }

    public void validate(InputStream inputStream, Destination destination) throws SchematronException {
        try {
            XsltTransformer xsltTransformer = xsltExecutable.load();
            xsltTransformer.setSource(new StreamSource(inputStream));
            xsltTransformer.setDestination(destination);
            xsltTransformer.transform();
            xsltTransformer.close();
        } catch (SaxonApiException e) {
            throw new SchematronException("Unable to validate document.", e);
        }
    }
}
