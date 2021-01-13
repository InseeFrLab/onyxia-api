package fr.insee.onyxia.api.controller.api.mylab;

import com.fasterxml.jackson.core.JsonProcessingException;
import fr.insee.onyxia.api.configuration.CatalogWrapper;
import fr.insee.onyxia.api.configuration.properties.RegionsConfiguration;
import fr.insee.onyxia.api.services.AppsService;
import fr.insee.onyxia.api.services.CatalogService;
import fr.insee.onyxia.api.services.UserProvider;
import fr.insee.onyxia.model.User;
import fr.insee.onyxia.model.catalog.Pkg;
import fr.insee.onyxia.model.catalog.Universe;
import fr.insee.onyxia.model.dto.CreateServiceDTO;
import fr.insee.onyxia.model.dto.ServicesListing;
import fr.insee.onyxia.model.region.Region;
import fr.insee.onyxia.model.service.Service;
import fr.insee.onyxia.model.service.UninstallService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import mesosphere.marathon.client.MarathonException;
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
    private UserProvider userProvider;

    @Autowired
    private CatalogService catalogService;

    @Autowired
    private RegionsConfiguration regionsConfiguration;

    private final Logger logger = LoggerFactory.getLogger(MyLabController.class);

    @GetMapping("/services")
    public ServicesListing getMyServices(@Parameter(hidden = true) Region region, @RequestParam(required = false) String groupId) throws Exception {
        User user = userProvider.getUser();
        ServicesListing dto = new ServicesListing();
        List<CompletableFuture<ServicesListing>> futures = new ArrayList<>();
        if (region.getServices().getType().equals(Service.ServiceType.KUBERNETES)) {
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
    public @ResponseBody Service getApp(@Parameter(hidden = true) Region region,@RequestParam("serviceId") String serviceId) throws Exception {
        if (Service.ServiceType.KUBERNETES.equals(region.getServices().getType())) {
            return helmAppsService.getUserService(region,userProvider.getUser(), serviceId);
        }
        return null;
    }

    @GetMapping("/app/logs")
    public @ResponseBody String getLogs( @Parameter(hidden = true) Region region, @RequestParam("serviceId") String serviceId,
                                        @RequestParam("taskId") String taskId) throws Exception {
        if (Service.ServiceType.KUBERNETES.equals(region.getServices().getType())) {
            return helmAppsService.getLogs(region, userProvider.getUser(),serviceId, taskId);
        }
        return null;
    }

    @DeleteMapping("/app")
    public UninstallService destroyApp(@Parameter(hidden = true) Region region,@RequestParam(value = "path", required = false) String path,@RequestParam(value = "bulk", required = false) boolean bulk) throws Exception {
        if (Service.ServiceType.KUBERNETES.equals(region.getServices().getType())) {
            return helmAppsService.destroyService(region, userProvider.getUser(), path, bulk);
        }
        return null;
    }

    @PutMapping("/app")
    public Object publishService(@Parameter(hidden = true) Region region,@RequestBody CreateServiceDTO requestDTO)
            throws JsonProcessingException, IOException, MarathonException, Exception {
        return publishApps(region, requestDTO);
    }

    private Collection<Object> publishApps(Region region, CreateServiceDTO requestDTO)
            throws JsonProcessingException, IOException, MarathonException, Exception {
        String catalogId = "internal";
        if (requestDTO.getCatalogId() != null && requestDTO.getCatalogId().length() > 0) {
            catalogId = requestDTO.getCatalogId();
        }
        CatalogWrapper catalog = catalogService.getCatalogById(catalogId);
        Pkg pkg = catalog.getCatalog().getPackageByName(requestDTO.getPackageName());
        User user = userProvider.getUser();
        Map<String, Object> fusion = new HashMap<>();
        fusion.putAll((Map<String, Object>) requestDTO.getOptions());
        return helmAppsService.installApp(region,requestDTO, catalogId, pkg, user, fusion);
    }



}
