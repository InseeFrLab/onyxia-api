package fr.insee.onyxia.api.services.control.xgenerated;

import fr.insee.onyxia.model.catalog.Pkg;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class XGeneratedProcessor {

    private final XGeneratedReader xGeneratedReader;

    private final XGeneratedIterator xGeneratedIterator;

    private final XGeneratedInjector xGeneratedInjector;

    @Autowired
    public XGeneratedProcessor(
            XGeneratedReader xGeneratedReader,
            XGeneratedIterator xGeneratedIterator,
            XGeneratedInjector xGeneratedInjector) {
        this.xGeneratedReader = xGeneratedReader;
        this.xGeneratedIterator = xGeneratedIterator;
        this.xGeneratedInjector = xGeneratedInjector;
    }

    public XGeneratedContext readContext(Pkg pkg) {
        XGeneratedContext xGeneratedContext = new XGeneratedContext();
        pkg.getConfig()
                .getProperties()
                .getProperties()
                .forEach(
                        (key, value) ->
                                xGeneratedReader.readXGenerated(
                                        List.of(key), value, xGeneratedContext));
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
}
