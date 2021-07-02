package fr.insee.onyxia.model.project;

public class Project {

    private String id;
    private String group;
    private String bucket;
    private String namespace;
    private String name;

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
}
