package fr.insee.onyxia.api.services;

import fr.insee.onyxia.model.User;
import fr.insee.onyxia.model.catalog.Package;
import fr.insee.onyxia.model.dto.CreateServiceDTO;
import fr.insee.onyxia.model.dto.ServicesListing;
import fr.insee.onyxia.model.region.Region;
import fr.insee.onyxia.model.service.Service;
import fr.insee.onyxia.model.service.UninstallService;
import org.springframework.scheduling.annotation.Async;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface AppsService {

        @Async
        CompletableFuture<ServicesListing> getUserServices(Region region, User user) throws  IllegalAccessException, IOException;

        @Async
        CompletableFuture<ServicesListing> getUserServices(Region region,User user, String groupId)
                throws IllegalAccessException,  IOException;

        Collection<Object> installApp(Region region,CreateServiceDTO requestDTO, String catalogId, Package pkg,
                        User user, Map<String, Object> fusion) throws Exception;

        Service getUserService(Region region,User user, String serviceId) throws Exception;

        UninstallService destroyService(Region region,User user, String serviceId) throws Exception;

        String getLogs(Region region,User user, String serviceId, String taskId);
}
