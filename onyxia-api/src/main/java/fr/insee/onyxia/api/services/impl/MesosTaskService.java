package fr.insee.onyxia.api.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.onyxia.api.services.TaskService;
import fr.insee.onyxia.model.deprecated.HttpUtils;
import fr.insee.onyxia.model.mesos.MesosSlave;
import fr.insee.onyxia.model.mesos.MesosSlaves;
import fr.insee.onyxia.model.mesos.MesosTask;
import fr.insee.onyxia.model.mesos.MesosTasks;
import fr.insee.onyxia.model.task.ServiceFile;
import okhttp3.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class MesosTaskService implements TaskService {

    @Autowired
    private ObjectMapper mapper;

    @Value("${mesos.url}")
    private String mesosUrl;

    @Override
    public MesosTask getTaskFromMesos(String taskId) {
        MesosTask task = null;
        try {
            Request request = new Request.Builder().url(mesosUrl + "/tasks?task_id=" + taskId).build();
            okhttp3.Response response_mesos = HttpUtils.CLIENT.newCall(request).execute();
            MesosTasks tasks = mapper.readValue(response_mesos.body().byteStream(), MesosTasks.class);
            if (tasks.getTasks().size() > 0) {
                task = tasks.getTasks().get(0);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return task;
    }

    @Override
    public ServiceFile[] browseFiles(String taskId, String path) throws IOException {
        String hostname;
        String port;
        String containerId;
        String slaveId;
        String frameworkId;

        MesosTask task = getTaskFromMesos(taskId);
        if (task != null) {
            frameworkId = task.getFramework_id();
            containerId = task.getStatuses().get(0).getContainer_status().getContainer_id().getValue();
            slaveId = task.getSlave_id();
            MesosSlave slave = getSlave(slaveId);
            if (slave != null) {
                hostname = slave.getHostname();
                port = slave.getPort();
                if (path == null) {
                    path = "";
                }
                String url = "http://"
                        + hostname + ":" + port + "/files/browse?path=%2Fvar%2Flib%2Fmesos%2Fslave%2Fslaves%2F"
                        + slaveId + "%2Fframeworks%2F" + frameworkId + "%2Fexecutors%2F" + taskId + "%2Fruns%2F"
                        + containerId + "%2F" + path;
                Request taskRequest =
                        new Request.Builder()
                                .url(url)
                                .build();
                okhttp3.Response response = HttpUtils.CLIENT.newCall(taskRequest).execute();
                return mapper.readValue(response.body().charStream(),ServiceFile[].class);
            }
            else {
                return null;
            }
        }
        else {
            return null;
        }
    }

    @Override
    public SandboxFile downloadFile(String taskId, String path) throws IOException {
        String hostname;
        String port;
        String containerId;
        String slaveId;
        String frameworkId;
        MesosTask task = getTaskFromMesos(taskId);
        if (task != null) {
            frameworkId = task.getFramework_id();
            containerId = task.getStatuses().get(0).getContainer_status().getContainer_id().getValue();
            slaveId = task.getSlave_id();
            MesosSlave slave = getSlave(slaveId);
            if (slave != null) {
                hostname = slave.getHostname();
                port = slave.getPort();
                if (path == null) {
                    path = "";
                }


                String url = "http://"
                        + hostname + ":" + port + "/files/download?path=%2Fvar%2Flib%2Fmesos%2Fslave%2Fslaves%2F"
                        + slaveId + "%2Fframeworks%2F" + frameworkId + "%2Fexecutors%2F" + taskId + "%2Fruns%2F"
                        + containerId + "%2F" + path;
                Request fileRequest =
                        new Request.Builder()
                                .url(url)
                                .build();
                final okhttp3.Response resp = HttpUtils.CLIENT.newCall(fileRequest).execute();
                long contentLength = Long.parseLong(resp.header("content-length"));
                SandboxFile file = new SandboxFile();
                file.setData(resp.body().byteStream());
                file.setContentLength(contentLength);
                return file;
            }
            else {
                return null;
            }
        }
        else {
            return null;
        }
    }

    private MesosSlave getSlave(String slaveId) throws IOException {
        MesosSlave slave = null;
        Request request = new Request.Builder().url(mesosUrl + "/slaves?slave_id=" + slaveId).build();
        okhttp3.Response slave_mesos = HttpUtils.CLIENT.newCall(request).execute();
        MesosSlaves slaves = mapper.readValue(slave_mesos.body().byteStream(), MesosSlaves.class);
        if (slaves.getSlaves().size() > 0) {
            slave = slaves.getSlaves().get(0);
        }
        return slave;
    }
}
