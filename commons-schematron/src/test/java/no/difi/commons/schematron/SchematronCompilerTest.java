package no.difi.commons.schematron;

import no.difi.commons.schematron.jaxb.svrl.SchematronOutput;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;

public class SchematronCompilerTest {

    private SchematronCompiler schematronCompiler;

    @BeforeClass
    public void beforeClass() throws Exception {
        schematronCompiler = new SchematronCompiler();
    }

    @Test
    public void createXslt() throws Exception {
        File file = new File(getClass().getResource("/PEPPOL-SBDH.sch").toURI());
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        schematronCompiler.compile(file, byteArrayOutputStream);

        SchematronValidator schematronValidator = new SchematronValidator(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));

        SchematronOutput schematronOutput = schematronValidator.validate(getClass().getResourceAsStream("/peppol-bis-invoice-sbdh.xml"));
        Assert.assertEquals(schematronOutput.getActivePatternAndFiredRuleAndFailedAssert().size(), 11);
    }

    @Test
    public void createValidator() throws Exception {
        File file = new File(getClass().getResource("/PEPPOL-SBDH.sch").toURI());
        SchematronValidator schematronValidator = schematronCompiler.createValidator(file);

        SchematronOutput schematronOutput = schematronValidator.validate(getClass().getResourceAsStream("/peppol-bis-invoice-sbdh.xml"));
        Assert.assertEquals(schematronOutput.getActivePatternAndFiredRuleAndFailedAssert().size(), 11);
    }
}
