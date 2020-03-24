package fr.insee.onyxia.model.catalog;

import java.util.List;

public class Universe {

    private List<UniversePackage> packages;

    public List<UniversePackage> getPackages() {
        return packages;
    }

    public void setPackages(List<UniversePackage> packages) {
        this.packages = packages;
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
