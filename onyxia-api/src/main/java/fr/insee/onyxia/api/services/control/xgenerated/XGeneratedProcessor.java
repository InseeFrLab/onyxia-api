package fr.insee.onyxia.api.services.control.xgenerated;

import fr.insee.onyxia.model.catalog.Pkg;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class XGeneratedProcessor {

    @Autowired private XGeneratedReader xGeneratedReader;

    @Autowired private XGeneratedIterator xGeneratedIterator;

    @Autowired private XGeneratedInjector xGeneratedInjector;

    public XGeneratedContext readContext(Pkg pkg) {
        XGeneratedContext xGeneratedContext = new XGeneratedContext();
        pkg.getConfig().getProperties().getProperties().entrySet().stream()
                .forEach(
                        (entry) -> {
                            xGeneratedReader.readXGenerated(
                                    Arrays.asList(entry.getKey()),
                                    entry.getValue(),
                                    xGeneratedContext);
                        });
        return xGeneratedContext;
    }

    public Map<String, String> process(
            XGeneratedContext xGeneratedContext, XGeneratedProvider provider) {
        Map<String, String> xGeneratedValues = new HashMap<>();
        xGeneratedIterator.iterateOverContext(xGeneratedContext, provider, xGeneratedValues);
        return xGeneratedValues;
    }

    public void injectIntoContext(
            Map<String, Object> target, Map<String, String> xGeneratedValues) {
        xGeneratedInjector.injectIntoContext(target, xGeneratedValues);
    }

    public XGeneratedReader getxGeneratedReader() {
        return xGeneratedReader;
    }

    public void setxGeneratedReader(XGeneratedReader xGeneratedReader) {
        this.xGeneratedReader = xGeneratedReader;
    }
}
