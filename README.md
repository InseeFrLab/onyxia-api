# Onyxia API

This is the server part of the Onyxia datalab, it interacts with your container orchestrator (Kubernetes) to deploy users services.  
Deployable services are listed and configured inside catalogs.  
Default catalogs are from InseeFrlab : [Interactive services](https://inseefrlab.github.io/helm-charts-interactive-services), [Databases](https://inseefrlab.github.io/helm-charts-databases), [Automation](https://inseefrlab.github.io/helm-charts-automation) but more catalogs (including your own) can be added.

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

The [Onyxia helm chart](https://github.com/InseeFrLab/helm-charts/tree/master/charts/onyxia) is available at [inseefrlab](https://github.com/InseeFrLab/helm-charts)

```
helm repo add inseefrlab https://inseefrlab.github.io/helm-charts
helm install inseefrlab/onyxia
```

This will install both the [API](https://github.com/InseeFrLab/onyxia-api) and the [Web](https://github.com/inseefrlab/onyxia-web) components.

## Usage

Once Onyxia is started, browse to http://localhost:8080 to get the OpenAPI documentation.  
Onyxia-API is primarly made to work with the webapp [Onyxia-Web](https://github.com/inseefrlab/onyxia-web).  
If you use it in other ways, we would love to hear from you :)

## Configuration

Main configuration file is [onyxia-api/src/main/resources/application.properties](onyxia-api/src/main/resources/application.properties).  
Each variable can be overridden using environment variables.  

Regions configuration :
| Key | Default | Description |
| --------------------- | ------- | ------------------------------------------------------------------ |
| `regions` | [onyxia-api/src/main/resources/regions.json](onyxia-api/src/main/resources/regions.json) | List of regions, see [Region configuration](docs/region-configuration.md) |

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
| `security.cors.allowed_origins` | | To indicate which origins are allowed by [CORS](https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS) |

Catalogs configuration :

| Key | Default | Description |
| --------------------- | ---------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `catalogs` | see [onyxia-api/src/main/resources/catalogs.json](onyxia-api/src/main/resources/catalogs.json) | List of catalogs. Each catalog can be of type `universe` or `helm`. Mixing is supported. If there is no region of corresponding type then the catalog will be ignored |
| `catalogs.refresh.ms` | `300000` (5 minutes) | The rate at which the catalogs should be refreshed. `<= 0` means no refreshs after initial loading |

HTTP configuration  
| Key | Default | Description |
| --------------------- | ------- | ------------------------------------------------------------------ |
| `http.proxyHost` | | Proxy hostname (e.g : proxy.example.com) |
| `http.proxyPort` | 80 for `HTTP`, 443 for `HTTPS` | Proxy port |
| `http.noProxy` | | Hosts that should not use the proxy (e.g : `localhost,host.example.com`) |
| `http.proxyUsername` | | Username if the proxy requires authentication |
| `http.proxyPassword` | | Password if the proxy requires authentication |

Other configurations
| Key | Default | Description |
| --------------------- | ------- | ------------------------------------------------------------------ |
| `springdoc.swagger-ui.path` | `/` | Open API (swagger) UI path |
| `springdoc.swagger-ui.oauth.clientId` | `` | clientid used by swagger to authenticate the user, in general the same which is used by onyxia-ui is ok. |

## Onyxia API dependency to Helm

Onyxia-API makes system calls to `helm` using the [helm-wrapper](helm-wrapper) Java library.  
`helm` is bundled in the `Onyxia API` Docker image, see current version bundled here : [Dockerfile](onyxia-api/Dockerfile).  
If running `Onyxia API` locally you need to have `helm` available in the `PATH`.  

## Onyxia API compatibility matrix with Kubernetes

In addition of using `helm`, Onyxia API interacts with the Kubernetes cluster thanks to the [fabric8.io Kubernetes client for Java](https://github.com/fabric8io/kubernetes-client).  
See [here](https://github.com/fabric8io/kubernetes-client#kubernetes-compatibility-matrix) for the compatibility matrix and [here](onyxia-api/helm-wrapper/pom.xml) for the current version used by Onyxia.

## Onyxia Helm format extension

Onyxia's catalogs are based on the Helm chart format and especially the `values.schema.json` (see https://helm.sh/docs/topics/charts/#schema-files) file used to populate the personalization tabs displayed by the UI.  
Onyxia is **fully interoperable** with the Helm chart format which means you can use any helm chart repository as a onyxia catalog. But you probably want to use one that includes `values.schema.json` files (those files are optional in helm).  
Onyxia extends this format to enhance it and provide more customization tools in the UI.  

An example of such extension can be found [here](https://github.com/InseeFrLab/helm-charts-interactive-services/blob/main/charts/jupyter-python/values.schema.json#L190), see `x-onyxia`.