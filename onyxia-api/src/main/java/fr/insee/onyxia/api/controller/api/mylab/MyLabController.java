package fr.insee.onyxia.api.controller.api.mylab;

import com.fasterxml.jackson.core.JsonProcessingException;
import fr.insee.onyxia.api.configuration.CatalogWrapper;
import fr.insee.onyxia.api.configuration.properties.RegionsConfiguration;
import fr.insee.onyxia.api.services.AppsService;
import fr.insee.onyxia.api.services.CatalogService;
import fr.insee.onyxia.api.services.UserProvider;
import fr.insee.onyxia.api.services.impl.MarathonAppsService;
import fr.insee.onyxia.model.User;
import fr.insee.onyxia.model.catalog.Package;
import fr.insee.onyxia.model.catalog.Universe;
import fr.insee.onyxia.model.dto.CreateServiceDTO;
import fr.insee.onyxia.model.dto.ServicesListing;
import fr.insee.onyxia.model.dto.UpdateServiceDTO;
import fr.insee.onyxia.model.region.Region;
import fr.insee.onyxia.model.service.Service;
import fr.insee.onyxia.model.service.UninstallService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import mesosphere.marathon.client.Marathon;
import mesosphere.marathon.client.MarathonException;
import mesosphere.marathon.client.model.v2.App;
import mesosphere.marathon.client.model.v2.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Tag(name = "My lab", description = "My services")
@RequestMapping("/my-lab")
@RestController
@SecurityRequirement(name = "auth")
public class MyLabController {
    @Autowired
    private AppsService helmAppsService;

    @Autowired
    private MarathonAppsService marathonAppsService;

    @Autowired
    private UserProvider userProvider;

    @Autowired
    private CatalogService catalogService;

    @Autowired(required = false)
    private Marathon marathon;

    @Autowired
    private RegionsConfiguration regionsConfiguration;

    private final Logger logger = LoggerFactory.getLogger(MyLabController.class);

    @GetMapping("/services")
    public ServicesListing getMyServices(@RequestParam(required = false) String groupId, Region region) throws Exception {
        User user = userProvider.getUser();
        ServicesListing dto = new ServicesListing();
        List<CompletableFuture<ServicesListing>> futures = new ArrayList<>();
        if (region.getType().equals(Service.ServiceType.MARATHON)) {
            futures.add(marathonAppsService.getUserServices(region,user,groupId));
        }
        if (region.getType().equals(Service.ServiceType.KUBERNETES)) {
            futures.add(helmAppsService.getUserServices(region,user,groupId));
        }
        for (var future : futures) {
            ServicesListing listing = future.get();
            dto.getApps().addAll(listing.getApps());
            dto.getGroups().addAll(listing.getGroups());
        }
        return dto;
    }

    @GetMapping("/app")
    public @ResponseBody Service getApp(@RequestParam("serviceId") String serviceId,
           Region region) throws Exception {
        if (Service.ServiceType.MARATHON.equals(region.getType())) {
            return marathonAppsService.getUserService(region, userProvider.getUser(), serviceId);
        } else if (Service.ServiceType.KUBERNETES.equals(region.getType())) {
            return helmAppsService.getUserService(region,userProvider.getUser(), serviceId);
        }
        return null;
    }

    @GetMapping("/app/logs")
    public @ResponseBody String getLogs(@RequestParam("serviceId") String serviceId,
                                        @RequestParam("taskId") String taskId,
                                       Region region) throws Exception {
        if (Service.ServiceType.MARATHON.equals(region.getType())) {
            return marathonAppsService.getLogs(region, userProvider.getUser(),serviceId, taskId);
        } else if (Service.ServiceType.KUBERNETES.equals(region.getType())) {
            return helmAppsService.getLogs(region, userProvider.getUser(),serviceId, taskId);
        }
        return null;
    }

    @DeleteMapping("/app")
    public UninstallService destroyApp(@RequestParam("serviceId") String serviceId,
           Region region) throws Exception {
        if (Service.ServiceType.MARATHON.equals(region.getType())) {
            return marathonAppsService.destroyService(region, userProvider.getUser(), serviceId);

        } else if (Service.ServiceType.KUBERNETES.equals(region.getType())) {
            return helmAppsService.destroyService(region, userProvider.getUser(), serviceId);
        }
        return null;
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
    public Object publishService(@RequestBody CreateServiceDTO requestDTO, Region region)
            throws JsonProcessingException, IOException, MarathonException, Exception {
        return publishApps(region, requestDTO, false).stream().findFirst().get();
    }

    @PutMapping("/group")
    public Collection<Object> publishGroup(@RequestBody CreateServiceDTO requestDTO, Region region)
            throws JsonProcessingException, IOException, MarathonException, Exception {
        return publishApps(region, requestDTO, true);
    }

    private Collection<Object> publishApps(Region region, CreateServiceDTO requestDTO, boolean isGroup)
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
            return marathonAppsService.installApp(region,requestDTO, isGroup, catalogId, pkg, user, fusion);
        } else {
            return helmAppsService.installApp(region,requestDTO, isGroup, catalogId, pkg, user, fusion);
        }
    }



}
