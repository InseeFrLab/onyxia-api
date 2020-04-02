package fr.insee.onyxia.api.controller.api.mylab;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.websocket.server.PathParam;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.onyxia.api.services.control.PublishContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import fr.insee.onyxia.api.configuration.Catalogs;
import fr.insee.onyxia.api.configuration.metrics.CustomMetrics;
import fr.insee.onyxia.api.services.CatalogService;
import fr.insee.onyxia.api.services.UserDataService;
import fr.insee.onyxia.api.services.UserProvider;
import fr.insee.onyxia.api.services.control.AdmissionController;
import fr.insee.onyxia.model.User;
import fr.insee.onyxia.model.catalog.Package;
import fr.insee.onyxia.model.catalog.Universe;
import fr.insee.onyxia.model.catalog.UniversePackage;
import fr.insee.onyxia.model.dto.CreateServiceDTO;
import fr.insee.onyxia.model.dto.UpdateServiceDTO;
import fr.insee.onyxia.mustache.Mustacheur;
import io.swagger.v3.oas.annotations.tags.Tag;
import mesosphere.marathon.client.Marathon;
import mesosphere.marathon.client.MarathonException;
import mesosphere.marathon.client.model.v2.App;
import mesosphere.marathon.client.model.v2.Group;
import mesosphere.marathon.client.model.v2.Result;
import mesosphere.marathon.client.model.v2.VersionedApp;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Tag(name = "My lab", description = "My services")
@RequestMapping("/my-lab")
@RestController
public class MyLabController {

    /**
     * @deprecated : should be moved to the marathon http client
     */
    @Deprecated
    @Value("${marathon.url}")
    String MARATHON_URL;

    @Autowired
    @Qualifier("marathon")
    private OkHttpClient marathonClient;

    @Autowired
    private CustomMetrics metrics;

    @Autowired
    private UserProvider userProvider;

    @Autowired
    private UserDataService userDataService;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private CatalogService catalogService;

    @Autowired
    private Catalogs catalogs;

    @Autowired
    private List<AdmissionController> admissionControllers;

    @Autowired(required = false)
    private Marathon marathon;

    @Value("${marathon.group.name}")
    private String MARATHON_GROUP_NAME;

    @PostConstruct
    public void postConstruct() {
        Collections.sort(admissionControllers, (admissionController, admissionController2) -> {
            return admissionController2.getPriority().compareTo(admissionController.getPriority());
        });
    }

    @GetMapping("/group")
    public Group getGroup(@RequestParam(value = "groupId", required = false) String id)
            throws JsonParseException, JsonMappingException, IOException {
        String uri = "";
        if (id != null && !id.equals("")) {
            uri = "/" + id;
        }
        Request requete = new Request.Builder().url(MARATHON_URL + "/v2/groups/users/"
                + userProvider.getUser().getIdep() + uri + "?" + "embed=group.groups" + "&" + "embed=group.apps" + "&"
                + "embed=group.apps.tasks" + "&" + "embed=group.apps.counts" + "&" + "embed=group.apps.deployments"
                + "&" + "embed=group.apps.readiness" + "&" + "embed=group.apps.lastTaskFailure" + "&"
                + "embed=group.pods" + "&" + "embed=group.apps.taskStats").build();
        Response response = marathonClient.newCall(requete).execute();
        Group groupResponse = mapper.readValue(response.body().byteStream(), Group.class);
        return groupResponse;

    }

    @GetMapping("/app")
    public @ResponseBody String getApp(@RequestParam("serviceId") String id)
            throws JsonParseException, JsonMappingException, IOException {

        String url = MARATHON_URL + "/v2/apps/users/" + userProvider.getUser().getIdep() + "/" + id + "?"
                + "embed=app.tasks" + "&" + "embed=app.counts" + "&" + "embed=app.deployments" + "&"
                + "embed=app.readiness" + "&" + "embed=app.lastTaskFailure" + "&" + "embed=app.taskStats";

        Request requete = new Request.Builder().url(url).build();
        Response response = marathonClient.newCall(requete).execute();

        return response.body().string();

    }

    @DeleteMapping("/app")
    public Result destroyApp(@RequestParam("serviceId") String id) throws MarathonException {

        if (id == null || !id.startsWith("/users/" + userProvider.getUser().getIdep())) {
            throw new RuntimeException("hack!");
        }
        Result result = marathon.deleteApp(id);
        return result;
    }

    @DeleteMapping("/group")
    public Result destroyGroup(@RequestParam("serviceId") String id) throws MarathonException {
        if (id == null || !id.startsWith("/users/" + userProvider.getUser().getIdep())) {
            throw new RuntimeException("hack!");
        }
        Result result = marathon.deleteGroup(id, true);
        return result;
    }

    @PostMapping("/app")
    public Result update(@RequestBody UpdateServiceDTO serviceId) throws MarathonException {
        String id = serviceId.getServiceId();
        if (id == null || !id.startsWith("/users/" + userProvider.getUser().getIdep())) {
            throw new RuntimeException("hack!");
        }
        App app = marathon.getApp(id).getApp();
        if (serviceId.getCpus() != null && serviceId.getCpus() > 0) {
            app.setCpus(serviceId.getCpus());
        }
        if (serviceId.getMems() != null && serviceId.getMems() > 0) {
            app.setMem(serviceId.getMems());
        }

        app.setInstances(serviceId.getInstances());
        if (serviceId.getFriendlyName() != null && !serviceId.getFriendlyName().equals("")) {
            app.getLabels().put("ONYXIA_TITLE", serviceId.getFriendlyName());
        }
        if (serviceId.getEnv() != null) {
            for (String key : serviceId.getEnv().keySet()) {
                app.getEnv().put(key, serviceId.getEnv().get(key));
            }
        }

        Result result = marathon.updateApp(id, app, true);
        return result;
    }

    @PutMapping("/app")
    public App publishService(@RequestBody CreateServiceDTO requestDTO)
            throws JsonProcessingException, IOException, MarathonException, Exception {
        return publishApps(requestDTO, false).stream().findFirst().get();
    }

    @PutMapping("/group")
    public Collection<App> publishGroup(@RequestBody CreateServiceDTO requestDTO)
            throws JsonProcessingException, IOException, MarathonException, Exception {
        return publishApps(requestDTO, true);
    }

    private Collection<App> publishApps(CreateServiceDTO requestDTO, boolean isGroup)
            throws JsonProcessingException, IOException, MarathonException, Exception {
        String catalogId = "internal";
        if (requestDTO.getCatalogId() != null && requestDTO.getCatalogId().length() > 0) {
            catalogId = requestDTO.getCatalogId();
        }
        Package pkg = catalogService.getPackage(catalogId, requestDTO.getPackageName());

        User user = userProvider.getUser();
        userDataService.fetchUserData(user);

        Map<String, Object> resource = pkg.getResource();
        Map<String, Object> fusion = new HashMap<>();
        fusion.putAll((Map<String, Object>) requestDTO.getOptions());
        fusion.putAll(Map.of("resource", resource));

        String toMarathon = Mustacheur.mustache(pkg.getJsonMustache(), fusion);
        Collection<App> apps;
        if (isGroup) {
            Group group = mapper.readValue(toMarathon, Group.class);
            apps = group.getApps();
        } else {
            apps = new ArrayList<>();
            apps.add(mapper.readValue(toMarathon, App.class));
        }

        for (App app : apps) {
            PublishContext context = new PublishContext(catalogId);

            // Apply every admission controller
            long nbInvalidations = admissionControllers.stream().map(admissionController -> admissionController
                    .validateContract(app, user, pkg, (Map<String, Object>) requestDTO.getOptions(), context))
                    .filter(b -> !b).count();
            if (nbInvalidations > 0) {
                throw new AccessDeniedException("Validation failed");
            }
        }

        if (requestDTO.isDryRun()) {
            return apps;
        } else {
            return apps.stream().map(app -> marathon.createApp(app)).collect(Collectors.toList());
        }
    }

}
