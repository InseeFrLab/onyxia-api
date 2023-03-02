package fr.insee.onyxia.api.services.control.xgenerated;

import fr.insee.onyxia.model.catalog.Config.Property;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class XGeneratedReader {

    public void readXGenerated(List<String> path, Property property, XGeneratedContext context) {
        String currentPath = path.stream().collect(Collectors.joining("."));
        if (property.getProperties() != null) {
            for (Map.Entry<String, Property> prop : property.getProperties().entrySet()) {
                List<String> newPath = new ArrayList<>();
                newPath.addAll(path);
                newPath.add(prop.getKey());
                readXGenerated(newPath, prop.getValue(), context);
            }
        } else if (property.getxGenerated() != null) {
            Property.XGenerated xGenerated = property.getxGenerated();
            if (xGenerated.getType() == Property.XGenerated.XGeneratedType.GroupID) {
                context.setGroupIdKey(path.stream().collect(Collectors.joining(".")));
                return;
            }

            String scopeName = xGenerated.getScope();
            if (!context.getScopes().containsKey(scopeName)) {
                context.getScopes().put(scopeName, new XGeneratedContext.Scope());
            }

            XGeneratedContext.Scope scope = context.getScopes().get(scopeName);
            scope.getxGenerateds().put(currentPath, property.getxGenerated());
        }
    }
}
