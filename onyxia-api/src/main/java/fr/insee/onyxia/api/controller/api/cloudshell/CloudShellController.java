package fr.insee.onyxia.api.controller.api.cloudshell;

import fr.insee.onyxia.api.controller.api.utils.OrchestratorConfiguration;
import fr.insee.onyxia.api.services.AppsService;
import fr.insee.onyxia.api.services.CatalogService;
import fr.insee.onyxia.api.services.UserProvider;
import fr.insee.onyxia.api.services.impl.MarathonAppsService;
import fr.insee.onyxia.model.catalog.Package;
import fr.insee.onyxia.model.service.Service;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Cloud Shell", description = "Cloud shell")
@RequestMapping("/cloudshell")
@RestController
@SecurityRequirement(name="auth")
public class CloudShellController {

	@Autowired
	private MarathonAppsService marathonAppsService;

	@Autowired
	private AppsService helmAppsService;

	@Autowired
	private UserProvider userProvider;

	@Autowired
	private CatalogService catalogService;

	@Value("${marathon.group.name}")
	private String MARATHON_GROUP_NAME;

	@Autowired
	private OrchestratorConfiguration orchestratorConfiguration;

	@Value("${cloudshell.catalogid}")
	private String catalogId;

	@Value("${cloudshell.packagename}")
	private String packageName;

	@GetMapping
	public CloudShellStatus getCloudShellStatus() {
		CloudShellStatus status = new CloudShellStatus();
		Service.ServiceType preferredServiceType = orchestratorConfiguration.getPreferredServiceType();
		try {
			Service service = null;
			if (preferredServiceType == Service.ServiceType.KUBERNETES) {
				service = helmAppsService.getUserService(userProvider.getUser(),"cloudshell");
			}
			else {
				service = marathonAppsService.getUserService(userProvider.getUser(),"cloudshell");
			}
			status.setStatus(CloudShellStatus.STATUS_UP);
			service.getUrls().stream().findFirst().ifPresent(url -> status.setUrl(url));
		} catch (Exception e) {
			status.setStatus(CloudShellStatus.STATUS_DOWN);
			status.setPackageToDeploy(catalogService.getPackage(catalogId,packageName));
			status.setCatalogId(catalogId);
			status.setUrl(null);
		}

		return status;
	}

	public static class CloudShellStatus {

		public static final String STATUS_UP = "UP", STATUS_LOADING = "LOADING", STATUS_DOWN = "DOWN";
		private String status = STATUS_UP;
		private String url = null;
		private Package packageToDeploy = null;
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

		public Package getPackageToDeploy() {
			return packageToDeploy;
		}

		public void setPackageToDeploy(Package packageToDeploy) {
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
