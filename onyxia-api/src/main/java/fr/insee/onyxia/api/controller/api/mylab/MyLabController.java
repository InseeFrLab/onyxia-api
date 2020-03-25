package fr.insee.onyxia.api.controller.api.mylab;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.websocket.server.PathParam;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
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

import fr.insee.onyxia.api.configuration.Multiverse;
import fr.insee.onyxia.api.configuration.metrics.CustomMetrics;
import fr.insee.onyxia.api.services.CatalogService;
import fr.insee.onyxia.api.services.UserDataService;
import fr.insee.onyxia.api.services.UserProvider;
import fr.insee.onyxia.api.services.control.AdmissionController;
import fr.insee.onyxia.model.User;
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

    @Value("${marathon.url}")
    private String MARATHON_URL;

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
    private Multiverse multiverse;

    @Autowired
    private List<AdmissionController> admissionControllers;

    @Autowired(required = false)
    private Marathon marathon;

    @Autowired
    private OkHttpClient httpClient;

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
        Response response = httpClient.newCall(requete).execute();
        Group groupResponse = mapper.readValue(response.body().byteStream(), Group.class);
        return groupResponse;

    }

    @GetMapping("/package")
    public Universe getAvailableServices() throws Exception {
        return multiverse.getUniverseById("inno").getUniverse();
    }

    @GetMapping("/package/{name}")
    public UniversePackage getPackage(@PathParam("name") String name) throws Exception {
        return multiverse.getUniverseById("inno").getUniverse().getPackageByName(name);
    }

    // @GetMapping("/package/tags")
    // public Map<String, Map<String, List>> getPackageTags() throws Exception {
    // return multiverse.getUniverseById("inno").getUniverse().getTypeOfFile();
    // }

    // TODO : return an inputstream instead of a resolved string
    @GetMapping("/app")
    public @ResponseBody String getApp(@RequestParam("serviceId") String id)
            throws JsonParseException, JsonMappingException, IOException {

        String url = MARATHON_URL + "/v2/apps/users/" + userProvider.getUser().getIdep() + "/" + id + "?"
                + "embed=app.tasks" + "&" + "embed=app.counts" + "&" + "embed=app.deployments" + "&"
                + "embed=app.readiness" + "&" + "embed=app.lastTaskFailure" + "&" + "embed=app.taskStats";

        Request requete = new Request.Builder().url(url).build();
        Response response = httpClient.newCall(requete).execute();

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
        // String id = serviceId.serviceId;
        // if (id == null || id.length() < 1) {
        // id = "XXX_XXX";
        // }
        // HttpResponse<JsonNode> response = Unirest.delete(MarathonRessource.PATH_MESOS
        // + "/v2/apps" + id).asJson();
        if (id == null || !id.startsWith("/users/" + userProvider.getUser().getIdep())) {
            throw new RuntimeException("hack!");
        }
        Result result = marathon.deleteGroup(id, true);
        return result;
    }

    @PostMapping("/app")
    public Result update(@RequestBody UpdateServiceDTO serviceId) throws MarathonException {
        String id = serviceId.getServiceId();
        // if (id == null || id.length() < 1) {
        // id = "XXX_XXX";
        // }
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
        // HttpResponse<JsonNode> response = Unirest.put(MarathonRessource.PATH_MESOS +
        // "/v2/apps" + id)
        // .header("Content-Type", "application/json").body("{\"instances\":" +
        // serviceId.getInstances() + "}")
        // .asJson();
        Result result = marathon.updateApp(id, app, true);
        return result;
    }

    @PutMapping("/app")
    public App publishService(@RequestBody CreateServiceDTO requestDTO)
            throws JsonProcessingException, IOException, MarathonException, Exception {
        String catalogId = "inno";
        if (requestDTO.getCatalogId() != null && requestDTO.getCatalogId().length() > 0) {
            catalogId = requestDTO.getCatalogId();
        }
        UniversePackage pkg = catalogService.getPackage(catalogId, requestDTO.getPackageName());

        Map<String, Object> resource = pkg.getResource();
        Map<String, Object> fusion = new HashMap<>();
        User user = userProvider.getUser();
        userDataService.fetchUserData(user);

        // Apply every admission controller
        long nbInvalidations = admissionControllers.stream()
                .map(admissionController -> admissionController.validateContract(user, pkg, requestDTO.getOptions()))
                .filter(b -> !b).count();
        if (nbInvalidations > 0) {
            throw new AccessDeniedException("Validation failed");
        }
        fusion.putAll((Map<String, Object>) requestDTO.getOptions());
        fusion.putAll(resource);

        String toMarathon = Mustacheur.mustache(pkg.getJsonMustache(), fusion);

        App app = mapper.readValue(toMarathon, App.class);
        Map<String,String> onyxiaOptions = ((Map<String, String>) ((Map<String, Object>) requestDTO.getOptions()).get("onyxia"));
        app.addLabel("ONYXIA_NAME", pkg.getName());
        if (onyxiaOptions != null) {
            app.addLabel("ONYXIA_TITLE",
                    onyxiaOptions
                            .get("friendly_name"));
        }

        app.addLabel("ONYXIA_SUBTITLE", pkg.getName());
        app.addLabel("ONYXIA_SCM", pkg.getScm());
        app.addLabel("ONYXIA_DESCRIPTION", pkg.getDescription());
        app.addLabel("ONYXIA_STATUS", pkg.getStatus());
        if (!app.getLabels().containsKey("ONYXIA_URL") && app.getLabels().containsKey("HAPROXY_0_VHOST")) {
            if (app.getLabels().containsKey("HAPROXY_1_VHOST")) {
                app.addLabel("ONYXIA_URL", "https://" + app.getLabels().get("HAPROXY_0_VHOST") + ",https://"
                        + app.getLabels().get("HAPROXY_1_VHOST"));
            } else {
                app.addLabel("ONYXIA_URL", "https://" + app.getLabels().get("HAPROXY_0_VHOST"));
            }
        }
        app.addLabel("ONYXIA_LOGO",
                (String) ((Map<String, Object>) resource.get("images"))
                        .get("icon-small"));

        if (requestDTO.isDryRun()) {
            return app;
        } else {
            VersionedApp versionedApp = marathon.createApp(app);
            metrics.plusUn();
            return versionedApp;
        }
    }

    @PutMapping("/group")
    public Collection<App> publishGroup(@RequestBody CreateServiceDTO requestDTO)
            throws JsonProcessingException, IOException, MarathonException, Exception {

        String catalogId = "inno";
        if (requestDTO.getCatalogId() != null && requestDTO.getCatalogId().length() > 0) {
            catalogId = requestDTO.getCatalogId();
        }
        UniversePackage pkg = catalogService.getPackage(catalogId, requestDTO.getPackageName());
        Map<String, Object> resource = pkg.getResource();
        Map<String, Object> fusion = new HashMap<>();
        User user = userProvider.getUser();
        userDataService.fetchUserData(user);

        // Apply every admission controller
        long nbInvalidations = admissionControllers.stream()
                .map(admissionController -> admissionController.validateContract(user, pkg, requestDTO.getOptions()))
                .filter(b -> !b).count();
        if (nbInvalidations > 0) {
            throw new AccessDeniedException("Validation failed");
        }

        fusion.putAll((Map<String, Object>) requestDTO.getOptions());
        fusion.putAll(resource);

        String toMarathon = Mustacheur.mustache(pkg.getJsonMustache(), fusion);
        Group group = mapper.readValue(toMarathon, Group.class);
        Collection<App> apps = group.getApps();
        Collection<App> versionedApps = new HashSet<>();
        for (App app : apps) {
            String moduleName = app.getId().substring(app.getId().lastIndexOf('/') + 1);
            String name = pkg.getName() + " : " + moduleName;

            app.addLabel("ONYXIA_NAME", name);
            app.addLabel("ONYXIA_TITLE",
                    ((Map<String, String>) ((Map<String, Object>) requestDTO.getOptions()).get("onyxia"))
                            .get("friendly_name"));
            app.addLabel("ONYXIA_SUBTITLE", moduleName);
            app.addLabel("ONYXIA_SCM", pkg.getScm());
            app.addLabel("ONYXIA_DESCRIPTION", pkg.getDescription());
            app.addLabel("ONYXIA_STATUS", pkg.getStatus());
            if (!app.getLabels().containsKey("ONYXIA_URL") && app.getLabels().containsKey("HAPROXY_0_VHOST")) {
                if (app.getLabels().containsKey("HAPROXY_1_VHOST")) {
                    app.addLabel("ONYXIA_URL", "https://" + app.getLabels().get("HAPROXY_0_VHOST") + ",https://"
                            + app.getLabels().get("HAPROXY_1_VHOST"));
                } else {
                    app.addLabel("ONYXIA_URL", "https://" + app.getLabels().get("HAPROXY_0_VHOST"));
                }
            }
            app.addLabel("ONYXIA_LOGO",
                    (String) ((Map<String, Object>) ((Map<String, Object>) resource.get("resource")).get("images"))
                            .get("icon-small"));

            if (!requestDTO.isDryRun()) {
                VersionedApp versionedApp = marathon.createApp(app);
                versionedApps.add(versionedApp);
            } else {
                versionedApps.add(app);
            }
        }

        return versionedApps;
    }

}
