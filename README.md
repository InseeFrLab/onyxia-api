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

### Using Helm (work in progress)

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

[Marathon](https://mesosphere.github.io/marathon/) configuration
| Key | Default | Description |
| --------------------- | ------- | ------------------------------------------------------------------ |
| `marathon.enabled` | `true` | Enable Marathon support |
| `marathon.url` | | Marathon base URL |
| `marathon.auth.token` | | If using DC/OS entreprise security, a valid token with `read, create, update, delete` permissions on `dcos:service:marathon:marathon:services:/{marathon.group.name}` |
| `marathon.auth.basic.username` | | |
| `marathon.auth.basic.password` | | |
| `marathon.group.name` | `users` | Base group / subgroup to deploy apps to. |
| `marathon.dns.suffix` | `marathon.containerip.dcos.thisdcos.directory` | |
| `marathon.publish.domain` | `selfservice.example.com` | External dns name |

Kubernetes configuration  
| Key | Default | Description |
| --------------------- | ------- | ------------------------------------------------------------------ |
| `kubernetes.enabled` | `false` | Enable Kubernetes support |
| `kubernetes.namespace.prefix` | `user-` | Prefix for user namespaces. Can be blank. |

Catalogs configuration  
| Key | Default | Description |
| --------------------- | ------- | ------------------------------------------------------------------ |
| `catalogs.configuration` | `classpath:multiverse.json` | Catalogs to use. Defaults to [multiverse.json](onyxia-api/src/main/resources/multiverse.json). `http://`, `https://` and `file:` schemes are supported |  
| `catalogs.refresh.ms` | `300000` (5 minutes) | The rate at which the catalogs should be refreshed. `<= 0` means no refreshs after initial loading |

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
