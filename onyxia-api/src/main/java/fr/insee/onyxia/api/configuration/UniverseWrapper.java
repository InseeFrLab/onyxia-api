package fr.insee.onyxia.api.configuration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.onyxia.model.catalog.Universe;
import fr.insee.onyxia.model.catalog.UniversePackage;
import fr.insee.onyxia.model.catalog.UniversePackage.Config.Category;
import fr.insee.onyxia.model.catalog.UniversePackage.Config.Category.Property;
import fr.insee.onyxia.model.catalog.UniverseStatus;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UniverseWrapper {

    private Universe universe;
    private String id;
    private String name;
    private String description;
    private String maintainer;
    private String location;
    private UniverseStatus status;
    private long lastUpdateTime;
    private String scm;

    public Universe getUniverse() {
        return universe;
    }

    public void setUniverse(Universe universe) {
        this.universe = universe;
    }

    public UniverseStatus getStatus() {
        return status;
    }

    public void setStatus(UniverseStatus status) {
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMaintainer() {
        return maintainer;
    }

    public void setMaintainer(String maintainer) {
        this.maintainer = maintainer;
    }

    public String getScm() {
        return scm;
    }

    public void setScm(String scm) {
        this.scm = scm;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

}
