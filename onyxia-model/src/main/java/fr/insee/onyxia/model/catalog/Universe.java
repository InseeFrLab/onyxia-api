package fr.insee.onyxia.model.catalog;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Universe extends CatalogWrapper {

    public static final String TYPE_UNIVERSE = "universe";

    @JsonProperty("packages")
    public void readPackages(List<UniversePackage> packages) {
        // We can't directly use List<UniversePackage> as List<Package>.
        // That's beacause even if UniversePackage extends Package, List<UniversePackage>  does not extends List<Package>
        this.setPackages(packages.stream().map(pkg -> (Pkg) pkg).collect(Collectors.toList()));
    }

}
