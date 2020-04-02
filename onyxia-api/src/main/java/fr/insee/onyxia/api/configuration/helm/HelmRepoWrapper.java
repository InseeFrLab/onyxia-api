package fr.insee.onyxia.api.configuration.helm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import fr.insee.onyxia.model.helm.Repository;

/**
 * HelmRepoWrapper
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class HelmRepoWrapper {
    private Repository repository;
    private String id;

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Repository getRepository() {
        return this.repository;
    }

    public void setRepository(Repository repository) {
        this.repository = repository;
    }

}