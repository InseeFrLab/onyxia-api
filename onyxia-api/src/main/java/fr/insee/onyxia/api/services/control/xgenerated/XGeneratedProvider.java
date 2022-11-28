package fr.insee.onyxia.api.services.control.xgenerated;

import fr.insee.onyxia.model.catalog.Config.Property;

public interface XGeneratedProvider {

    public String getGroupId();

    public String getAppId(
            String scopeName, XGeneratedContext.Scope scope, Property.XGenerated xGenerated);

    public String getExternalDns(
            String scopeName, XGeneratedContext.Scope scope, Property.XGenerated xGenerated);

    public String getInternalDns(
            String scopeName, XGeneratedContext.Scope scope, Property.XGenerated xGenerated);

    public String getInitScript(
            String scopeName, XGeneratedContext.Scope scope, Property.XGenerated xGenerated);
}
