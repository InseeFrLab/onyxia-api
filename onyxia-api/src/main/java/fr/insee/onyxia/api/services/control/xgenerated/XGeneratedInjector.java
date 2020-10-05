package fr.insee.onyxia.api.services.control.xgenerated;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class XGeneratedInjector {

    public void injectIntoContext(Map<String, Object> context, Map<String,String> toInject ) {
        toInject.forEach((k,v) -> {
            String[] splittedPath = k.split("\\.");
            Map<String,Object> currentContext = context;
            for (int i = 0; i < splittedPath.length - 1; i++) {
                currentContext.putIfAbsent(splittedPath[i], new HashMap<String,Object>());
                currentContext = (Map<String,Object>) currentContext.get(splittedPath[i]);
            }
            currentContext.put(splittedPath[splittedPath.length-1],v);
        });
    }
}
