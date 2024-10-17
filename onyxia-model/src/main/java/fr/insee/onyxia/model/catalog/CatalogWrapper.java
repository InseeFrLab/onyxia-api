package fr.insee.onyxia.model.catalog;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import fr.insee.onyxia.model.helm.Chart;
import fr.insee.onyxia.model.views.Views;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Schema(description = "A set of packages and charts coming from a same endpoint")
public abstract class CatalogWrapper {

    @Schema(description = "This entries are those from a standard helm repository")
    @JsonView(Views.Full.class)
    private Map<String, List<Chart>> entries = Map.of();

    public Optional<Chart> getPackageByName(String name) {
        if (entries.containsKey(name)) {
            return entries.get(name).stream().findFirst();
        } else {
            return Optional.empty();
        }
    }

    public Optional<Chart> getPackageByNameAndVersion(String name, String version) {
        if (!entries.containsKey(name)) {
            return Optional.empty();
        }
        return entries.get(name).stream()
                .filter(p -> version.equalsIgnoreCase(p.getVersion()))
                .findFirst();
    }

    /**
     * @return the packages
     */
    @JsonView(Views.Full.class)
    public Map<String, List<Chart>> getEntries() {
        return entries;
    }

    @JsonProperty("latestPackages")
    public Map<String, Chart> getLatestPackages() {
        if (entries == null || entries.isEmpty()) {
            return null;
        }
        final Map<String, Chart> latestCharts = new HashMap<>();
        entries.forEach(
                (key, value) -> {
                    if (value != null && !value.isEmpty()) {
                        latestCharts.put(key, value.get(0));
                    }
                });
        return latestCharts;
    }

    /**
     * @param entries the packages to set
     */
    public void setEntries(Map<String, List<Chart>> entries) {
        this.entries = entries;
    }
}
