package fr.insee.onyxia.model.service;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.stereotype.Component;

@Schema(description = "")
@Component
public class UninstallService {

    @Schema(description = "")
    private String path;

    @Schema(description = "")
    private Boolean success;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * @param success the success to set
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Boolean getSuccess() {
        return success;
    }
}
