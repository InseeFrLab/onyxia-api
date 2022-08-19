package fr.insee.onyxia.model.catalog;

import java.util.List;
import java.util.Map;
import fr.insee.onyxia.model.helm.Chart;

public abstract class CatalogWrapper {

    private List<Pkg> packages;
    private Map<String, List<Chart>> entries;
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

    /**
     * @return the packages
     */
    public Map<String, List<Chart>> getEntries() {
        return entries;
    }

    /**
     * @param entries the packages to set
     */
    public void setEntries(Map<String, List<Chart>> entries) {
        this.entries = entries;
    }

}