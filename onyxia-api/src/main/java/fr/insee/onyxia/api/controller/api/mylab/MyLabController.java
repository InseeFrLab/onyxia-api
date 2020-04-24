package fr.insee.onyxia.api.controller.api.mylab;

import com.fasterxml.jackson.core.JsonProcessingException;
import fr.insee.onyxia.api.configuration.CatalogWrapper;
import fr.insee.onyxia.api.services.AppsService;
import fr.insee.onyxia.api.services.CatalogService;
import fr.insee.onyxia.api.services.UserProvider;
import fr.insee.onyxia.api.services.control.AdmissionController;
import fr.insee.onyxia.api.services.impl.MarathonAppsService;
import fr.insee.onyxia.model.User;
import fr.insee.onyxia.model.catalog.Package;
import fr.insee.onyxia.model.catalog.Universe;
import fr.insee.onyxia.model.dto.CreateServiceDTO;
import fr.insee.onyxia.model.dto.ServicesDTO;
import fr.insee.onyxia.model.dto.UpdateServiceDTO;
import fr.insee.onyxia.model.service.Service;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import mesosphere.marathon.client.Marathon;
import mesosphere.marathon.client.MarathonException;
import mesosphere.marathon.client.model.v2.App;
import mesosphere.marathon.client.model.v2.Result;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Tag(name = "My lab", description = "My services")
@RequestMapping("/my-lab")
@RestController
@SecurityRequirement(name = "auth")
public class MyLabController {

    /**
     * @deprecated : should be moved to the marathon http client
     */
    @Deprecated
    @Value("${marathon.url}")
    private String MARATHON_URL;

    @Value("${kubernetes.enabled}")
    private boolean KUB_ENABLED;

    @Value("${marathon.enabled}")
    private boolean MARATHON_ENABLED;

    @Autowired
    @Qualifier("marathon")
    private OkHttpClient marathonClient;

    @Autowired
    private AppsService helmAppsService;

    @Autowired
    private MarathonAppsService marathonAppsService;

    @Autowired
    private UserProvider userProvider;

    @Autowired
    private CatalogService catalogService;

    @Autowired
    private List<AdmissionController> admissionControllers;

    @Autowired(required = false)
    private Marathon marathon;

    private final Logger logger = LoggerFactory.getLogger(MyLabController.class);

    @GetMapping("/services")
    public ServicesDTO getMyServices() throws Exception {
        User user = userProvider.getUser();
        ServicesDTO dto = new ServicesDTO();
        List<CompletableFuture<List<Service>>> futures = new ArrayList<>();
        if (MARATHON_ENABLED) {
            futures.add(marathonAppsService.getUserServices(user));
        }
        if (KUB_ENABLED) {
            futures.add(helmAppsService.getUserServices(user));
        }
        for (var future : futures) {
            dto.getApps().addAll(future.get());
        }
        return dto;
    }

    @GetMapping("/app")
    public @ResponseBody Service getApp(@RequestParam("serviceId") String id, @RequestParam(required = false) Service.ServiceType type)
            throws Exception {
        if (type == null) {
            type = determineServiceType(id);
        }
        if (Service.ServiceType.MARATHON.equals(type)) {
            return marathonAppsService.getUserService(userProvider.getUser(),id);
        } else if (Service.ServiceType.KUBERNETES.equals(type)) {
            return helmAppsService.getUserService(userProvider.getUser(), id);
        }
        return null;
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
        User user = userProvider.getUser();
        Map<String, Object> fusion = new HashMap<>();
        fusion.putAll((Map<String, Object>) requestDTO.getOptions());
        if (Universe.TYPE_UNIVERSE.equals(catalog.getType())) {
            return marathonAppsService.installApp(requestDTO, isGroup, catalogId, pkg, user, fusion);
        } else {
            return helmAppsService.installApp(requestDTO, isGroup, catalogId, pkg, user, fusion);
        }
    }

    private Service.ServiceType determineServiceType(String id) {
        if (MARATHON_ENABLED && !KUB_ENABLED) {
            return Service.ServiceType.MARATHON;
        }

        if (!MARATHON_ENABLED && KUB_ENABLED) {
            return Service.ServiceType.KUBERNETES;
        }

        return Service.ServiceType.MARATHON;
    }

}
