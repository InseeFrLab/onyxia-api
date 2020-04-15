package fr.insee.onyxia.api.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.onyxia.api.services.AppsService;
import fr.insee.onyxia.model.User;
import mesosphere.marathon.client.Marathon;
import mesosphere.marathon.client.model.v2.App;
import mesosphere.marathon.client.model.v2.GetAppResponse;
import mesosphere.marathon.client.model.v2.Group;
import mesosphere.marathon.client.model.v2.VersionedApp;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Service
@Qualifier("Marathon")
public class MarathonAppsService implements AppsService {


    @Autowired(required = false)
    Marathon marathon;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    @Qualifier("marathon")
    private OkHttpClient marathonClient;

    private @Value("${marathon.url}") String MARATHON_URL;

    public VersionedApp getServiceById(String id) {
        GetAppResponse appsResponse = marathon.getApp(id);
        VersionedApp app = appsResponse.getApp();
        return app;
    }

    public Group getGroups(String id) throws IOException {
        Request requete = new Request.Builder().url(MARATHON_URL+"/v2/groups/users/"
                + id + "?" + "embed=group.groups" + "&" + "embed=group.apps" + "&"
                + "embed=group.apps.tasks" + "&" + "embed=group.apps.counts" + "&" + "embed=group.apps.deployments"
                + "&" + "embed=group.apps.readiness" + "&" + "embed=group.apps.lastTaskFailure" + "&"
                + "embed=group.pods" + "&" + "embed=group.apps.taskStats").build();
        Response response = marathonClient.newCall(requete).execute();
        Group groupResponse = mapper.readValue(response.body().byteStream(), Group.class);
        return groupResponse;
    }

    @Override
    public List<fr.insee.onyxia.model.service.Service> getUserServices(User user) throws InterruptedException, TimeoutException, IOException {
        Group group = getGroups(user.getIdep());
        return group.getApps().stream().map(app -> getServiceFromApp(app)).collect(Collectors.toList());
    }

    private fr.insee.onyxia.model.service.Service getServiceFromApp(App app) {
        fr.insee.onyxia.model.service.Service service = new fr.insee.onyxia.model.service.Service();
        service.setLabels(app.getLabels());
        service.setCpus(app.getCpus());
        service.setInstances(app.getInstances());
        service.setMem(app.getMem());
        service.setTitle(app.getId());
        service.setId(app.getId());
        return service;
    }

}
