package fr.insee.onyxia.api.controller.api.mylab;

import fr.insee.onyxia.api.dao.StorageS3Client;
import fr.insee.onyxia.api.services.TaskService;
import fr.insee.onyxia.api.services.UserProvider;
import fr.insee.onyxia.model.User;
import fr.insee.onyxia.model.mesos.MesosTask;
import fr.insee.onyxia.model.task.ServiceFile;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Tag(name = "My lab", description = "My services")
@RequestMapping("/my-lab/task")
@RestController
public class TaskController {

    @Autowired
    private UserProvider userProvider;

    @Autowired
    private TaskService taskService;

    @Autowired
    private StorageS3Client s3client;

    @Value("${s3.url.stable}")
    private String MINIO_URL;

    @Value("${s3.region}")
    private String MINIO_REGION;

    @GetMapping("/{taskId}")
    public MesosTask getTask(@PathVariable("taskId") String taskId) {
        User user = userProvider.getUser();
        MesosTask task = taskService.getTaskFromMesos(taskId);
        return task;
    }

    @GetMapping("/{taskId}/browse")
    public ServiceFile[] browse(@PathVariable("taskId") String taskId, @RequestParam("path") String path)
            throws IOException {
        return taskService.browseFiles(taskId, path);
    }

    @GetMapping("/{taskId}/transfer")
    public ResponseEntity transferFile(@PathVariable("taskId") String taskId, @RequestParam("path") String path,
            @RequestHeader(value = "S3_ACCESS_TOKEN") String accessToken,
            @RequestHeader(value = "S3_ACCESS_KEY") String accessKey,
            @RequestHeader(value = "S3_SECRET_KEY") String secretKey) throws IOException {
        TaskService.SandboxFile file = taskService.downloadFile(taskId, path);

        User user = userProvider.getUser();
        // final okhttp3.Response resp = CLIENT.newCall(fileRequest).execute();

        s3client.uploadObject(MINIO_REGION, MINIO_URL, accessKey, secretKey, accessToken, user.getIdep(),
                taskId + "/" + path, file.getData(), file.getContentLength());
        return new ResponseEntity(HttpStatus.OK);
    }

    @GetMapping("/{taskId}/download")
    public ResponseEntity downloadFile(@PathVariable("taskId") String taskId, @RequestParam("path") String path)
            throws IOException {
        TaskService.SandboxFile file = taskService.downloadFile(taskId, path);
        InputStreamResource inputStreamResource = new InputStreamResource(file.getData());
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentLength(file.getContentLength());
        return new ResponseEntity(inputStreamResource, responseHeaders, HttpStatus.OK);
    }
}
