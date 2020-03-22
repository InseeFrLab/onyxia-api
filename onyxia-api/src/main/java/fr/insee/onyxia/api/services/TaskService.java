package fr.insee.onyxia.api.services;

import fr.insee.onyxia.model.mesos.MesosTask;
import fr.insee.onyxia.model.task.ServiceFile;

import java.io.IOException;
import java.io.InputStream;

public interface TaskService {

    public MesosTask getTaskFromMesos(String taskId);

    public ServiceFile[] browseFiles(String taskId, String path) throws IOException;

    public SandboxFile downloadFile(String taskId, String path) throws IOException;

    public static class SandboxFile {
        private InputStream data;
        private long contentLength;

        public InputStream getData() {
            return data;
        }

        public void setData(InputStream data) {
            this.data = data;
        }

        public long getContentLength() {
            return contentLength;
        }

        public void setContentLength(long contentLength) {
            this.contentLength = contentLength;
        }
    }

}
