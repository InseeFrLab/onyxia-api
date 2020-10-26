# Onyxia API

This is the server part of the Onyxia datalab.  
It interacts with your container orchestrator to deploy users services.  
Onyxia supports both Mesos (using Marathon) and Kubernetes (using Helm).  
Deployable services are listed and configured inside catalogs (Universe for Marathon, Helm charts for Helm).  
Default catalogs are from InseeFrlab : [Universe datascience](https://github.com/InseeFrLab/Universe-Datascience) and [Inseefrlab helm charts](https://github.com/InseeFrLab/helm-charts) but more catalogs can be added.

The opensourcing (and documentation) is still a work in progress, please be patient :)

## Quick start

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

### Using Helm

A [helm](helm.sh) package is available at [inseefrlab](https://github.com/InseeFrLab/helm-charts)

```
helm repo add inseefrlab https://inseefrlab.github.io/helm-charts
helm install inseefrlab/onyxia
```

Note that this installs both the API and the [UI](https://github.com/inseefrlab/onyxia-ui)

## Usage

Once onyxia is started, browse to http://localhost:8080 to get the OpenAPI documentation.  
Onyxia-API is primarly made to work with the webapp [Onyxia-UI](https://github.com/inseefrlab/onyxia-ui).  
If you use it in other ways, we would love to hear from you :)

## Configuration

Main configuration file is [onyxia-api/src/main/resources/application.properties](onyxia-api/src/main/resources/application.properties).  
Each variable can be overriden using environment variables.

Authentication configuration
| Key | Default | Description |
| --------------------- | ------- | ------------------------------------------------------------------ |
| `authentication.mode` | `none` | Supported modes are : `none`, `openidconnect` (must be configured) |

Open id configuration  
| Key | Default | Description |
| -------------------------------- | ---------- | ---------------------------------------------------------------------------------------------------------------------------------------- |
| `keycloak.realm` | | See [Keycloak configuration](https://www.keycloak.org/docs/latest/securing_apps/#_java_adapter_config) |
| `keycloak.resource` | | See [Keycloak configuration](https://www.keycloak.org/docs/latest/securing_apps/#_java_adapter_config) |
| `keycloak.auth-server-url` | | See [Keycloak configuration](https://www.keycloak.org/docs/latest/securing_apps/#_java_adapter_config) |
| `keycloak.ssl-required` | `external` | See [Keycloak configuration](https://www.keycloak.org/docs/latest/securing_apps/#_java_adapter_config) |
| `keycloak.public-client` | `true` | See [Keycloak configuration](https://www.keycloak.org/docs/latest/securing_apps/#_java_adapter_config) |
| `keycloak.enable-basic-auth` | `true` | See [Keycloak configuration](https://www.keycloak.org/docs/latest/securing_apps/#_java_adapter_config) |
| `keycloak.bearer-only` | `true` | See [Keycloak configuration](https://www.keycloak.org/docs/latest/securing_apps/#_java_adapter_config) |
| `keycloak.disable-trust-manager` | `false` | See [Keycloak configuration](https://www.keycloak.org/docs/latest/securing_apps/#_java_adapter_config) |

Security configuration :
| Key | Default | Description |
| --------------------- | ------- | ------------------------------------------------------------------ |
| `security.cors.allowed_origins` |  | To indicate which origins are allowed by [CORS](https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS) |

Regions configuration :
| Key | Default | Description |
| --------------------- | ------- | ------------------------------------------------------------------ |
| `regions` | see [onyxia-api/src/main/resources/regions.json](onyxia-api/src/main/resources/regions.json) | List of regions. Each region can be of type `KUBERNETES` or `MARATHON`. Mixing is supported. For `KUBERNETES`, `in-cluster` configuration is possible |

Catalogs configuration :  

| Key | Default | Description |
| --------------------- | ------- | ------------------------------------------------------------------ |
| `catalogs` | see [onyxia-api/src/main/resources/catalogs.json](onyxia-api/src/main/resources/catalogs.json) | List of catalogs. Each catalog can be of type `universe` or `helm`. Mixing is supported. If there is no region of corresponding type then the catalog will be ignored |
| `catalogs.refresh.ms` | `300000` (5 minutes) | The rate at which the catalogs should be refreshed. `<= 0` means no refreshs after initial loading |

HTTP configuration  
| Key | Default | Description |
| --------------------- | ------- | ------------------------------------------------------------------ |
| `http.proxyHost` | | Proxy hostname (e.g : proxy.example.com) |
| `http.proxyPort` | 80 for `HTTP`, 443 for `HTTPS` | Proxy port |
| `http.noProxy` | | Hosts that should not use the proxy (e.g : `localhost|host.example.com`) |
| `http.proxyUsername` | | Username if the proxy requires authentication |
| `http.proxyPassword` | | Password if the proxy requires authentication |

Other configurations
| Key | Default | Description |
| --------------------- | ------- | ------------------------------------------------------------------ |
| `springdoc.swagger-ui.path` | `/` | Open API (swagger) UI path |
|`springdoc.swagger-ui.oauth.clientId`|``|clientid use by swagger to authenticate the user, in general the same which is use by onyxia-ui is ok.|

## Onyxia Universe package format extension

Onyxia extends the official Universe format (see https://github.com/mesosphere/universe) to enhance it.  
This format extension is **fully interoperable** with the official Universe format meaning **Onyxia works with any Universe** and **Universes using Onyxia's extension should be usable in other apps**.

The specification is defined [here](docs/specification/README.md).

An example of a Universe using this extension is available [here](https://github.com/inseefrlab/Universe-Datascience).
