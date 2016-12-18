package no.difi.commons.schematron;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;

public class SchematronCompilerTest {

    @Test
    public void simple() throws Exception {
        SchematronCompiler schematronCompiler = new SchematronCompiler();

        File file = new File(getClass().getResource("/PEPPOL-SBDH.sch").toURI());
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        schematronCompiler.transform(file, byteArrayOutputStream);

        Assert.assertEquals(byteArrayOutputStream.size(), 23764);
    }
}
