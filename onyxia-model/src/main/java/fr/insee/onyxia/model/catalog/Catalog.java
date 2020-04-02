package fr.insee.onyxia.model.catalog;

import java.util.List;

public abstract class Catalog {

    private List<Package> packages;

    /**
     * @return the packages
     */
    public List<Package> getPackages() {
        return packages;
    }

    /**
     * @param packages the packages to set
     */
    public void setPackages(List<Package> packages) {
        this.packages = packages;
    }

    public Package getPackageByName(String name) {
        for (Package pkg : packages) {
            if (pkg.getName().equals(name)) {
                return pkg;
            }
        }
        return null;
    }
}