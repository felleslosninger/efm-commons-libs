package no.difi.commons.schematron;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

class ClasspathURIResolver implements URIResolver {

    private String path;

    public ClasspathURIResolver(String path) {
        this.path = path;
    }

    @Override
    public Source resolve(String href, String base) throws TransformerException {
        return !"".equals(base) ? null : new StreamSource(getClass().getResourceAsStream(String.format("%s/%s", path, href)));
    }
}
