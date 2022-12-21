# Region configuration  

A **region** is the configuration of an independant set of Onyxia services. Thus multiple configuration accessing different services can be plugged on a single Onyxia instance.

A region mainly defines **Onyxia service provider** on which are run the users, groups and global services and how users can interact with it. It also defines a **S3 object storage** and how "buckets" are provided to users.

Most of the configuration of an Onyxia client comes from the region that can be accessed as json via /public/configuration or /public/regions.

See [regions.json](/onyxia-api/src/main/resources/regions.json) for a complete example of regions configuration.

- [Region configuration](#region-configuration)
  - [Main region properties](#main-region-properties)
  - [Services properties](#services-properties)
    - [Server properties](#server-properties)
    - [Quotas properties](#quotas-properties)
    - [Default configuration properties](#default-configuration-properties)
    - [CustomInitScript properties](#custom-init-script-properties)
  - [Data properties](#data-properties)
    - [S3](#s3)
    - [Atlas](#atlas)
  - [Vault properties](#vault-properties)

## Main region properties

| Key | Description | Example |
| --------------------- | ------------------------------------------------------------------ | ----- |
| `id` | Unique name of the region | "mycloud" |
| `name` | Descriptive name for the region | "mycloud region" |
| `description` | Description of the region | "This region is in an awesome cloud" |
| `location` | Geographical position of the datacenter on which the region is supposed to run. | {lat: 48.864716, longitude: 2.349014, name: "Paris" } |
| `includedGroupPattern` | Pattern of user groups considered for the user in the region. Patterns are case sensitive. | ".*_Onyxia" |
| `excludedGroupPattern` | Pattern of user groups that will not be considered for the user in the region. Patterns are case sensitive. | ".*_BadGroup" |
| `transformGroupPattern` | Indicate how to transform a group based on `includedGroupPattern` to make a project name used for namespace or S3 bucket for example. For example with a `includedGroupPattern` of "(.*)_Onxyia" and a `transformGroupPattern` of "$1-k8s", a mygroup_Onyxia will generate a mygroup-k8s namespace. | "$1-k8s" |
| `onyxiaAPI` | Contains the base url of an onyxia api | {baseURL: "http://localhost:8080"} |
| `services` | Configuration of Onyxia services provider platform | See [Services properties](#services-properties) |
| `data` | Configuration of the S3 Object Storage | See [S3](#data-properties) |
| `vault` | Configuration of the Vault API | See [Vault properties](#vault-properties) |

## Services properties

The onyxia service plateform is a Kubernetes cluster but Onyxia is meant to be extendable to other types of platform if necessary.

Users can work on Onyxia as a User or as a Group to which they belong. Each user and group can have its own **namespace** which is an isolated space of Kubernetes.

| Key | Default | Description | Example |
| --------------------- | ------- | ------------------------------------------------------------------ | ---- |
| `type` | | Type of the platform on which services are launched. Only Kubernetes is supported, Marathon has been removed. | "KUBERNETES" |
| `allowNamespaceCreation` | true | If true, the /onboarding endpoint is enabled and the user will have a namespace created on its first request on a service resource. | true |
| `singleNamespace` | true | When true, all users share the same namespace on the service provider. This configuration can be used if a project work on its own Onyxia region. | |
| `userNamespace` | true | When true, all users have a namespace for his work. This configuration can be used if you don't allow user to have their own space to work and only use project space | |
| `namespacePrefix` | "user-" | User have a personal namespace like namespacePrefix + userId (should only be used when not singleNamespace but not the case) | |
| `groupNamespacePrefix` | "projet-" | User in a group groupId can access the namespace groupeNamespacePrefix + groupId. This prefix is also used for vault group directory. | |
| `usernamePrefix` | | If set, the Kubernetes user corresponding to the Onyxia user is named usernamePrefix + userId on impersonation mode, otherwise it is identified only as userId | "user-" |
| `groupPrefix` | | not used | |
| `authenticationMode` | IMPERSONATE | IMPERSONATE or ADMIN : on ADMIN mode Onyxia uses its admin account on the services provider, with IMPERSONATE mode Onyxia request the API as the user (helm option --kube-as-user) but is only available if the helm version used is above 3.4.0 | |
| `expose` | | When users request to expose their service, only subdomain of this object domain are allowed | See [Expose properties](#expose-properties) |
| `monitoring` | | Define the URL pattern of the monitoring service that is to be launch with each service. Only for client purpose. | {URLPattern: "https://$NAMESPACE-$INSTANCE.mymonitoring.sspcloud.fr"} |
| `cloudshell` | | Define the catalog and package name where to fetch the cloudshell in the helm catalog. | {catalogId: "inseefrlab-helm-charts-datascience", packageName: "cloudshell"} |
| `initScript` | | Define where to fetch a script that will be launched on some service on startup. | "https://inseefrlab.github.io/onyxia/onyxia-init.sh" |
| `allowedURIPattern` | "^https://" | Init scripts set by the user have to respect this pattern. | |
| `server` | | Define configuration of the services provider API server, this value is not served on the API as it contains credentials for the API. | See [Server properties](#server-properties) |
| `k8sPublicEndpoint` | | Define external access to kubernetes API if available. It helps Onyxia users to directly connect to kubernetes outside the datalab | See [K8sPublicEndpoint properties](#k8sPublicEndpoint-properties) |
| `quotas` | | Properties setting quotas on how much resource a user can get on the services provider. | See [Quotas properties](#quotas-properties) |
| `defaultConfiguration` | | Default configuration on services that a user can override. For client purpose only. | See [Default Configuration](#default-configuration-properties) |
| `customInitScript` | | This can be use to customize user environnement using a regional script executed by some users pods. | See [CustomInitScript properties](#custom-init-script-properties) |

### CustomInitScript properties

These properties define how to reach the **service provider API**.

| Key | Description | Example |
| --------------------- | ------------------------------------------------------------------ | ---- |
| `URL` | URL of the init script | "api.kub.sspcloud.fr" |
| `checksum` | checksum of the init script |  |

### Server properties

These properties define how to reach the **service provider API**.

| Key | Description | Example |
| --------------------- | ------------------------------------------------------------------ | ---- |
| `URL` | URL of the service provider API | "api.kub.sspcloud.fr" |
| `auth` | Credentials for the service provider API. | {token: "ey...", password : "pwd", username: "admin"} |

### K8sPublicEndpoint properties

It can be used to add additionnal feature to Onyxia. It helps Onyxia users to directly connect to kubernetes outside the datalab.

| Key | Default | Description | Example |
| --------------------- | ------- | ------------------------------------------------------------------ | ---- |
| `URL` | | public URL of the kubernetes API of the region. | "https://vault.change.me" |
| `keycloakParams` | | Configuration of the keycloak service used to get an access token on the kubernetes api. It defines the keycloak realm, clientId, and Url. | {realm: "sspcloud", clientId: "kubernetes", URL: "https://auth.change.me/auth"} |

### Quotas properties

When this feature is enabled, namespaces are created with **quotas**.

| Key | Default | Description |
| --------------------- | ------- | ------------------------------------------------------------------ |
| `enabled` | false | Whether or not users are subject to a resource limitation. Quotas can only be applied on user and not on group. |
| `allowUserModification` | true | Whether or not the user can manually disable or change its own limitation. |
| `defaultQuota` | | The quota applied on the namespace before user modification or on reset. |

A quota follows the kubernetes model which is composed of:
"requests.memory"
"requests.cpu"
"limits.memory"
"limits.cpu"
"requests.storage"
"count/pods"

### Expose properties

 with **expose**.

| Key | Default | Description |
| --------------------- | ------- | ------------------------------------------------------------------ |
| `domain` | | When users request to expose their service, only subdomain of this object will be created. |
| `ingressClassName` | '' | Ingress Class Name : useful if you want to use a specific ingress controller in stead of a default one |
| `ingress` | true | Whether or not Kubernetes Ingress is enabled |
| `route` | false | Whether or not OpenShift Route is enabled |

### Default configuration properties

| Key | Default | Description |
| --------------------- | ------- | ------------------------------------------------------------------ |
| `IPProtection` | false | Whether or not the default behavior of the reverse-proxy serving the service is to block request from an ip other than the one from which it has been created. For client purpose only. |
| `networkPolicy` | false | Whether or not services can be reached by pods outside of the current namespace. For client purpose only. |
| `from` | NA | List of allowed source (kubernetes network policies format for from) to reach user http services. Used to allow ingress access to users services |
| `nodeSelector` | NA | This node selector can be injected in a service to restrain on which node it can be launched  |
| `tolerations` | NA | This node selector can be injected in a service to force it to run on nodes with this taint |
| `startupProbe` | NA | This startup probe can be injected in a service. It can help you in environment with slow network to specify a long duration before killing a container |
| `kafka` | | See [Kafka](#kafka) |
| `sliders` | | See [Sliders](#sliders) |
| `Resources` | | See [Resources](#resources) |

#### Kafka

Kafka can be used to get some events in users chart like hive metastore.

| Key | Default | Description |
| --------------------- | ------- | ------------------------------------------------------------------ |
| `URL` | N.A | brokerURL |
| `topicName` | N.A | topic name for those events |

#### Sliders

Sliders specify some slider parameters that may overwrite some defaults.

| Key | Default | Description |
| --------------------- | ------- | ------------------------------------------------------------------ |
| `cpu` | N.A | cpu slider parameters |
| `memory` | N.A | memory slider parameters |
| `gpu` | N.A | gpu slider parameters |
| `disk` | N.A | disk slider parameters |


| Key | Default | Description |
| --------------------- | ------- | ------------------------------------------------------------------ |
| `sliderMin` | N.A | sliderMin |
| `sliderMax` | N.A | sliderMax |
| `sliderStep` | N.A | sliderStep |
| `sliderUnit` | N.A | sliderUnit |

#### Resources

Resources specify some values that may overwrite some defaults.

| Key | Default | Description |
| --------------------- | ------- | ------------------------------------------------------------------ |
| `cpuRequest` | N.A | overwrite default cpu request if asked by helm-charts |
| `cpuLimit` | N.A | overwrite default cpu limit if asked by helm-charts |
| `memoryRequest` | N.A | overwrite default memory request if asked by helm-charts |
| `memoryLimit` | N.A | overwrite default memory limit if asked by helm-charts |
| `disk` | N.A | overwrite default disk size if asked by helm-charts |
| `gpu` | N.A | overwrite default  gpu if asked by helm-charts |


## Data properties

Data properties only contain an object storage S3 configuration.

### S3

There are several implementations of the S3 standard like Minio or AWS.

S3 storage are divided by **buckets** with their own access policy.

All these properties which configure the access to the storage are intended for Onyxia clients apart except properties on bucket naming.

| Key | Default | Description | Example |
| --------------------- | ------- | ------------------------------------------------------------------ | ---- |
| `type` | | Type of S3 storage implementation. | "minio", "amazon" |
| `URL` | | URL of the S3 service for the region. Only used when type is minio. | "https://minio.lab.sspcloud.fr" |
| `region` | | Name of the region on the S3 service when this service deals with multiple regions. | "us-east-1" |
| `roleARN` | | Only used when type is "amazon". See [Assume Role With Web Identity Amazon documentation](https://docs.aws.amazon.com/STS/latest/APIReference/API_AssumeRoleWithWebIdentity.html) | |
| `roleSessionName` | | Only used when type is "amazon". See [Assume Role With Web Identity Amazon documentation](https://docs.aws.amazon.com/STS/latest/APIReference/API_AssumeRoleWithWebIdentity.html) | |
| `bucketClaim` | "preferred_username" | Key of the access token used to create bucket name. | "id" |
| `bucketPrefix` | | User buckets are named bucketPrefix + the value of the user bucketClaim | "user-" |
| `groupBucketPrefix` | | Group buckets are named groupBucketPrefix + the value of the user bucketClaim | "project-" |
| `defaultDurationSeconds` | | Maximum time to live of the S3 access key | 86400 |
| `keycloakParams` | | Configuration of the keycloak service used to get an access token on the S3 service. It defines the keycloak realm, clientId, and Url. | {realm: "sspcloud", clientId: "onyxia", URL: "https://auth.lab.sspcloud.fr/auth"} |
| `monitoring` | | Defines the URL pattern of the monitoring service of each bucket. | "https://monitoring.sspcloud.fr/$BUCKET_ID" |
| `acceptBucketCreation` | true | If true, the S3 client should not create bucket. | true |

### Atlas

Atlas is a data management tool.

It can be used to add additional feature to the file explorer to transform it into a data explorer

| Key | Default | Description | Example |
| --------------------- | ------- | ------------------------------------------------------------------ | ---- |
| `URL` | | URL of the atlas service for the region. | "https://atlas.change.me" |
| `keycloakParams` | | Configuration of the keycloak service used to get an access token on the S3 service. It defines the keycloak realm, clientId, and Url. | {realm: "sspcloud", clientId: "atlas", URL: "https://auth.change.me/auth"} |

## Vault properties

It can be used to add additional feature to Onyxia. It helps user to keep their secret safe.

| Key | Default | Description | Example |
| --------------------- | ------- | ------------------------------------------------------------------ | ---- |
| `URL` | | URL of the atlas service for the region. | "https://vault.change.me" |
| `kvEngine` | | mount point of the kv engine. | "onyxia-kv" |
| `role` | | role of the user in vault | "onyxia-user" |
| `keycloakParams` | | Configuration of the keycloak service used to get an access token on the vault service. It defines the keycloak realm, clientId, and Url. | {realm: "sspcloud", clientId: "vault", URL: "https://auth.change.me/auth"} |

## ProxyConfiguration properties

It can be used to inject proxyConfiguration in the services, if the helm chart in catalog allow it you can bind this value to helm chart value to override for example HTTP_PROXY, HTTPS_PROXY and NO_PROXY en variable in the pod launched.

| Key | Default | Description | Example |
| --------------------- | ------- | ------------------------------------------------------------------ | ---- |
| `httpProxyUrl` | | url of the enterprise proxy for the region for http. | "http://proxy.enterprise.com:8080" |
| `httpsProxyUrl` | | url of the enterprise proxy for the region for https. | "http://proxy.enterprise.com:8080" |
| `noProxy` | | enterprise local domain that should not take proxy comma separated | "corporate.com" |

## ProxyInjection properties

It can be used to inject proxy settings in the services, if the helm chart in catalog allow it you can bind this value to helm chart value to override for example HTTP_PROXY, HTTPS_PROXY and NO_PROXY en variable in the pod launched.

| Key | Default | Description | Example |
| --------------------- | ------- | ------------------------------------------------------------------ | ---- |
| `httpProxyUrl` | | url of the enterprise proxy for the region for http. | "http://proxy.enterprise.com:8080" |
| `httpsProxyUrl` | | url of the enterprise proxy for the region for https. | "http://proxy.enterprise.com:8080" |
| `noProxy` | | enterprise local domain that should not take proxy comma separated | "corporate.com" |

## PackageRepositoryInjection properties

It can be used to inject Package repository in the services, if the helm chart in catalog allow it you can bind this value to helm chart value to override for example the cran, pypi and conda repository to reach some local entreprise repository on the network.

| Key | Default | Description | Example |
| --------------------- | ------- | ------------------------------------------------------------------ | ---- |
| `cranProxyUrl` | | url of enterprise local cran repo. | "https://cranProxy" |
| `condaProxyUrl` | | url of enterprise local conda repo. | "https://condaProxyUrl" |
| `pypiProxyUrl` | | eurl of enterprise local pypi repo|"https://pypiProxyUrl" |

## CertificateAuthorityInjection properties

It can be used to inject CertificateAuthority in the services, if the helm chart in catalog allow it you can bind this value to helm chart value to add some certificate authorities in the pod.

| Key | Default | Description | Example |
| --------------------- | ------- | ------------------------------------------------------------------ | ---- |
| `crts` | | List of encodedbase64 crt. | "LS0tLSBCRUdJTiBDRVJUSUZJQ0FURS0tLS0KTUlJQ1VEQ0NBZG9DQkRhTTF0WXdEUVlKS29aSWh2Y05BUUVFQlFBd2dZOHhDekFKQmdOVkJBWVRBbFZUTVJNd01JSUNDREFhQmdrcWhraUc5dzBCQlFNd0RRUUlJZll5QUVGS2FFRUNBUVVFZ2dIb3pkbWdHejd6YkMxbWNKMnJjTkFRRUVCUUF3Z1k4eEN6QUpCZ05WQkFZVEFsVlRNUk13TUlJQ0NEQWFCZ2txaGtpRzlsVlRNUk13TUlJQ0NEQWFCZ2txaGtpRzl3MEJCUU13RFFRSUlmWXdEUVlKS29aSWh2Y05BUUVFQlFBd2dZOHhDekFraUc5dzBCQlFNd0RRUUlJZll5QUVGS2FFRUNBUVVFZ2dIb3pkbWdHejd3Z1k4eEN6QUpCZ05WQkFZVEFsVlRNUk13TUlJQ0NEQWFCZ2txaGtpRzl3MEJCUU13RFFRSUlmWXlBRUZLYUVFQ0FRVUVnZ0hvemRtZ0d6N3piQzFtY0oycmNOQVFFRUJRQXdnWTh4Q3pBSkJnTlZCQVlUQWxWVE1SCi0tLS1FTkQgQ0VSVElGSUNBVEUtLS0t" |
