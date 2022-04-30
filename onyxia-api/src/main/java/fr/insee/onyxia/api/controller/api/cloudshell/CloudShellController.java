package fr.insee.onyxia.api.controller.api.cloudshell;

import fr.insee.onyxia.api.services.AppsService;
import fr.insee.onyxia.api.services.CatalogService;
import fr.insee.onyxia.api.services.UserProvider;
import fr.insee.onyxia.model.catalog.Pkg;
import fr.insee.onyxia.model.project.Project;
import fr.insee.onyxia.model.region.Region;
import fr.insee.onyxia.model.service.Service;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Cloud Shell", description = "Cloud shell")
@RequestMapping(value={"/api/cloudshell", "/cloudshell"})
@RestController
@SecurityRequirement(name="auth")
public class CloudShellController {

	@Autowired
	private AppsService helmAppsService;

	@Autowired
	private UserProvider userProvider;

	@Autowired
	private CatalogService catalogService;

	@GetMapping
	public CloudShellStatus getCloudShellStatus(Region region, Project project) {
		CloudShellStatus status = new CloudShellStatus();
		Region.CloudshellConfiguration cloudshellConfiguration = region.getServices().getCloudshell();
		try {
			if (region.getServices().getType().equals(Service.ServiceType.KUBERNETES)) {
				Service service = helmAppsService.getUserService(region, project, userProvider.getUser(region),
						cloudshellConfiguration.getPackageName() + "*");
				status.setStatus(CloudShellStatus.STATUS_UP);
				service.getUrls().stream().findFirst().ifPresent(status::setUrl);
			} else {
				throw new NotImplementedException("Cloudshell is only supported for Kubernetes service provider");
			}
		} catch (Exception e) {
			status.setStatus(CloudShellStatus.STATUS_DOWN);
			status.setPackageToDeploy(catalogService.getPackage(cloudshellConfiguration.getCatalogId(),cloudshellConfiguration.getPackageName()));
			status.setCatalogId(cloudshellConfiguration.getCatalogId());
			status.setUrl(null);
		}

		return status;
	}

	public static class CloudShellStatus {

		public static final String STATUS_UP = "UP", STATUS_LOADING = "LOADING", STATUS_DOWN = "DOWN";
		private String status = STATUS_UP;
		private String url = null;
		private Pkg packageToDeploy = null;
		private String catalogId;

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public Pkg getPackageToDeploy() {
			return packageToDeploy;
		}

		public void setPackageToDeploy(Pkg packageToDeploy) {
			this.packageToDeploy = packageToDeploy;
		}

		public String getCatalogId() {
			return catalogId;
		}

		public void setCatalogId(String catalogId) {
			this.catalogId = catalogId;
		}
	}
}
