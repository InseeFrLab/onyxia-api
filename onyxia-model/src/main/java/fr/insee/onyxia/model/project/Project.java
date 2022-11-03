package fr.insee.onyxia.model.project;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "")

public class Project {
    @Schema(description = "")
    private String id;
    @Schema(description = "If not null, this project belong to this group name.")
    private String group;
    @Schema(description = "If not null, this project have this bucket")
    private String bucket;
    @Schema(description = "If not null, this project have this deployment environment.")
    private String namespace;
    @Schema(description = "This project have this name")
    private String name;
    @Schema(description = "This project have this vault top dir")
    private String vaultTopDir;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
        if (name == null) {
            setName(id);
        }
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVaultTopDir() {
        return vaultTopDir;
    }

    public void setVaultTopDir(String vaultTopDir) {
        this.vaultTopDir = vaultTopDir;
    }
}
