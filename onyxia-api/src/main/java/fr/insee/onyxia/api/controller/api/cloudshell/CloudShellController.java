package fr.insee.onyxia.api.controller.api.cloudshell;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.insee.onyxia.api.services.CatalogService;
import fr.insee.onyxia.api.services.UserProvider;
import fr.insee.onyxia.api.services.impl.MarathonAppsService;
import fr.insee.onyxia.model.catalog.UniversePackage;
import io.swagger.v3.oas.annotations.tags.Tag;
import mesosphere.marathon.client.MarathonException;
import mesosphere.marathon.client.model.v2.VersionedApp;

@Tag(name = "Cloud Shell", description = "Cloud shell")
@RequestMapping("/cloudshell")
@RestController
public class CloudShellController {

	@Autowired
	private MarathonAppsService marathonAppsService;

	@Autowired
	private UserProvider userProvider;

	@Autowired
	private CatalogService catalogService;

	@Value("${marathon.group.name}")
	private String MARATHON_GROUP_NAME;

	@GetMapping
	public CloudShellStatus getCloudShellStatus() {
		CloudShellStatus status = new CloudShellStatus();
		VersionedApp app;
		status.setPackageToDeploy((UniversePackage) catalogService.getPackage("internal", "shelly"));
		try {
			app = marathonAppsService
					.getServiceById(MARATHON_GROUP_NAME + "/" + userProvider.getUser().getIdep() + "/cloudshell");
			status.setStatus(CloudShellStatus.STATUS_UP);
			status.setUrl(app.getLabels().get("ONYXIA_URL"));
		} catch (MarathonException e) {
			status.setStatus(CloudShellStatus.STATUS_DOWN);
			status.setUrl(null);
		}

		return status;
	}

	public static class CloudShellStatus {

		public static final String STATUS_UP = "UP", STATUS_LOADING = "LOADING", STATUS_DOWN = "DOWN";
		private String status = STATUS_UP;
		private String url = null;
		private UniversePackage packageToDeploy = null;

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

		public UniversePackage getPackageToDeploy() {
			return packageToDeploy;
		}

		public void setPackageToDeploy(UniversePackage packageToDeploy) {
			this.packageToDeploy = packageToDeploy;
		}

	}
}
