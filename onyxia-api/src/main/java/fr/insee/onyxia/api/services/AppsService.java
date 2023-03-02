package fr.insee.onyxia.api.services;

import fr.insee.onyxia.model.User;
import fr.insee.onyxia.model.catalog.Pkg;
import fr.insee.onyxia.model.dto.CreateServiceDTO;
import fr.insee.onyxia.model.dto.ServicesListing;
import fr.insee.onyxia.model.project.Project;
import fr.insee.onyxia.model.region.Region;
import fr.insee.onyxia.model.service.Service;
import fr.insee.onyxia.model.service.UninstallService;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.springframework.scheduling.annotation.Async;

public interface AppsService {

    @Async
    CompletableFuture<ServicesListing> getUserServices(Region region, Project project, User user)
            throws IllegalAccessException, IOException;

    @Async
    CompletableFuture<ServicesListing> getUserServices(
            Region region, Project project, User user, String groupId)
            throws IllegalAccessException, IOException;

    InstallDTO installApp(
            Region region,
            Project project,
            CreateServiceDTO requestDTO,
            String catalogId,
            Pkg pkg,
            User user,
            Map<String, Object> fusion)
            throws Exception;

    Service getUserService(Region region, Project project, User user, String serviceId)
            throws Exception;

    UninstallService destroyService(
            Region region, Project project, User user, String path, boolean bulk) throws Exception;

    String getLogs(Region region, Project project, User user, String serviceId, String taskId);
}
