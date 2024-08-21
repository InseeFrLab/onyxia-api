package fr.insee.onyxia.api.services;

import fr.insee.onyxia.model.User;
import fr.insee.onyxia.model.catalog.Pkg;
import fr.insee.onyxia.model.dto.CreateServiceDTO;
import fr.insee.onyxia.model.dto.ServicesListing;
import fr.insee.onyxia.model.project.Project;
import fr.insee.onyxia.model.region.Region;
import fr.insee.onyxia.model.service.Service;
import fr.insee.onyxia.model.service.UninstallService;
import io.fabric8.kubernetes.api.model.Event;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.github.inseefrlab.helmwrapper.service.HelmInstallService;
import java.io.IOException;
import java.text.ParseException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import org.springframework.scheduling.annotation.Async;

public interface AppsService {

    @Async
    CompletableFuture<ServicesListing> getUserServices(Region region, Project project, User user)
            throws IllegalAccessException, IOException;

    @Async
    CompletableFuture<ServicesListing> getUserServices(
            Region region, Project project, User user, String groupId)
            throws IllegalAccessException, IOException;

    Collection<Object> installApp(
            Region region,
            Project project,
            CreateServiceDTO requestDTO,
            String catalogId,
            Pkg pkg,
            User user,
            Map<String, Object> fusion,
            final boolean skipTlsVerify,
            final String caFile)
            throws Exception;

    Service getUserService(Region region, Project project, User user, String serviceId)
            throws Exception;

    UninstallService destroyService(
            Region region, Project project, User user, String path, boolean bulk) throws Exception;

    String getLogs(
            Region region,
            Project project,
            User user,
            String serviceId,
            String taskId,
            String containerId);

    Watch getEvents(Region region, Project project, User user, Watcher<Event> watcher)
            throws HelmInstallService.MultipleServiceFound, ParseException;

    void rename(Region region, Project project, User user, String serviceId, String friendlyName)
            throws IOException, InterruptedException, TimeoutException;

    void share(Region region, Project project, User user, String serviceId, boolean share)
            throws IOException, InterruptedException, TimeoutException;

    void resume(
            Region region,
            Project project,
            String catalogId,
            String chartName,
            String version,
            User user,
            String serviceId,
            boolean skipTlsVerify,
            String caFile,
            boolean dryRun)
            throws IOException, InterruptedException, TimeoutException;

    void suspend(
            Region region,
            Project project,
            String catalogId,
            String chartName,
            String version,
            User user,
            String serviceId,
            boolean skipTlsVerify,
            String caFile,
            boolean dryRun)
            throws IOException, InterruptedException, TimeoutException;
}
