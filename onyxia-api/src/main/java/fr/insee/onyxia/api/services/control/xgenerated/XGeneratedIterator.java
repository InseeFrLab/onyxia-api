package fr.insee.onyxia.api.services.control.xgenerated;

import fr.insee.onyxia.model.catalog.Config.Property;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class XGeneratedIterator {

    public void iterateOverContext(XGeneratedContext context, XGeneratedProvider xGeneratedProvider, Map<String,String> xGeneratedValues) {

        if (context.getGroupIdKey() != null) {
            if (context.getGroupIdKey() != null) {
                xGeneratedValues.put(context.getGroupIdKey(), xGeneratedProvider.getGroupId());
            }
        }

        context.getScopes().forEach((scopeName,scope) -> {
            scope.getxGenerateds().forEach((name,xGenerated) -> {
                if (xGenerated.getType() == Property.XGenerated.XGeneratedType.AppID) {
                    xGeneratedValues.put(name, xGeneratedProvider.getAppId(scopeName,scope,xGenerated));
                }
                if (xGenerated.getType() == Property.XGenerated.XGeneratedType.ExternalDNS) {
                    xGeneratedValues.put(name, xGeneratedProvider.getExternalDns(scopeName,scope,xGenerated));
                }

                if (xGenerated.getType() == Property.XGenerated.XGeneratedType.InternalDNS) {
                    xGeneratedValues.put(name, xGeneratedProvider.getInternalDns(scopeName,scope,xGenerated));
                }
                if (xGenerated.getType() == Property.XGenerated.XGeneratedType.InitScript) {
                    xGeneratedValues.put(name, xGeneratedProvider.getInitScript(scopeName,scope,xGenerated));
                }
            });
        });
    }
}
