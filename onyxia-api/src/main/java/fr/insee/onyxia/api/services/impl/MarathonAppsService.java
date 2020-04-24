package fr.insee.onyxia.api.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.onyxia.api.services.AppsService;
import fr.insee.onyxia.api.services.control.AdmissionController;
import fr.insee.onyxia.api.services.control.marathon.UrlGenerator;
import fr.insee.onyxia.api.services.control.utils.IDSanitizer;
import fr.insee.onyxia.api.services.control.utils.PublishContext;
import fr.insee.onyxia.model.User;
import fr.insee.onyxia.model.catalog.Package;
import fr.insee.onyxia.model.catalog.UniversePackage;
import fr.insee.onyxia.model.dto.CreateServiceDTO;
import fr.insee.onyxia.mustache.Mustacheur;
import mesosphere.marathon.client.Marathon;
import mesosphere.marathon.client.model.v2.App;
import mesosphere.marathon.client.model.v2.GetAppResponse;
import mesosphere.marathon.client.model.v2.Group;
import mesosphere.marathon.client.model.v2.VersionedApp;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Service
@Qualifier("Marathon")
public class MarathonAppsService implements AppsService {
    @Value("${marathon.dns.suffix}")
    private String MARATHON_DNS_SUFFIX;

    @Value("${marathon.group.name}")
    private String MARATHON_GROUP_NAME;

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

    private @Value("${marathon.url}") String MARATHON_URL;

    private DateFormat marathonDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    @PostConstruct
    public void postConstruct() {
        Collections.sort(admissionControllers, (admissionController, admissionController2) -> {
            return admissionController2.getPriority().compareTo(admissionController.getPriority());
        });
    }

    @NotNull
    public Collection<Object> installApp(CreateServiceDTO requestDTO, boolean isGroup, String catalogId, Package pkg,
            User user, Map<String, Object> fusion) throws Exception {
        PublishContext context = new PublishContext(catalogId);
        UniversePackage universePkg = (UniversePackage) pkg;
        Map<String, Object> resource = universePkg.getResource();
        fusion.putAll(Map.of("resource", resource));

        Map<String, String> contextData = new HashMap<>();
        contextData.put("internaldns",
                idSanitizer.sanitize(pkg.getName()) + "-" + context.getRandomizedId() + "-"
                        + idSanitizer.sanitize(user.getIdep()) + "-" + idSanitizer.sanitize(MARATHON_GROUP_NAME) + "."
                        + MARATHON_DNS_SUFFIX);

        for (int i = 0; i < 10; i++) {
            contextData.put("externaldns-" + i,
                    generator.generateUrl(user.getIdep(), pkg.getName(), context.getRandomizedId(), i));
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
                    .validateContract(app, user, universePkg, (Map<String, Object>) requestDTO.getOptions(), context))
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

    public VersionedApp getServiceById(String id) {
        GetAppResponse appsResponse = marathon.getApp(id);
        VersionedApp app = appsResponse.getApp();
        return app;
    }

    public Group getGroups(String id) throws IOException {
        Request requete = new Request.Builder().url(MARATHON_URL + "/v2/groups/users/" + id + "?" + "embed=group.groups"
                + "&" + "embed=group.apps" + "&" + "embed=group.apps.tasks" + "&" + "embed=group.apps.counts" + "&"
                + "embed=group.apps.deployments" + "&" + "embed=group.apps.readiness" + "&"
                + "embed=group.apps.lastTaskFailure" + "&" + "embed=group.pods" + "&" + "embed=group.apps.taskStats")
                .build();
        Response response = marathonClient.newCall(requete).execute();
        Group groupResponse = mapper.readValue(response.body().byteStream(), Group.class);
        return groupResponse;
    }

    @Override
    public CompletableFuture<List<fr.insee.onyxia.model.service.Service>> getUserServices(User user)
            throws InterruptedException, TimeoutException, IOException {
        Group group = getGroups(user.getIdep());
        return CompletableFuture.completedFuture(
                group.getApps().stream().map(app -> getServiceFromApp(app)).collect(Collectors.toList()));
    }

    private fr.insee.onyxia.model.service.Service getServiceFromApp(App app) {
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
        service.setStatus(findAppStatus(app));
        return service;
    }

    private fr.insee.onyxia.model.service.Service.ServiceStatus findAppStatus(App app) {
        if (app.getTasksRunning() > 0) {
            return fr.insee.onyxia.model.service.Service.ServiceStatus.RUNNING;
        } else if (app.getInstances() == 0) {
            return fr.insee.onyxia.model.service.Service.ServiceStatus.STOPPED;
        } else {
            return fr.insee.onyxia.model.service.Service.ServiceStatus.DEPLOYING;
        }
    }

    @Override
    public Object getApp(String serviceId, User user) throws IOException {
        String url = MARATHON_URL + "/v2/apps/users/" + user.getIdep() + "/" + serviceId + "?" + "embed=app.tasks" + "&"
                + "embed=app.counts" + "&" + "embed=app.deployments" + "&" + "embed=app.readiness" + "&"
                + "embed=app.lastTaskFailure" + "&" + "embed=app.taskStats";

        Request requete = new Request.Builder().url(url).build();
        Response response = marathonClient.newCall(requete).execute();

        return response.body().string();
    }

}
