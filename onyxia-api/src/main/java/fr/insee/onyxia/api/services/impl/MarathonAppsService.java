package fr.insee.onyxia.api.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.onyxia.api.configuration.properties.RegionsConfiguration;
import fr.insee.onyxia.api.services.AppsService;
import fr.insee.onyxia.api.services.control.AdmissionController;
import fr.insee.onyxia.api.services.control.commons.UrlGenerator;
import fr.insee.onyxia.api.services.control.utils.IDSanitizer;
import fr.insee.onyxia.api.services.control.utils.PublishContext;
import fr.insee.onyxia.model.User;
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
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
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
    private List<AdmissionController> admissionControllers;

    @Autowired
    private UrlGenerator generator;

    @Autowired
    @Qualifier("marathon")
    private OkHttpClient marathonClient;

    private DateFormat marathonDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    @Autowired
    private RegionsConfiguration regionsConfiguration;

    @PostConstruct
    public void postConstruct() {
        Collections.sort(admissionControllers, (admissionController, admissionController2) -> {
            return admissionController2.getPriority().compareTo(admissionController.getPriority());
        });
    }

    @NotNull
    @Override
    public Collection<Object> installApp(Region region, CreateServiceDTO requestDTO, boolean isGroup, String catalogId, Package pkg,
            User user, Map<String, Object> fusion) throws Exception {
        PublishContext context = new PublishContext(catalogId);
        UniversePackage universePkg = (UniversePackage) pkg;
        Map<String, Object> resource = universePkg.getResource();
        fusion.putAll(Map.of("resource", resource));

        Map<String, String> contextData = new HashMap<>();
        contextData.put("internaldns",
                idSanitizer.sanitize(pkg.getName()) + "-" + context.getRandomizedId() + "-"
                        + idSanitizer.sanitize(user.getIdep()) + "-" + idSanitizer.sanitize(region.getNamespacePrefix()) + "."
                        + region.getMarathonDnsSuffix());

        for (int i = 0; i < 10; i++) {
            contextData.put("externaldns-" + i,
                    generator.generateUrl(user.getIdep(), pkg.getName(), context.getRandomizedId(), i, region.getPublishDomain()));
        }

        fusion.put("context", contextData);

        String toMarathon = Mustacheur.mustache(universePkg.getJsonMustache(), fusion);
        Collection<App> apps;
        if (isGroup) {
            Group group = mapper.readValue(toMarathon, Group.class);
            apps = group.getApps();
        } else {
            apps = new ArrayList<>();
            apps.add(mapper.readValue(toMarathon, App.class));
        }

        for (App app : apps) {

            // Apply every admission controller
            long nbInvalidations = admissionControllers.stream().map(admissionController -> admissionController
                    .validateContract(region, app, user, universePkg, (Map<String, Object>) requestDTO.getOptions(), context))
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
        checkPermission(region, user,fullServiceId);
        return mapAppToService(marathon.getApp(fullServiceId.substring(1)).getApp());
    }

    @Override
    public String getLogs(Region region, User user,String serviceId, String taskId) {
        return "Feature not implemented";
    }

    @Override
    public UninstallService destroyService(Region region, User user, String serviceId) throws IllegalAccessException {
        String fullId = getFullServiceId(user,region.getNamespacePrefix(), serviceId);
        checkPermission(region, user,fullId);
        Result appUninstaller = marathon.deleteApp(fullId.substring(1));
        UninstallService result = new UninstallService();
        result.setId(appUninstaller.getDeploymentId());
        result.setVersion(appUninstaller.getVersion());
        result.setSuccess(true);
        return result;
    }

    private void checkPermission(Region region, User user, String fullId) throws IllegalAccessException {
        if (!fullId.startsWith("/"+region.getNamespacePrefix()+"/"+user.getIdep())) {
            throw new IllegalAccessException("User "+user.getIdep()+" can not access "+fullId);
        }
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
        Response response = marathonClient.newCall(requete).execute();
        Group groupResponse = mapper.readValue(response.body().byteStream(), Group.class);
        return groupResponse;

    }



}
