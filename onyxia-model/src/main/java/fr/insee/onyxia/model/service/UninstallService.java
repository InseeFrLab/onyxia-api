package fr.insee.onyxia.model.service;

public class UninstallService {

    private String path;


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

    public Boolean getSuccess(){
        return success;
    }

}