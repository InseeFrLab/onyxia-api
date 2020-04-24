package fr.insee.onyxia.model.service;

public class UninstallService {

    private String id;

    private String version;

    private Boolean success;

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @param version the version to set
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * @param success the success to set
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Boolean getSuccess(){
        return success;
    }

}