package fr.insee.onyxia.api.controller.api.mylab;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.onyxia.api.configuration.CatalogWrapper;
import fr.insee.onyxia.api.configuration.Catalogs;
import fr.insee.onyxia.api.configuration.metrics.CustomMetrics;
import fr.insee.onyxia.api.services.CatalogService;
import fr.insee.onyxia.api.services.UserProvider;
import fr.insee.onyxia.api.services.control.AdmissionController;
import fr.insee.onyxia.api.services.control.marathon.UrlGenerator;
import fr.insee.onyxia.api.services.control.utils.IDSanitizer;
import fr.insee.onyxia.api.services.control.utils.PublishContext;
import fr.insee.onyxia.model.User;
import fr.insee.onyxia.model.catalog.Package;
import fr.insee.onyxia.model.catalog.Universe;
import fr.insee.onyxia.model.catalog.UniversePackage;
import fr.insee.onyxia.model.dto.CreateServiceDTO;
import fr.insee.onyxia.model.dto.UpdateServiceDTO;
import fr.insee.onyxia.mustache.Mustacheur;
import io.github.inseefrlab.helmwrapper.model.HelmInstaller;
import io.github.inseefrlab.helmwrapper.service.HelmInstallService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import mesosphere.marathon.client.Marathon;
import mesosphere.marathon.client.MarathonException;
import mesosphere.marathon.client.model.v2.App;
import mesosphere.marathon.client.model.v2.Group;
import mesosphere.marathon.client.model.v2.Result;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "My lab", description = "My services")
@RequestMapping("/my-lab")
@RestController
@SecurityRequirement(name="auth")
public class MyLabController {

    /**
     * @deprecated : should be moved to the marathon http client
     */
    @Deprecated
    @Value("${marathon.url}")
    private String MARATHON_URL;

    @Value("${marathon.dns.suffix}")
    private String MARATHON_DNS_SUFFIX;

    @Autowired
    private IDSanitizer idSanitizer;

    @Autowired
    HelmInstallService helm;

    @Autowired
    @Qualifier("helm")
    ObjectMapper mapperHelm;

    @Autowired
    @Qualifier("marathon")
    private OkHttpClient marathonClient;

    @Autowired
    private CustomMetrics metrics;

    @Autowired
    private UserProvider userProvider;

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

    @Autowired
    private UrlGenerator generator;

    private final Logger logger = LoggerFactory.getLogger(MyLabController.class);


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
    public Object publishService(@RequestBody CreateServiceDTO requestDTO)
            throws JsonProcessingException, IOException, MarathonException, Exception {
        return publishApps(requestDTO, false).stream().findFirst().get();
    }

    @PutMapping("/group")
    public Collection<Object> publishGroup(@RequestBody CreateServiceDTO requestDTO)
            throws JsonProcessingException, IOException, MarathonException, Exception {
        return publishApps(requestDTO, true);
    }

    private Collection<Object> publishApps(CreateServiceDTO requestDTO, boolean isGroup)
            throws JsonProcessingException, IOException, MarathonException, Exception {
        String catalogId = "internal";
        if (requestDTO.getCatalogId() != null && requestDTO.getCatalogId().length() > 0) {
            catalogId = requestDTO.getCatalogId();
        }
        CatalogWrapper catalog = catalogService.getCatalogById(catalogId);
        Package pkg = catalog.getCatalog().getPackageByName(requestDTO.getPackageName());
        PublishContext context = new PublishContext(catalogId);

        User user = userProvider.getUser();
        Map<String, Object> fusion = new HashMap<>();
        fusion.putAll((Map<String, Object>) requestDTO.getOptions());
        if (!Universe.TYPE_UNIVERSE.equals(catalog.getType())) {
            File values = File.createTempFile("values", ".yaml");
            mapperHelm.writeValue(values,fusion);
            logger.info(Files.readString(values.toPath()));
            HelmInstaller res = helm.installChart(pkg.getName(),requestDTO.getCatalogId()+"/"+pkg.getName(),values,user.getIdep(), requestDTO.isDryRun());
            values.delete();
            return List.of(res.getManifest());
        }
        UniversePackage universePkg = (UniversePackage) pkg;
        Map<String, Object> resource = universePkg.getResource();
        fusion.putAll(Map.of("resource", resource));

        Map<String, String> contextData = new HashMap<>();
        contextData.put("internaldns",idSanitizer.sanitize(pkg.getName())+"-"+context.getRandomizedId()+"-"+idSanitizer.sanitize(user.getIdep())+"-"+idSanitizer.sanitize(MARATHON_GROUP_NAME)+"."+MARATHON_DNS_SUFFIX);

        for (int i = 0; i < 10; i++) {
            contextData.put("externaldns-"+i,generator.generateUrl(user.getIdep(), pkg.getName(), context.getRandomizedId(), i));
        }
        fusion.put("context",contextData);

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

}
