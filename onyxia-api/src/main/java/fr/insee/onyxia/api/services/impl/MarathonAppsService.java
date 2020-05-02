package fr.insee.onyxia.api.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.onyxia.api.configuration.HttpClientProvider;
import fr.insee.onyxia.api.configuration.properties.RegionsConfiguration;
import fr.insee.onyxia.api.services.AppsService;
import fr.insee.onyxia.api.services.control.AdmissionControllerMarathon;
import fr.insee.onyxia.api.services.control.commons.UrlGenerator;
import fr.insee.onyxia.api.services.control.marathon.security.PermissionsChecker;
import fr.insee.onyxia.api.services.control.utils.IDSanitizer;
import fr.insee.onyxia.api.services.control.utils.PublishContext;
import fr.insee.onyxia.model.User;
import fr.insee.onyxia.model.catalog.Config.Property;
import fr.insee.onyxia.model.catalog.Package;
import fr.insee.onyxia.model.catalog.UniversePackage;
import fr.insee.onyxia.model.dto.CreateServiceDTO;
import fr.insee.onyxia.model.dto.ServicesListing;
import fr.insee.onyxia.model.region.Region;
import fr.insee.onyxia.model.service.Task;
import fr.insee.onyxia.model.service.UninstallService;
import fr.insee.onyxia.mustache.Mustacheur;
import mesosphere.marathon.client.Marathon;
import mesosphere.marathon.client.model.v2.App;
import mesosphere.marathon.client.model.v2.Group;
import mesosphere.marathon.client.model.v2.Result;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Qualifier("Marathon")
public class MarathonAppsService implements AppsService {

    @Autowired(required = false)
    Marathon marathon;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private IDSanitizer idSanitizer;

    @Autowired
    private List<AdmissionControllerMarathon> admissionControllers;

    @Autowired
    private UrlGenerator generator;

    @Autowired
    private HttpClientProvider httpClientProvider;

    private DateFormat marathonDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    @Autowired
    private RegionsConfiguration regionsConfiguration;

    @Autowired
    private PermissionsChecker permissionsChecker;

    @PostConstruct
    public void postConstruct() {
        Collections.sort(admissionControllers, (admissionController, admissionController2) -> {
            return admissionController2.getPriority().compareTo(admissionController.getPriority());
        });
    }


    private static class XGeneratedContext {
        private String groupIdKey;
        private Map<String, Scope> scopes = new HashMap<>();

        public String getGroupIdKey() {
            return groupIdKey;
        }

        public void setGroupIdKey(String groupIdKey) {
            this.groupIdKey = groupIdKey;
        }

        public Map<String, Scope> getScopes() {
            return scopes;
        }

        public void setScopes(Map<String, Scope> scopes) {
            this.scopes = scopes;
        }

        private static class Scope {
            private String scopeName;
            private Map<String, Property.XGenerated> xGenerateds = new HashMap<>();

            public String getScopeName() {
                return scopeName;
            }

            public void setScopeName(String scopeName) {
                this.scopeName = scopeName;
            }

            public Map<String, Property.XGenerated> getxGenerateds() {
                return xGenerateds;
            }

            public void setxGenerateds(Map<String, Property.XGenerated> xGenerateds) {
                this.xGenerateds = xGenerateds;
            }
        }
    }

    private void readXGenerated(List<String> path, Property property, XGeneratedContext context) {
        String currentPath = path.stream().collect(Collectors.joining("."));
        if (property.getProperties() != null) {
            for (Map.Entry<String,Property> prop: property.getProperties().entrySet()) {
                List<String> newPath = new ArrayList<>();
                newPath.addAll(path);
                newPath.add(prop.getKey());
                readXGenerated(newPath, prop.getValue(), context);
            }
        }
        else if (property.getxGenerated() != null) {
            Property.XGenerated xGenerated = property.getxGenerated();
            if (xGenerated.getType() == Property.XGenerated.XGeneratedType.GroupID) {
                context.setGroupIdKey(path.stream().collect(Collectors.joining(".")));
                return;
            }

            String scopeName = xGenerated.getScope();
            if (!context.getScopes().containsKey(scopeName)) {
                context.getScopes().put(scopeName,new XGeneratedContext.Scope());
            }

            XGeneratedContext.Scope scope = context.getScopes().get(scopeName);
            scope.getxGenerateds().put(currentPath,property.getxGenerated());
        }
    }

    private String generateBaseId(Region region, User user, String groupName, String randomizedId) {
        return "/"+region.getNamespacePrefix() + "/" + idSanitizer.sanitize(user.getIdep()) + (groupName != null ? "/" + groupName+"-"+ randomizedId : "");
    }

    private String generateGroupId(Region region, User user, String groupName, String randomizedId) {
        return generateBaseId(region, user, groupName, randomizedId);
    }

    private String generateAppId(Region region, User user, String groupName, String appName, String randomizedId) {
        return generateBaseId(region, user, groupName,randomizedId)+"/"+idSanitizer.sanitize(appName)+(groupName != null ? "" :  "-" + randomizedId);
    }

    private void injectIntoContext(Map<String, Object> context, Map<String,String> toInject ) {
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

    @NotNull
    @Override
    public Collection<Object> installApp(Region region, CreateServiceDTO requestDTO, String catalogId, Package pkg,
            User user, Map<String, Object> fusion) throws Exception {
        PublishContext context = new PublishContext(catalogId);
        UniversePackage universePkg = (UniversePackage) pkg;
        Map<String, Object> resource = universePkg.getResource();
        fusion.putAll(Map.of("resource", resource));

        XGeneratedContext xGeneratedContext = new XGeneratedContext();
        universePkg.getConfig().getProperties().getProperties().entrySet().stream().forEach((entry) -> {
            readXGenerated(Arrays.asList(entry.getKey()),entry.getValue(),xGeneratedContext);
        });

        boolean isGroup = false;
        final String sanitizedPackageName = idSanitizer.sanitize(pkg.getName());
        Map<String,String> xGeneratedValues = new HashMap<>();
        if (xGeneratedContext.getGroupIdKey() != null) {
            isGroup = true;
            if (xGeneratedContext.getGroupIdKey() != null) {
                xGeneratedValues.put(xGeneratedContext.getGroupIdKey(), generateGroupId(region,user, pkg.getName(), context.getGlobalContext().getRandomizedId()));
            }
        }

        final boolean isGroupFinal = isGroup;
        xGeneratedContext.getScopes().forEach((scopeName,scope) -> {
            scope.getxGenerateds().forEach((name,xGenerated) -> {
                if (xGenerated.getType() == Property.XGenerated.XGeneratedType.AppID) {
                    xGeneratedValues.put(name, generateAppId(region,user,isGroupFinal ? sanitizedPackageName : null,scopeName, context.getGlobalContext().getRandomizedId()));
                }
                if (xGenerated.getType() == Property.XGenerated.XGeneratedType.ExternalDNS) {
                    xGeneratedValues.put(name, generator.generateUrl(user.getIdep(), pkg.getName(), context.getGlobalContext().getRandomizedId(), scopeName+(StringUtils.isNotBlank(xGenerated.getName()) ? "-"+xGenerated.getName() : ""), region.getPublishDomain()));
                }

                if (xGenerated.getType() == Property.XGenerated.XGeneratedType.InternalDNS) {
                    xGeneratedValues.put(name, idSanitizer.sanitize(scopeName) + "-" + (isGroupFinal ? sanitizedPackageName + "-" + context.getGlobalContext().getRandomizedId() : sanitizedPackageName) + "-"
                            + idSanitizer.sanitize(user.getIdep()) + "-" + idSanitizer.sanitize(region.getNamespacePrefix()) + "."
                            + region.getMarathonDnsSuffix());
                }
            });
        });

        injectIntoContext(fusion,xGeneratedValues);

        String toMarathon = Mustacheur.mustache(universePkg.getJsonMustache(), fusion);
        Collection<App> apps;
        Group enclosingGroup = null;
        if (isGroup) {
            enclosingGroup = mapper.readValue(toMarathon, Group.class);
            apps = enclosingGroup.getApps();
        } else {
            apps = new ArrayList<>();
            apps.add(mapper.readValue(toMarathon, App.class));
        }

        for (App app : apps) {
            final Group enclosingGroupFinal = enclosingGroup;
            // Apply every admission controller
            long nbInvalidations = admissionControllers.stream().map(admissionController -> admissionController
                    .validateContract(region, enclosingGroupFinal, app, user, universePkg, (Map<String, Object>) requestDTO.getOptions(), context))
                    .filter(b -> !b).count();
            if (nbInvalidations > 0) {
                throw new AccessDeniedException("Validation failed");
            }
        }

        if (requestDTO.isDryRun()) {
            return apps.stream().collect(Collectors.toList());
        } else {
            return apps.stream().map(app -> marathon.createApp(app)).collect(Collectors.toList());
        }
    }

    @Override
    public CompletableFuture<ServicesListing> getUserServices(Region region, User user) throws IllegalAccessException, IOException {
        return getUserServices(region, user, null);
    }

    @Override
    public CompletableFuture<ServicesListing> getUserServices(Region region, User user, String groupId)
            throws IllegalAccessException, IOException {
        String namespacePrefix = region.getNamespacePrefix();
        if (groupId != null && !groupId.startsWith(getUserGroupPath(namespacePrefix,user))) {
            throw new IllegalAccessException("Permission denied. " + user.getIdep() + " can not access " + groupId);
        }

        if (groupId == null) {
            groupId = getUserGroupPath(namespacePrefix,user);
        }
        Group group = getGroup(region, groupId);
        ServicesListing listing = new ServicesListing();
        listing.setApps(group.getApps().stream().map(app -> mapAppToService(app)).collect(Collectors.toList()));
        listing.setGroups(group.getGroups().stream().map(gr -> mapGroup(gr)).collect(Collectors.toList()));
        return CompletableFuture.completedFuture(listing);
    }

    @Override
    public fr.insee.onyxia.model.service.Service getUserService(Region region, User user, String serviceId) throws Exception {
        String fullServiceId = getFullServiceId(user,region.getNamespacePrefix(), serviceId);
        permissionsChecker.checkPermission(region, user,fullServiceId);
        return mapAppToService(marathon.getApp(fullServiceId.substring(1)).getApp());
    }

    @Override
    public String getLogs(Region region, User user,String serviceId, String taskId) {
        return "Feature not implemented";
    }

    @Override
    public UninstallService destroyService(Region region, User user, String serviceId) throws IllegalAccessException {
        String fullId = getFullServiceId(user,region.getNamespacePrefix(), serviceId);
        permissionsChecker.checkPermission(region, user,fullId);
        Result appUninstaller = marathon.deleteApp(fullId.substring(1));
        UninstallService result = new UninstallService();
        result.setId(appUninstaller.getDeploymentId());
        result.setVersion(appUninstaller.getVersion());
        result.setSuccess(true);
        return result;
    }



    private String getFullServiceId(User user, String namespacePrefix, String serviceId) {
        String fullId = serviceId;
        if (!fullId.startsWith("/")) {
            fullId = "/"+getUserGroupPath(namespacePrefix, user) + "/" + serviceId;
        }
        return fullId;
    }

    private fr.insee.onyxia.model.service.Group mapGroup(Group marathonGroup) {
        fr.insee.onyxia.model.service.Group group = new fr.insee.onyxia.model.service.Group();
        group.setId(marathonGroup.getId());
        group.setApps(marathonGroup.getApps().stream().map(app -> mapAppToService(app)).collect(Collectors.toList()));
        return group;
    }

    private fr.insee.onyxia.model.service.Service mapAppToService(App app) {
        fr.insee.onyxia.model.service.Service service = new fr.insee.onyxia.model.service.Service();
        service.setLabels(app.getLabels());
        service.setCpus(app.getCpus());
        service.setInstances(app.getInstances());
        service.setMem(app.getMem());
        service.setName(app.getLabels().get("ONYXIA_NAME"));
        service.setId(app.getId());
        service.setType(fr.insee.onyxia.model.service.Service.ServiceType.MARATHON);
        List<String> uris = new ArrayList<String>();
        uris.add(app.getLabels().get("ONYXIA_URL"));
        service.setUrls(uris);
        service.setLogo(app.getLabels().get("ONYXIA_LOGO"));
        app.getTasks().stream().findFirst().ifPresent(task -> {
            try {
                service.setStartedAt(marathonDateFormat.parse(task.getStartedAt()).getTime());
            } catch (Exception e) {
            }
        });
        service.setTasks(app.getTasks().stream().map(task -> {
            Task serviceTask = new Task();
            serviceTask.setId(task.getId());
            return serviceTask;
        }).collect(Collectors.toList()));
        if (app.getEnv() != null) {
            app.getEnv().entrySet().stream()
                    .forEach(entry -> service.getEnv().put(entry.getKey(), entry.getValue().toString()));
        }
        service.setStatus(findAppStatus(app));
        return service;
    }

    private fr.insee.onyxia.model.service.Service.ServiceStatus findAppStatus(App app) {
        if (app.getTasksRunning() != null && app.getTasksRunning() > 0) {
            return fr.insee.onyxia.model.service.Service.ServiceStatus.RUNNING;
        } else if (app.getInstances() != null && app.getInstances() == 0) {
            return fr.insee.onyxia.model.service.Service.ServiceStatus.STOPPED;
        } else {
            return fr.insee.onyxia.model.service.Service.ServiceStatus.DEPLOYING;
        }
    }

    private String getUserGroupPath(String namespacePrefix, User user) {
        return namespacePrefix + "/" + user.getIdep();
    }

    /**
     * This methods uses a custom request as the marathon.getGroup() does not
     * returns full data on apps (e.g : no tasks data)
     *
     * @param id
     * @return
     * @throws IOException
     */
    private Group getGroup(Region region, String id) throws IOException {

        Request requete = new Request.Builder().url(region.getServerUrl() + "/v2/groups/" + id + "?" + "embed=group.groups" + "&"
                + "embed=group.apps" + "&" + "embed=group.apps.tasks" + "&" + "embed=group.apps.counts" + "&"
                + "embed=group.apps.deployments" + "&" + "embed=group.apps.readiness" + "&"
                + "embed=group.apps.lastTaskFailure" + "&" + "embed=group.pods" + "&" + "embed=group.apps.taskStats")
                .build();
        Response response = httpClientProvider.getClientForRegion(region).newCall(requete).execute();
        Group groupResponse = mapper.readValue(response.body().byteStream(), Group.class);
        return groupResponse;

    }



}
