package no.difi.commons.schematron;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.s9api.XdmNode;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

class SaxonUtils {

    public final static Processor PROCESSOR = new Processor(false);

    public static InputStream xdmToInputStream(XdmDestination xdmDestination) {
        return xdmToInputStream(xdmDestination.getXdmNode());
    }

    public static InputStream xdmToInputStream(XdmNode xdmNode) {
        return new ByteArrayInputStream(xdmToBytes(xdmNode));
    }

    public static byte[] xdmToBytes(XdmDestination xdmDestination) {
        return xdmToBytes(xdmDestination.getXdmNode());
    }

    public static byte[] xdmToBytes(XdmNode xdmNode) {
        return xdmNode.toString().getBytes(StandardCharsets.UTF_8);
    }
}
