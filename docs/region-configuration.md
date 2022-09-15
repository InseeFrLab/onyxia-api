# Region configuration

- [Region configuration](#region-configuration)
  - [Main region properties](#main-region-properties)
  - [Services properties](#services-properties)
    - [Server properties](#server-properties)
    - [Quotas properties](#quotas-properties)
    - [Default configuration properties](#default-configuration-properties)
  - [Data properties](#data-properties)
    - [S3](#s3)
    - [atlas](#atlas)
  - [Vault properties](#vault-properties)

A **region** is the configuration of an independant set of Onyxia services. Thus multiple configuration accessing different services can be plugged on a single Onyxia instance.

A region mainly defines **Onyxia service provider** on which are run the users, groups and global services and how users can interact with it. It also defines a **S3 object storage** and how "buckets" are provided to users.

Most of the configuration of an Onyxia client comes from the region that can be accessed as json via /public/configuration or /public/regions.

See [regions.json](/onyxia-api/src/main/resources/regions.json) for a complete example of regions configuration.

## Main region properties

| Key | Description | Example |
| --------------------- | ------------------------------------------------------------------ | ----- |
| `id` | Unique name of the region | "mycloud" |
| `name` | Descriptive name for the region | "mycloud region" |
| `description` | Description of the region | "This region is in an awesome cloud" |
| `location` | Geographical position of the datacenter on which the region is supposed to run. | {lat: 48.864716, longitude: 2.349014, name: "Paris" } |
| `includedGroupPattern` | Pattern of user groups considered for the user in the region. Patterns are case insensitive. | ".*_Onyxia" |
| `excludedGroupPattern` | Pattern of user groups that will not be considered for the user in the region. Patterns are case insensitive. | ".*_BadGroup" |
| `onyxiaAPI` | Contains the base url of an onyxia api | {baseURL: "http://localhost:8080"} |
| `services` | Configuration of Onyxia services provider platform | See [Service](##services-properties) |
| `data` | Configuration of the S3 Object Storage | See [S3](#data-properties) |
| `vault` | Configuration of the Vault API | See [vault](#vault-properties) |

## Services properties

The onyxia service plateform is a Kubernetes cluster but Onyxia is meant to be extendable to other types of platform if necessary.

Users can work on Onyxia as a User or as a Group to which they belong. Each user and group can have its own **namespace** which is an isolated space of Kubernetes.

| Key | Default | Description | Example |
| --------------------- | ------- | ------------------------------------------------------------------ | ---- |
| `type` | | Type of the platform on which services are launched. Only Kubernetes is supported, Marathon has been removed. | "KUBERNETES" |
| `singleNamespace` | true | When true, all users share the same namespace on the service provider. This configuration can be used if a project work on its own Onyxia region. | |
| `namespacePrefix` | "user-" | User have a personal namespace like namespacePrefix + userId (should only be used when not singleNamespace but not the case) | |
| `groupNamespacePrefix` | "projet-" | User in a group groupId can access the namespace groupeNamespacePrefix + groupId. This prefix is also used for vault group directory. | |
| `usernamePrefix` | | If set, the Kubernetes user corresponding to the Onyxia user is named usernamePrefix + userId on impersonation mode, otherwise it is identified only as userId | "user-" |
| `groupPrefix` | | not used | |
| `authenticationMode` | IMPERSONATE | IMPERSONATE or ADMIN : on ADMIN mode Onyxia uses its admin account on the services provider, with IMPERSONATE mode Onyxia request the API as the user (helm option --kube-as-user) but is only available if the helm version used is above 3.4.0 | |
| `expose` | | When users request to expose their service, only subdomain of this object domain are allowed | {domain: "kub.sspcloud.fr"} |
| `monitoring` | | Define the URL pattern of the monitoring service that is to be launch with each service. Only for client purpose. | {URLPattern: "https://$NAMESPACE-$INSTANCE.mymonitoring.sspcloud.fr"} |
| `cloudshell` | | Define the catalog and package name where to fetch the cloudshell in the helm catalog. | {catalogId: "inseefrlab-helm-charts-datascience", packageName: "cloudshell"} |
| `initScript` | | Define where to fetch a script that will be launched on some service on startup. | "https://inseefrlab.github.io/onyxia/onyxia-init.sh" |
| `allowedURIPattern` | "^https://" | Init scripts set by the user have to respect this pattern. | |
| `server` | | Define configuration of the services provider API server, this value is not served on the API as it contains credentials for the API. | See [server properties](###server-properties) |
| `k8sPublicEndpoint` | | Define external access to kubernetes API if available. It helps Onyxia users to directly connect to kubernetes outside the datalab | See [server properties](###k8sPublicEndpoint-properties) |
| `quotas` | | Properties setting quotas on how much resource a user can get on the services provider. | See [Quotas properties](###quotas-properties) |
| `defaultConfiguration` | | Default configuration on services that a user can override. For client purpose only. | See [Default Configuration](###default-configuration-properties) |

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
| `enabled` | false | Whether or not users are subject to a resouce limitation. Quotas can only be applied on user and not on group. |
| `allowUserModification` | true | Whether or not the user can manually disable or change its own limitation. |
| `defaultQuota` | | The quota applied on the namespace before user modification or on reset. |

A quota follows the kubernetes model which is composed of:
"requests.memory"
"requests.cpu"
"limits.memory"
"limits.cpu"
"requests.storage"
"count/pods"

### Default configuration properties

| Key | Default | Description |
| --------------------- | ------- | ------------------------------------------------------------------ |
| `IPProtection` | true | Whether or not the default behavior of the reverse-proxy serving the service is to block request from an ip other than the one from which it has been created. For client purpose only. |
| `networkPolicy` | true | Whether or not services can be reached by pods outside of the current namespace. For client purpose only. |
| `from` | NA | List of allowed source (kubernetes network policies format for from) to reach user http services. Used to allow ingress access to users services |
| `nodeSelector` | NA | This node selector can be injected in a service to restrain on which node it can be launched  |
| `tolerations` | NA | This node selector can be injected in a service to force it to run on nodes with this taint |
| `startupProbe` | NA | This startup probe can be injected in a service. It can help you in environment with slow network to specify a long duration before killing a container |
| `kafka` | | See [Kafka](####kafka) |

#### kafka

kafka can be used to get some events in users chart like hive metastore.

| Key | Default | Description |
| --------------------- | ------- | ------------------------------------------------------------------ |
| `URL` | N.A | brokerURL |
| `topicName` | N.A | topic name for those events |


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

### atlas

Atlas is a data management tool.

It can be used to add additionnal feature to the file explorer to transform it into a data explorer

| Key | Default | Description | Example |
| --------------------- | ------- | ------------------------------------------------------------------ | ---- |
| `URL` | | URL of the atlas service for the region. | "https://atlas.change.me" |
| `keycloakParams` | | Configuration of the keycloak service used to get an access token on the S3 service. It defines the keycloak realm, clientId, and Url. | {realm: "sspcloud", clientId: "atlas", URL: "https://auth.change.me/auth"} |

## Vault properties

It can be used to add additionnal feature to Onyxia. It helps user to keep their secret safe.

| Key | Default | Description | Example |
| --------------------- | ------- | ------------------------------------------------------------------ | ---- |
| `URL` | | URL of the atlas service for the region. | "https://vault.change.me" |
| `kvEngine` | | mount point of the kv engine. | "onyxia-kv" |
| `role` | | role of the user in vault | "onyxia-user" |
| `keycloakParams` | | Configuration of the keycloak service used to get an access token on the vault service. It defines the keycloak realm, clientId, and Url. | {realm: "sspcloud", clientId: "vault", URL: "https://auth.change.me/auth"} |

