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
You have to specify `oidc.issuer-uri`. `oidc.jwk-uri` is optional.  
Common used configurations :  
| Provider | `oidc.issuer-uri` | `oidc.jwk-uri` |
|---|---|---|
| Keycloak  | `https://keycloak.example.com/auth/realms/REALMNAME` |   |
| Google  | https://accounts.google.com  | `https://www.googleapis.com/oauth2/v3/certs` |
| Microsoft | `https://login.microsoftonline.com/TENANTID/v2.0` |   |

Configurable properties :  
| Key | Default | Description |
| -------------------------------- | ---------- | ---------------------------------------------------------------------------------------------------------------------------------------- |
| `oidc.issuer-uri` | | Issuer URI, should be the same as the `iss` field of the tokens |
| `oidc.skip-tls-verify` | `false` | Disable tls cert verification when retrieving keys from the IDP. Not intended for production. Consider mounting the proper `cacerts` instead of disabling the verification. |
| `oidc.jwk-uri` | | JWK URI, useful when auto discovery is not available or when `iss` is not consistent across tokens (e.g [Google](https://stackoverflow.com/questions/38618826/can-i-get-a-consistent-iss-value-for-a-google-openidconnect-id-token)) |
| `oidc.public-key` | | Public key used for validating incoming tokens. Don't provide this if you set `issuer-uri` or `jwk-uri` as it will be bootstrapped from that. This is useful if Onyxia-API has trouble connecting to your IDP (e.g self signed certificate). You can usually get this key directly by loading the issuer URI : (e.g `https://auth.example.com/realms/my-realm`) |
| `oidc.clientID` | | Client id to be used by Onyxia web application |
| `oidc.audience` | | Optional : audience to validate. Must be the same as the token's `aud` field |
| `oidc.username-claim` | `preferred_username` | Claim to be used as user id. Must conform to [RFC 1123](https://kubernetes.io/docs/concepts/overview/working-with-objects/names/#dns-label-names) |
| `oidc.groups-claim` | `groups` | Claim to be used as list of user groups. |
| `oidc.extra-query-params` | | Optional : query params to be added by client. e.g : `prompt=consent&kc_idp_hint=google` |

### Security configuration :
| Key | Default | Description |
| --------------------- | ------- | ------------------------------------------------------------------ |
| `security.cors.allowed_origins` | | To indicate which origins are allowed by [CORS](https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS) |

### HTTP configuration  
| Key | Default | Description |
| --------------------- | ------- | ------------------------------------------------------------------ |
| `http.proxyHost` | | Proxy hostname (e.g : proxy.example.com) |
| `http.proxyPort` | 80 for `HTTP`, 443 for `HTTPS` | Proxy port |
| `http.noProxy` | | Hosts that should not use the proxy (e.g : `localhost,host.example.com`) |
| `http.proxyUsername` | | Username if the proxy requires authentication |
| `http.proxyPassword` | | Password if the proxy requires authentication |

### Events
| Key                      | Default | Description                                                        |
|--------------------------|--|--------------------------------------------------------------------|
| `event.logging.enabled`  | `true` | whether events should be logged or not                             |
| `event.webhook.enabled`  | `false` | whether events should be sent to an external webhook via HTTP POST |
| `event.webhook.url`      |  | URL of the webhook to send the events to                           |
| `event.webhook.includes` |  | List of events types to send the webhook for (empty = all events). e.g `service.uninstall,service.install`                           |
| `event.webhook.excludes` |  | List of events types to ignore for the webhook. e.g `service.uninstall,service.install`                           |

### Other configurations
| Key | Default | Description |
| --------------------- | ------- | ------------------------------------------------------------------ |
| `springdoc.swagger-ui.path` | `/` | Open API (swagger) UI path |
| `springdoc.swagger-ui.oauth.clientId` | `` | clientid used by swagger to authenticate the user, in general the same which is used by onyxia-ui is ok. |

## Onyxia API dependency to Helm

Onyxia-API makes system calls to `helm` using the [helm-wrapper](helm-wrapper) Java library.  
`helm` is bundled in the `Onyxia API` Docker image, see current version bundled here : [Dockerfile](onyxia-api/Dockerfile).  
If running `Onyxia API` locally you need to have `helm` available in the `PATH`.  

## Onyxia Helm format extension

Onyxia's catalogs are based on the Helm chart format and especially the `values.schema.json` (see https://helm.sh/docs/topics/charts/#schema-files) file used to populate the personalization tabs displayed by the UI.  
Onyxia is **fully interoperable** with the Helm chart format which means you can use any helm chart repository as a onyxia catalog. But you probably want to use one that includes `values.schema.json` files (those files are optional in helm).  
Onyxia extends this format to enhance it and provide more customization tools in the UI.  

An example of such extension can be found [here](https://github.com/InseeFrLab/helm-charts-interactive-services/blob/main/charts/jupyter-python/values.schema.json#L190), see `x-onyxia`.
