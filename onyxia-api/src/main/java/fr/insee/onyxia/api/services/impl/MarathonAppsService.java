package fr.insee.onyxia.api.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.onyxia.api.configuration.HttpClientProvider;
import fr.insee.onyxia.api.services.AppsService;
import fr.insee.onyxia.api.services.control.AdmissionControllerMarathon;
import fr.insee.onyxia.api.services.control.commons.UrlGenerator;
import fr.insee.onyxia.api.services.control.marathon.security.PermissionsChecker;
import fr.insee.onyxia.api.services.control.utils.IDSanitizer;
import fr.insee.onyxia.api.services.control.utils.PublishContext;
import fr.insee.onyxia.api.services.control.xgenerated.XGeneratedContext;
import fr.insee.onyxia.api.services.control.xgenerated.XGeneratedProcessor;
import fr.insee.onyxia.api.services.control.xgenerated.XGeneratedProvider;
import fr.insee.onyxia.model.User;
import fr.insee.onyxia.model.catalog.Config.Property;
import fr.insee.onyxia.model.catalog.Pkg;
import fr.insee.onyxia.model.catalog.UniversePackage;
import fr.insee.onyxia.model.dto.CreateServiceDTO;
import fr.insee.onyxia.model.dto.ServicesListing;
import fr.insee.onyxia.model.region.Region;
import fr.insee.onyxia.model.service.Event;
import fr.insee.onyxia.model.service.Task;
import fr.insee.onyxia.model.service.UninstallService;
import fr.insee.onyxia.mustache.Mustacheur;
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

    @Autowired
    MarathonClientProvider marathonClientProvider;

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
    private PermissionsChecker permissionsChecker;

    @Autowired
    private XGeneratedProcessor xGeneratedProcessor;

    @PostConstruct
    public void postConstruct() {
        Collections.sort(admissionControllers, (admissionController, admissionController2) -> {
            return admissionController2.getPriority().compareTo(admissionController.getPriority());
        });
    }




    private String generateBaseId(Region region, User user, String groupName, String randomizedId) {
        return "/"+region.getServices().getNamespacePrefix() + "/" + idSanitizer.sanitize(user.getIdep()) + (groupName != null ? "/" + idSanitizer.sanitize(groupName)+"-"+ randomizedId : "");
    }

    private String generateGroupId(Region region, User user, String groupName, String randomizedId) {
        return generateBaseId(region, user, groupName, randomizedId);
    }

    private String generateAppId(Region region, User user, String groupName, String appName, String randomizedId, boolean isCloudshell) {
        String appId = idSanitizer.sanitize(appName)+(groupName != null ? "" :  "-" + randomizedId);
        if (isCloudshell) {
            appId = "cloudshell";
        }
        return generateBaseId(region, user, groupName,randomizedId)+"/"+appId;
    }



    public String getInternalDnsFromId(String id, String dnsSuffix) {
        String[] parts = id.split("/");
        StringBuffer internalDns = new StringBuffer();
        for (int i = parts.length - 1; i >= 0; i--) {
            internalDns.append(parts[i]);
            if (i > 1) {
                internalDns.append("-");
            }
        }
        internalDns.append(".");
        internalDns.append(dnsSuffix);
        return internalDns.toString();
    }

    @NotNull
    @Override
    public Collection<Object> installApp(Region region, CreateServiceDTO requestDTO, String catalogId, Pkg pkg,
            User user, Map<String, Object> fusion) throws Exception {
        PublishContext context = new PublishContext(catalogId);
        UniversePackage universePkg = (UniversePackage) pkg;
        Map<String, Object> resource = universePkg.getResource();
        fusion.putAll(Map.of("resource", resource));

        XGeneratedContext xGeneratedContext = xGeneratedProcessor.readContext(pkg);
        final boolean isGroup = xGeneratedContext.getGroupIdKey() != null;
        final String sanitizedPackageName = idSanitizer.sanitize(pkg.getName());
        final boolean isCloudshell = region.getServices().getCloudshell() != null && region.getServices().getCloudshell().getCatalogId().equals(catalogId) && region.getServices().getCloudshell().getPackageName().equals(pkg.getName());

        XGeneratedProvider xGeneratedProvider = new XGeneratedProvider() {

            @Override
            public String getGroupId() {
                return generateGroupId(region,user, sanitizedPackageName, context.getGlobalContext().getRandomizedId());
            }

            @Override
            public String getAppId(String scopeName, XGeneratedContext.Scope scope, Property.XGenerated xGenerated) {
                return generateAppId(region,user,isGroup ? sanitizedPackageName : null,scopeName, context.getGlobalContext().getRandomizedId(), isCloudshell);
            }

            @Override
            public String getExternalDns(String scopeName, XGeneratedContext.Scope scope, Property.XGenerated xGenerated) {
                return generator.generateUrl(user.getIdep(), sanitizedPackageName, context.getGlobalContext().getRandomizedId(), scopeName+(StringUtils.isNotBlank(xGenerated.getName()) ? "-"+xGenerated.getName() : ""), region.getServices().getExpose().getDomain());
            }

            @Override
            public String getInternalDns(String scopeName, XGeneratedContext.Scope scope, Property.XGenerated xGenerated) {
                return getInternalDnsFromId(getAppId(scopeName, scope, xGenerated), region.getServices().getMarathonDnsSuffix());
            }

            @Override
            public String getNetworkName(String scopeName, XGeneratedContext.Scope scope, Property.XGenerated xGenerated) {
                return region.getServices().getNetwork();
            }

            @Override
            public String getInitScript(String scopeName, XGeneratedContext.Scope scope, Property.XGenerated xGenerated) {
                return region.getServices().getInitScript();
            }
        };

        Map<String,String> xGeneratedValues = xGeneratedProcessor.process(xGeneratedContext,xGeneratedProvider);
        xGeneratedProcessor.injectIntoContext(fusion,xGeneratedValues);

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
            return apps.stream().map(app -> marathonClientProvider.getMarathonClientForRegion(region).createApp(app)).collect(Collectors.toList());
        }
    }

    @Override
    public CompletableFuture<ServicesListing> getUserServices(Region region, User user) throws IllegalAccessException, IOException {
        return getUserServices(region, user, null);
    }

    @Override
    public CompletableFuture<ServicesListing> getUserServices(Region region, User user, String groupId)
            throws IllegalAccessException, IOException {
        String namespacePrefix = region.getServices().getNamespacePrefix();
        if (groupId != null && !groupId.startsWith(getUserGroupPath(namespacePrefix,user))) {
            throw new IllegalAccessException("Permission denied. " + user.getIdep() + " can not access " + groupId);
        }

        if (groupId == null) {
            groupId = getUserGroupPath(namespacePrefix,user);
        }
        Group group = getGroup(region, groupId);
        ServicesListing listing = new ServicesListing();
        if (group.getApps() != null) {
            listing.setApps(group.getApps().stream().map(app -> mapAppToService(app)).collect(Collectors.toList()));
        }
        if (group.getGroups() != null) {
            listing.setGroups(group.getGroups().stream().map(gr -> mapGroup(gr)).collect(Collectors.toList()));
        }


        return CompletableFuture.completedFuture(listing);
    }

    @Override
    public fr.insee.onyxia.model.service.Service getUserService(Region region, User user, String serviceId) throws Exception {
        String fullServiceId = getFullServiceId(user,region.getServices().getNamespacePrefix(), serviceId);
        permissionsChecker.checkPermission(region, user,fullServiceId);
        return mapAppToService(marathonClientProvider.getMarathonClientForRegion(region).getApp(fullServiceId.substring(1)).getApp());
    }

    @Override
    public String getLogs(Region region, User user,String serviceId, String taskId) {
        return "Feature not implemented";
    }

    @Override
    public UninstallService destroyService(Region region, User user, String path, boolean bulk) throws IllegalAccessException {
        String fullId = getFullServiceId(user,region.getServices().getNamespacePrefix(), path);
        permissionsChecker.checkPermission(region, user,fullId);
        Result appUninstaller;
        if (bulk) {
            appUninstaller = marathonClientProvider.getMarathonClientForRegion(region).deleteGroup(fullId.substring(1), true);
        }
        else {
            appUninstaller = marathonClientProvider.getMarathonClientForRegion(region).deleteApp(fullId.substring(1));
        }
        UninstallService result = new UninstallService();
        result.setPath(path);
        result.setSuccess(true);
        return result;
    }



    private String getFullServiceId(User user, String namespacePrefix, String serviceId) {
        String basePath = "/"+getUserGroupPath(namespacePrefix, user) + "/";
        if (serviceId == null) {
            return basePath;
        }
        String fullId = serviceId;
        if (!fullId.startsWith("/")) {
            fullId = basePath + serviceId;
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
        service.setName( app.getLabels().get("ONYXIA_TITLE") != null ?app.getLabels().get("ONYXIA_TITLE") : app.getLabels().get("ONYXIA_NAME"));
        service.setId(app.getId());
        service.setType(fr.insee.onyxia.model.service.Service.ServiceType.MARATHON);

        List<String> uris = new ArrayList<String>();
        if (app.getLabels().containsKey("ONYXIA_URL")) {
            String onyxiaUrls = app.getLabels().get("ONYXIA_URL");
            uris.addAll(Arrays.asList(onyxiaUrls.split(",")));
        }
        service.setUrls(uris);
        List<String> internalUrls = new ArrayList<String>();
        if (app.getLabels().containsKey("ONYXIA_PRIVATE_ENDPOINT")) {
            internalUrls.add(app.getLabels().get("ONYXIA_PRIVATE_ENDPOINT"));
        }
        service.setInternalUrls(internalUrls);
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

        if (app.getLastTaskFailure() != null) {
            Event event = new Event();
            event.setMessage(app.getLastTaskFailure().getMessage());
            try {
                event.setTimestamp(marathonDateFormat.parse(app.getLastTaskFailure().getTimestamp()).getTime());
            }
            catch (Exception e) {

            }
            service.getEvents().add(event);
        }

        if (app.getLabels().containsKey("ONYXIA_MONITORING")) {
            fr.insee.onyxia.model.service.Service.Monitoring monitoring = new fr.insee.onyxia.model.service.Service.Monitoring();
            monitoring.setUrl(app.getLabels().get("ONYXIA_MONITORING"));
            service.setMonitoring(monitoring);
        }

        if (app.getLabels().containsKey("ONYXIA_POST_INSTALL_INSTRUCTIONS")) {
            service.setPostInstallInstructions(app.getLabels().get("ONYXIA_POST_INSTALL_INSTRUCTIONS"));
        }

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

        Request requete = new Request.Builder().url(region.getServices().getServer().getUrl() + "/v2/groups/" + id + "?" + "embed=group.groups" + "&"
                + "embed=group.apps" + "&" + "embed=group.apps.tasks" + "&" + "embed=group.apps.counts" + "&"
                + "embed=group.apps.deployments" + "&" + "embed=group.apps.readiness" + "&"
                + "embed=group.apps.lastTaskFailure" + "&" + "embed=group.pods" + "&" + "embed=group.apps.taskStats")
                .build();
        Response response = httpClientProvider.getClientForRegion(region).newCall(requete).execute();
        Group groupResponse = mapper.readValue(response.body().byteStream(), Group.class);
        return groupResponse;

    }



}
