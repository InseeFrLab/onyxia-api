package fr.insee.onyxia.model.catalog;

import java.util.List;
import java.util.Map;

public class Universe {

    private List<UniversePackage> packages;
    String version;
    private Map<String, Map<String, List>> typeOfFile;

    public List<UniversePackage> getPackages() {
        return packages;
    }

    public void setPackages(List<UniversePackage> packages) {
        this.packages = packages;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Map<String, Map<String, List>> getTypeOfFile() {
        return typeOfFile;
    }

    public void setTypeOfFile(Map<String, Map<String, List>> typeOfFile) {
        this.typeOfFile = typeOfFile;
    }

    public UniversePackage getPackageByName(String name) {
        for (UniversePackage pkg : packages) {
            if (pkg.getName().equals(name)) {
                return pkg;
            }
        }
        return null;
    }
}
