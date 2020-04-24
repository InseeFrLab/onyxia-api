package fr.insee.onyxia.api.services;

import fr.insee.onyxia.model.User;
import fr.insee.onyxia.model.catalog.Package;
import fr.insee.onyxia.model.dto.CreateServiceDTO;
import fr.insee.onyxia.model.dto.ServicesListing;
import fr.insee.onyxia.model.service.Service;
import fr.insee.onyxia.model.service.UninstallService;
import org.springframework.scheduling.annotation.Async;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface AppsService {

        @Async
        CompletableFuture<ServicesListing> getUserServices(User user) throws  IllegalAccessException, IOException;

        @Async
        CompletableFuture<ServicesListing> getUserServices(User user, String groupId)
                throws IllegalAccessException,  IOException;

        Collection<Object> installApp(CreateServiceDTO requestDTO, boolean isGroup, String catalogId, Package pkg,
                        User user, Map<String, Object> fusion) throws Exception;

        Service getUserService(User user, String serviceId) throws Exception;

        UninstallService destroyService(User user, String serviceId) throws Exception;
}
