package fr.insee.onyxia.model.catalog;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Universe extends Catalog {

    @JsonProperty("packages")
    public void readPackages(List<Package> packages) {
        this.setPackages(packages);
    }

}
