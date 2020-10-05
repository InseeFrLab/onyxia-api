package fr.insee.onyxia.model.catalog;

import java.util.List;

public abstract class CatalogWrapper {

    private List<Pkg> packages;

    /**
     * @return the packages
     */
    public List<Pkg> getPackages() {
        return packages;
    }

    /**
     * @param packages the packages to set
     */
    public void setPackages(List<Pkg> packages) {
        this.packages = packages;
    }

    public Pkg getPackageByName(String name) {
        for (Pkg pkg : packages) {
            if (pkg.getName().equals(name)) {
                return pkg;
            }
        }
        return null;
    }
}