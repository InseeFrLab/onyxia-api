package fr.insee.onyxia.api.configuration.helm;

import java.util.ArrayList;
import java.util.List;

public class HelmRepo {
    private List<HelmRepoWrapper> helmRepos = new ArrayList<>();

    public HelmRepo() {

    }

    public HelmRepoWrapper getHelmRepo(String id) {
        for (HelmRepoWrapper helmRepo : helmRepos) {
            if (helmRepo.getId().equals(id)) {
                return helmRepo;
            }
        }
        return null;
    }

    public List<HelmRepoWrapper> getHelmRepos() {
        return helmRepos;
    }

    public void setHelmRepo(List<HelmRepoWrapper> helmRepos) {
        this.helmRepos = helmRepos;
    }
}