package fr.insee.onyxia.model;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import fr.insee.onyxia.model.project.Project;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;

@Schema(description = "")
public class OnyxiaUser {

    @JsonUnwrapped
    @Schema(description = "")
    private User user;

    @Schema(description = "")
    private List<Project> projects = new ArrayList<>();

    public OnyxiaUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Project> getProjects() {
        return projects;
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects;
    }
}
