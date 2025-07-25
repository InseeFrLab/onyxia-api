# Onyxia API

This is the server part of the Onyxia datalab, it interacts with your container orchestrator (Kubernetes) to deploy users services.  
Deployable services are listed and configured inside catalogs.  
Default catalogs are from InseeFrlab : [Interactive services](https://inseefrlab.github.io/helm-charts-interactive-services), [Databases](https://inseefrlab.github.io/helm-charts-databases), [Automation](https://inseefrlab.github.io/helm-charts-automation) but more catalogs (including your own) can be added.

## Quick start  

Onyxia-api is usually run as a component within the Onyxia stack. See [Onyxia helm chart](https://github.com/InseeFrLab/onyxia/tree/main/helm-chart) and [docs.onyxia.sh](https://docs.onyxia.sh/) for installation instruction.  

## Running Onyxia-API standalone

### Using docker

```
docker run -p 8080:8080 inseefrlab/onyxia-api
```

### Using Java / maven (from sources)

```
git clone https://github.com/InseeFrLab/onyxia-api.git
cd onyxia-api
mvn spring-boot:run
```

## Usage

Once Onyxia is started, browse to http://localhost:8080 to get started with the OpenAPI documentation.  

## Contributing

Contributions are welcome.
Make sure to conform to Android Open Source Project code style : `mvn spotless:apply` can enforce it.

## Configuration

Main configuration file is [onyxia-api/src/main/resources/application.properties](onyxia-api/src/main/resources/application.properties).  
Each variable can be overridden using environment variables.  

### Regions configuration :
| Key | Default | Description |
| --------------------- | ------- | ------------------------------------------------------------------ |
| `regions` | [onyxia-api/src/main/resources/regions.json](onyxia-api/src/main/resources/regions.json) | List of regions, see [Region configuration](docs/region-configuration.md) |

### Catalogs configuration :

| Key | Default | Description |
| --------------------- | ---------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `catalogs` | [onyxia-api/src/main/resources/catalogs.json](onyxia-api/src/main/resources/catalogs.json) | List of helm catalogs, see [Admin doc](https://docs.onyxia.sh/admin-doc/catalog-of-services) |
| `catalogs.refresh.ms` | `300000` (5 minutes) | The rate at which the catalogs should be refreshed. `<= 0` means no refreshs after initial loading |

### Authentication configuration
| Key | Default | Description |
| --------------------- | ------- | ------------------------------------------------------------------ |
| `authentication.mode` | `none` | Supported modes are : `none`, `openidconnect` (must be configured) |

### Open id configuration (used when `authentication.mode`=`openidconnect`)  

The only two required parameters are `oidc.issuer-uri` and `oidc.clientID`. See below for more details.

Configurable properties :  
| Key | Default | Description |
| -------------------------------- | ---------- | ---------------------------------------------------------------------------------------------------------------------------------------- |
| `oidc.issuer-uri` | | Issuer URI. Onyxia-API will use this URL to retrieve the public key for token validation. e.g for Keycloak : `https://keycloak.example.com/auth/realms/REALMNAME` |
| `oidc.clientID` | | Client id to be used by Onyxia web application |
| `oidc.username-claim` | `preferred_username` | Claim to be used as user id. Must conform to [RFC 1123](https://kubernetes.io/docs/concepts/overview/working-with-objects/names/#dns-label-names) |
| `oidc.groups-claim` | `groups` | Claim to be used as list of user groups. |
| `oidc.roles-claim` | `roles` | Claim to be used as list of user roles. |
| `oidc.audience` | | Optional : audience to validate. Must be the same as the token's `aud` field |
| `oidc.skip-tls-verify` | `false` | Disable tls cert verification when retrieving keys from the IDP. Not intended for production. Consider mounting the proper `cacerts` instead of disabling the verification. |
| `oidc.public-key` | | Optional: If for some reason you don't want Onyxia-API to bootstrap configuration by requesting the `issuer-uri` then you can manually provide the public key used for validating incoming tokens. |
| `oidc.extra-query-params` | | Optional : query params to be added by client. e.g : `prompt=consent&kc_idp_hint=google` |
| `oidc.scope` | `openid profile` | Optional : Specifies the OIDC scopes to be requested by the Onyxia client. `"openid"` is always requested, regardless of this setting. |
| `oidc.idleSessionLifetimeInSeconds` | | Optional: Automatically logs out users after a set period of inactivity. |

### Security configuration :
| Key | Default | Description |
| --------------------- | ------- | ------------------------------------------------------------------ |
| `security.cors.allowed_origins` | | To indicate which origins are allowed by [CORS](https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS) |

### HTTP configuration  
| Key | Default                                                            | Description                                                                                        |
| --------------------- |--------------------------------------------------------------------|----------------------------------------------------------------------------------------------------|
| `http.proxyHost` |                                                                    | Proxy hostname (e.g : proxy.example.com)                                                           |
| `http.proxyPort` | 80 for `HTTP`, 443 for `HTTPS`                                     | Proxy port                                                                                         |
| `http.noProxy` |                                                                    | Hosts that should not use the proxy (e.g : `localhost,host.example.com`)                           |
| `http.proxyUsername` |                                                                    | Username if the proxy requires authentication                                                      |
| `http.proxyPassword` |                                                                    | Password if the proxy requires authentication                                                      |
| `http.cacheEnabled` | true                                                               | Enable cache for charts retrieval.                                                                 |
| `http.cacheMaxSizeMB` | 500                                                                | Maximum size (in MB) used by the cache (cleanup will be done automatically if space is not enough) |
| `http.overrideCacheLocation` | `${java.io.tmpdir}/http_cache` (=`/tmp/http_cache` on most setups) | Specify where to store the cache                                                                   |

### Events
| Key                      | Default | Description                                                        |
|--------------------------|--|--------------------------------------------------------------------|
| `event.logging.enabled`  | `true` | whether events should be logged or not                             |
| `event.webhook.enabled`  | `false` | whether events should be sent to an external webhook via HTTP POST |
| `event.webhook.url`      |  | URL of the webhook to send the events to                           |
| `event.webhook.includes` |  | List of events types to send the webhook for (empty = all events). e.g `service.uninstall,service.install`                           |
| `event.webhook.excludes` |  | List of events types to ignore for the webhook. e.g `service.uninstall,service.install`                           |

### Admin configuration:
:warning: This section should be considered pre-alpha and may be subject to major changes and revamps :warning:

| Key              | Default | Description                                                                                |
|------------------|-----|--------------------------------------------------------------------------------------------|
| `admin.enabled`  | `false` | Whether to enable the admin endpoints. :warning: Do not use this in production ! :warning: |


### Other configurations
| Key | Default | Description |
| --------------------- | ------- | ------------------------------------------------------------------ |
| `logging.structured.format.console` | `` | Format for structured logging. Valid values : `ecs`, `gelf`, `logstash`. Leave empty for no structured logging (default). See https://docs.spring.io/spring-boot/reference/features/logging.html#features.logging.structured  |
| `springdoc.swagger-ui.path` | `/` | Open API (swagger) UI path |
| `springdoc.swagger-ui.oauth.clientId` | `` | clientid used by swagger to authenticate the user, in general the same which is used by onyxia-ui is ok. |
| `DEBUG_JMX` | `` | Enable JMX monitoring. This is useful for profiling the app to improve performance but is not intended for production / daily use. Once enabled (`true`), use `kubectl port-forward` + a profiler (e.g VisualVM) to profile the app. |
| `JMX_PORT` | `10000` | Port used by JMX if enabled. |

## Onyxia API dependency to Helm

Onyxia-API makes system calls to `helm` using the [helm-wrapper](helm-wrapper) Java library.  
`helm` is bundled in the `Onyxia API` Docker image, see current version bundled here : [Dockerfile](onyxia-api/Dockerfile).  
If running `Onyxia API` locally you need to have `helm` available in the `PATH`.  

## Onyxia Helm format extension

Onyxia's catalogs are based on the Helm chart format and especially the `values.schema.json` (see https://helm.sh/docs/topics/charts/#schema-files) file used to populate the personalization tabs displayed by the UI.  
Onyxia is **fully interoperable** with the Helm chart format which means you can use any helm chart repository as a onyxia catalog. But you probably want to use one that includes `values.schema.json` files (those files are optional in helm).  
Onyxia extends this format to enhance it and provide more customization tools in the UI.  

An example of such extension can be found [here](https://github.com/InseeFrLab/helm-charts-interactive-services/blob/main/charts/jupyter-python/values.schema.json#L190), see `x-onyxia`.
