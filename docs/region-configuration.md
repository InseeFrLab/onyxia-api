# Region configuration  

A **region** is the configuration of an independent set of Onyxia services. Thus multiple configurations accessing different services can be plugged into a single Onyxia instance.

A region mainly defines **Onyxia service provider** on which are run the users, groups, and global services and how users can interact with it. It also defines an **S3 object storage** and how "buckets" are provided to users.

Most of the configuration of an Onyxia client comes from the region that can be accessed as JSON via /public/configuration or /public/regions.

See [regions.json](/onyxia-api/src/main/resources/regions.json) for a complete example of regions configuration.

- [Region configuration](#region-configuration)
  - [Main region properties](#main-region-properties)
  - [Services properties](#services-properties)
    - [CustomInitScript properties](#custominitscript-properties)
    - [Server properties](#server-properties)
    - [K8sPublicEndpoint properties](#k8spublicendpoint-properties)
    - [Quotas properties](#quotas-properties)
    - [Expose properties](#expose-properties)
      - [istio](#istio)
      - [CertManager](#certManager)
    - [Default configuration properties](#default-configuration-properties)
      - [Kafka](#kafka)
      - [Sliders](#sliders)
      - [Resources](#resources)
  - [Data properties](#data-properties)
    - [S3](#s3)
    - [Atlas](#atlas)
  - [Vault properties](#vault-properties)
  - [Git properties](#git-properties)
  - [ProxyConfiguration properties](#proxyconfiguration-properties)
  - [ProxyInjection properties](#proxyinjection-properties)
  - [PackageRepositoryInjection properties](#packagerepositoryinjection-properties)
  - [CertificateAuthorityInjection properties](#certificateauthorityinjection-properties)

## Main region properties

| Key                     | Description                                                                                                                                                                                                                                                                                            | Example                                               |
|-------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------|
| `id`                    | Unique name of the region                                                                                                                                                                                                                                                                              | "mycloud"                                             |
| `name`                  | Descriptive name for the region                                                                                                                                                                                                                                                                        | "mycloud region"                                      |
| `description`           | Description of the region                                                                                                                                                                                                                                                                              | "This region is in an awesome cloud"                  |
| `location`              | Geographical position of the data center on which the region is supposed to run.                                                                                                                                                                                                                       | {lat: 48.864716, longitude: 2.349014, name: "Paris" } |
| `includedGroupPattern`  | Pattern of user groups considered for the user in the region. Patterns are case-sensitive.                                                                                                                                                                                                             | ".*_Onyxia"                                           |
| `excludedGroupPattern`  | Pattern of user groups that will not be considered for the user in the region. Patterns are case-sensitive.                                                                                                                                                                                            | ".*_BadGroup"                                         |
| `transformGroupPattern` | Indicate how to transform a group based on `includedGroupPattern` to make a project name used for a namespace or S3 bucket for example. For example with an `includedGroupPattern` of "(.*)_Onxyia" and a `transformGroupPattern` of "$1-k8s", a mygroup_Onyxia will generate a mygroup-k8s namespace. | "$1-k8s"                                              |
| `onyxiaAPI`             | Contains the base url of an onyxia api                                                                                                                                                                                                                                                                 | {baseURL: "http://localhost:8080"}                    |
| `services`              | Configuration of Onyxia services provider platform                                                                                                                                                                                                                                                     | See [Services properties](#services-properties)       |
| `data`                  | Configuration of the S3 Object Storage                                                                                                                                                                                                                                                                 | See [S3](#data-properties)                            |
| `vault`                 | Configuration of the Vault API                                                                                                                                                                                                                                                                         | See [Vault properties](#vault-properties)             |
| `certManager`           | Configuration on the use of CertManager                                                                                                                                                                                                                                                                | See [CertManager properties](#certManager-properties)  |

## Services properties

The Onyxia service platform is a Kubernetes cluster but Onyxia is meant to be extendable to other types of a platform if necessary.

Users can work on Onyxia as a User or as a Group to which they belong. Each user and group can have its own **namespace** which is an isolated space of Kubernetes.

| Key | Default | Description | Example |
| --------------------- | ------- | ------------------------------------------------------------------ | ---- |
| `type` | | Type of the platform on which services are launched. Only Kubernetes is supported, Marathon has been removed. | "KUBERNETES" |
| `allowNamespaceCreation` | true | If true, the /onboarding endpoint is enabled and the user will have a namespace created on its first request on a service resource. | true |
| `namespaceLabels` |  | Labels to add at namespace creation | {"zone":"prod"} |
| `namespaceAnnotations` |  | Annotations to add at namespace creation | {"zone":"prod"} |
| `singleNamespace` | true | When true, all users share the same namespace on the service provider. This configuration can be used if a project works on its own Onyxia region. | |
| `userNamespace` | true | When true, all users have a namespace for their work. This configuration can be used if you don't allow a user to have their own space to work and only use project space | |
| `namespacePrefix` | "user-" | User has a personal namespace like namespacePrefix + userId (should only be used when not singleNamespace but not the case) | |
| `groupNamespacePrefix` | "projet-" | User in a group groupId can access the namespace groupeNamespacePrefix + groupId. This prefix is also used for the Vault group directory. | |
| `usernamePrefix` | | If set, the Kubernetes user corresponding to the Onyxia user is named usernamePrefix + userId on impersonation mode, otherwise it is identified only as userId | "user-" |
| `groupPrefix` | | not used | |
| `authenticationMode` | serviceAccount | serviceAccount, impersonate or tokenPassthrough : on serviceAccount mode Onyxia API uses its own serviceAccount (by default admin or cluster-admin), with impersonate mode Onyxia requests the API with user's permissions (helm option `--kube-as-user`). With tokenPassthrough, the authentication token is passed to the API server. | |
| `expose` | | When users request to expose their service, only subdomain of this object domain are allowed | See [Expose properties](#expose-properties) |
| `monitoring` | | Define the URL pattern of the monitoring service that is to be launched with each service. Only for client purposes. | {URLPattern: "https://$NAMESPACE-$INSTANCE.mymonitoring.sspcloud.fr"} |
| `initScript` | | Define where to fetch a script that will be launched on some service on startup. | "https://inseefrlab.github.io/onyxia/onyxia-init.sh" |
| `allowedURIPattern` | "^https://" | Init scripts set by the user have to respect this pattern. | |
| `server` | | Define the configuration of the services provider API server, this value is not served on the API as it contains credentials for the API. | See [Server properties](#server-properties) |
| `k8sPublicEndpoint` | | Define external access to Kubernetes API if available. It helps Onyxia users to directly connect to Kubernetes outside the datalab | See [K8sPublicEndpoint properties](#k8sPublicEndpoint-properties) |
| `quotas` | | Properties setting quotas on how many resources a user can get on the services provider. | See [Quotas properties](#quotas-properties) |
| `defaultConfiguration` | | Default configuration on services that a user can override. For client purposes only. | See [Default Configuration](#default-configuration-properties) |
| `customInitScript` | | This can be used to customize user environments using a regional script executed by some users' pods. | See [CustomInitScript properties](#custom-init-script-properties) |
| `customValues` | | This can be used to specify custom values that will be available for helm chart injection in the web app. Nested values are supported. | ` "customValues": {"myCustomKey": "myValue", "myNestedCustomKey": {"nestedKey": "nestedValue"} }` |

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
| `auth` | Credentials for the service provider API. | {token: "ey...", password: "pwd", username: "admin"} |

### K8sPublicEndpoint properties

It can be used to add additional features to Onyxia. It helps Onyxia users to directly connect to Kubernetes outside the datalab.

| Key | Default | Description | Example |
| --------------------- | ------- | ------------------------------------------------------------------ | ---- |
| `URL` | | public URL of the Kubernetes API of the region. | "https://vault.change.me" |
| `oidcConfiguration` | | Allow override of openidconnect authentication for this specific service. If not defined then global Onyxia authentication will be used. | {clientID: "onyxia", issuerURI: "https://auth.lab.sspcloud.fr/auth"} |

### Quotas properties

When this feature is enabled, namespaces are created with **quotas**.

| Key | Default | Description |
| --------------------- | ------- | ------------------------------------------------------------------ |
| `enabled` | false | Whether or not users are subject to a resource limitation. Quotas can only be applied to users and not to groups. (will be deprecated see userEnabled and groupEnabled) |
| `allowUserModification` | true | Whether or not the user can manually disable or change its own limitation or group limitation. |
| `default` | | This quota is applied on the namespace at creation, before user modification or reset. New configuration will not be applied to existing namespaces. (will be deprecated see userEnabled and groupEnabled) |
| `userEnabled` | false | Whether or not users are subject to a resource limitation. Enable this on user namespace only with user quota content based on kubernetes model . |
| `user` | false | This quota is applied on the user namespace at creation, before user modification or reset. New configuration will not be applied to already existing namespaces. |
| `groupEnabled` | false |Whether or not users are subject to a resource limitation. Enable this on group/project namespace only ith group quota content. |
| `group` | false | This quota is applied on the group namespace at creation, before user modification or reset. New configuration will not be applied to already existing namespaces. |

A quota follows the Kubernetes model which is composed of:
"requests.memory"
"requests.cpu"
"limits.memory"
"limits.cpu"
"requests.storage"
"count/pods"
"requests.ephemeral-storage"
"limits.ephemeral-storage"
"requests.nvidia.com/gpu"
"limits.nvidia.com/gpu"

### Expose properties

 with **expose**.

| Key                     | Default | Description                                                                                                                 |
|-------------------------|-------|-----------------------------------------------------------------------------------------------------------------------------|
| `domain`                |       | When users request to expose their service, only the subdomain of this object will be created.                              |
| `ingress`               | true  | Whether or not Kubernetes Ingress is enabled                                                                                |
| `route`                 | false | Whether or not OpenShift Route is enabled                                                                                   |
| `istio`                 |       | See [Istio](#istio)                                                                                                         |
| `ingressClassName`      | ''    | Ingress Class Name: useful if you want to use a specific ingress controller instead of a default one                        |
| `annotations`           |       | Annotations to add at ingress creation {"cert-manager.io/cluster-issuer": "nameOfClusterIssuer"}                            |
| `useDefaultCertificate` | true  | When true, no TLS secret name will be generated, specify false if you want ingress certificate to be managed by CertManager |
| `certManager`           |       | See [CertManager](#certManager)                                                                                             |


#### istio

| Key        | Default | Description                                                                                                 |
|------------|--------|--------------------------------------------------------------------------------------------------------------|
| `enabled`  | false  | Whether or not Istio is enabled                                                                              |
| `gateways` | []     | List of istio gateways to be used. Should contain at least one element. E.g. `["istio-system/my-gateway"]`   |



### Default configuration properties

| Key | Default | Description |
| --------------------- | ------- | ------------------------------------------------------------------ |
| `IPProtection` | false | Whether or not the default behavior of the reverse proxy serving the service is to block a request from an IP other than the one from which it has been created. For client purposes only. |
| `networkPolicy` | false | Whether or not services can be reached by pods outside of the current namespace. For client purposes only. |
| `from` | NA | List of allowed sources (Kubernetes network policies format for from) to reach user HTTP services. Used to allow ingress access to users' services |
| `nodeSelector` | NA | This node selector can be injected in a service to restrain on which node it can be launched  |
| `tolerations` | NA | This node selector can be injected in a service to force it to run on nodes with this taint |
| `startupProbe` | NA | This startup probe can be injected into a service. It can help you in an environment with a slow network to specify a long duration before killing a container |
| `kafka` | | See [Kafka](#kafka) |
| `sliders` | | See [Sliders](#sliders) |
| `Resources` | | See [Resources](#resources) |

#### Kafka

Kafka can be used to get some events in the user chart like Hive metastore.

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
| `cpuRequest` | N.A | overwrite default CPU request if asked by helm-charts |
| `cpuLimit` | N.A | overwrite default CPU limit if asked by helm-charts |
| `memoryRequest` | N.A | overwrite default memory request if asked by helm-charts |
| `memoryLimit` | N.A | overwrite default memory limit if asked by helm-charts |
| `disk` | N.A | overwrite default disk size if asked by helm-charts |
| `gpu` | N.A | overwrite default GPU if asked by helm-charts |


## Data properties

### S3

Configuration parameters for integrating your Onyxia service with S3.
This part of the documentation is provided as a commented type definition.  
When a property has a `?` it means that it's optional, you don't have to provide it.  

```ts
type Region = {
  // ...
  data: {
    S3: {

      /**
       * The URL of the S3 server.
       * Examples: "https://minio.lab.sspcloud.fr" or "https://s3.amazonaws.com".
       */
      URL: string;

      /**
       * The AWS S3 region. This parameter is optional if you are configuring
       * integration with a MinIO server.
       * Example: "us-east-1"
       */
      region?: string;

      /**
       * This parameter informs Onyxia how to format file download URLs for the configured S3 server.
       * Default: true
       * 
       * Example:  
       * Assume "https://minio.lab.sspcloud.fr" as the value for region.data.S3.URL.  
       * For a file "a/b/c/foo.parquet" in the bucket "user-bob":
       * 
       * With pathStyleAccess set to true, the download link will be:
       *   https://minio.lab.sspcloud.fr/user-bob/a/b/c/foo.parquet
       * 
       * With pathStyleAccess set to false (virtual-hosted style), the link will be:
       *   https://user-bob.minio.lab.sspcloud.fr/a/b/c/foo.parquet
       * 
       * For MinIO, pathStyleAccess is typically set to true.
       * For Amazon Web Services S3, is has to be set to false.
       */
      pathStyleAccess?: boolean;

      /**
       * Defines where users are permitted to read/write S3 files, 
       * specifying the allocated storage space in terms of bucket and object name prefixes.
       * 
       * Mandatory unless data.S3.sts is not defined then it's optional.
       *
       * Example: 
       * For a user "bob" in the "exploration" group, using the configuration:
       * 
       * Single bucket mode:
       *   "workingDirectory": {
       *       "bucketMode": "single",
       *       "bucketName": "onyxia",
       *       "prefix": "user-",
       *       "prefixGroup": "project-"
       *   }
       * 
       * In this configuration Onyxia will assumes that Bob has read/write access to objects starting 
       * with "user-bob/" and "project-exploration/" in the "onyxia" bucket.  
       * 
       * Multi bucket mode:
       *   "workingDirectory": {
       *       "bucketMode": "multi",
       *       "bucketNamePrefix": "user-",
       *       "bucketNamePrefixGroup": "project-",
       *   }
       * 
       * In this configuration Onyxia will assumes that Bob has read/wite access to the entire
       * "user-bob" and "project-exploration" buckets.  
       * 
       * If STS is enabled and a bucket doesn't exist, Onyxia will try to create it.
       */
      workingDirectory?: {
        bucketMode: "shared";
        bucketName: string;
        prefix: string;
        prefixGroup: string;
      } | {
        bucketMode: "multi";
        bucketNamePrefix: string;
        bucketNamePrefixGroup: string;
      };

      /**
       * Configuration for Onyxia to dynamically request S3 tokens on behalf of users.
       * Enabling S3 allows users to avoid manual configuration of a service account via the Onyxia interface.
       */
      sts?: {
        /**
         * The STS endpoint URL of your S3 server.
         * For integration with MinIO, this property is optional as it defaults to region.data.S3.URL.
         * For Amazon Web Services S3, set this to "https://sts.amazonaws.com".
         */
        URL?: string;

        /**
         * The duration for which temporary credentials are valid.
         * AWS: Maximum of 43200 seconds (12 hours).
         * MinIO: Maximum of 604800 seconds (7 days).
         * Without this parameter, Onyxia requests 7-day validity, subject to the S3 server's policy limits.
         */
        durationSeconds?: number;

        /**
         * Optional parameter to specify RoleARN and RoleSessionName for the STS request.
         * 
         * Example:  
         *   "role": {
         *     "roleARN": "arn:aws:iam::123456789012:role/onyxia",
         *     "roleSessionName": "onyxia"
         *   }
         */
        role?: {
          roleARN: string;
          roleSessionName: string;
        };

        /**
         * OIDC configuration. Defaults to Onyxia API's configuration if unspecified.
         * If only the ClientID is provided, the issuer URI defaults to the Onyxia API's configuration.
         * 
         * Example:
         *   "oidcConfiguration": {
         *     "clientID": "onyxia-minio"
         *   }
         */
        oidcConfiguration?: {
          issuerURI?: string;
          clientID: string;
        };

      };

    };
  };
};
```

### Atlas

Atlas is a data management tool.

It can be used to add additional features to the file explorer to transform it into a data explorer

| Key | Default | Description | Example |
| --------------------- | ------- | ------------------------------------------------------------------ | ---- |
| `URL` | | URL of the atlas service for the region. | "https://atlas.change.me" |
| `oidcConfiguration` | | Allow override of openidconnect authentication for this specific service. If not defined then global Onyxia authentication will be used. | {clientID: "onyxia", issuerURI: "https://auth.lab.sspcloud.fr/auth"} |

## Vault properties

It can be used to add additional features to Onyxia. It helps users to keep their secrets safe.

| Key | Default | Description | Example |
| --------------------- | ------- | ------------------------------------------------------------------ | ---- |
| `URL` | | URL of the vault service for the region. | "https://vault.change.me" |
| `kvEngine` | | mount point of the kv engine. | "onyxia-kv" |
| `role` | | role of the user in vault | "onyxia-user" |
| `authPath` | "jwt" | path of the jwt auth method. | "jwt" |
| `oidcConfiguration` | | Allow override of openidconnect authentication for this specific service. If not defined then global Onyxia authentication will be used. | {clientID: "onyxia", issuerURI: "https://auth.lab.sspcloud.fr/auth"} |

## CertManager

It can be used to generate a certManager certificate.

| Key                        | Default | Description                                                                                       |
|----------------------------|---------|---------------------------------------------------------------------------------------------------|
| `useCertManager`           | false   | When true, a secret name will be generated and ingress certificate will be managed by CertManager |
| `certManagerClusterIssuer` | ""      |                                                                                                   |

## Git properties

It can be used to add additional features to Onyxia. It helps users to keep their code safe.

| Key | Default | Description | Example |
| --------------------- | ------- | ------------------------------------------------------------------ | ---- |
| `type` | | Type of Git implementation. | "gitlab", "github" |
| `URL` | | URL of the git service for the region. | "https://git.change.me" |
| `oidcConfiguration` | | Allow override of openidconnect authentication for this specific service. If not defined then global Onyxia authentication will be used. | {clientID: "onyxia", issuerURI: "https://auth.lab.sspcloud.fr/auth"} |

## ProxyConfiguration properties

It can be used to inject proxy configuration in the services, if the helm chart in the catalog allows it you can bind this value to the Helm chart value to override for example HTTP_PROXY, HTTPS_PROXY and NO_PROXY en variable in the pod launched.

| Key | Default | Description | Example |
| --------------------- | ------- | ------------------------------------------------------------------ | ---- |
| `httpProxyUrl` | | URL of the enterprise proxy for the region for HTTP. | "http://proxy.enterprise.com:8080" |
| `httpsProxyUrl` | | URL of the enterprise proxy for the region for HTTPS. | "http://proxy.enterprise.com:8080" |
| `noProxy` | | enterprise local domain that should not take proxy comma separated | "corporate.com" |

## ProxyInjection properties

It can be used to inject proxy settings in the services, if the Helm chart in the catalog allows it you can bind this value to the Helm chart value to override for example HTTP_PROXY, HTTPS_PROXY, and NO_PROXY to variables in the pod launched.

| Key | Default | Description | Example |
| --------------------- | ------- | ------------------------------------------------------------------ | ---- |
| `httpProxyUrl` | | URL of the enterprise proxy for the region for HTTP. | "http://proxy.enterprise.com:8080" |
| `httpsProxyUrl` | | URL of the enterprise proxy for the region for HTTPS. | "http://proxy.enterprise.com:8080" |
| `noProxy` | | enterprise local domain that should not take proxy comma separated | "corporate.com" |

## PackageRepositoryInjection properties

It can be used to inject the package repository in the services, if the Helm chart in the catalog allows it you can bind this value to the Helm chart value to override for example the CRAN, PyPI and Conda repositories to reach some local enterprise repository on the network.

| Key | Default | Description | Example |
| --------------------- | ------- | ------------------------------------------------------------------ | ---- |
| `cranProxyUrl` | | URL of enterprise local cran repository. | "https://cranProxy" |
| `condaProxyUrl` | | URL of enterprise local Conda repository. | "https://condaProxyUrl" |
| `packageManagerUrl` | | URL of the packagemanager. | "https://packagemanager.posit.co/cran/__linux__" or "https://packagemanagerUrl.internal/cran/__linux__" will be in first position |
| `pypiProxyUrl` | | URL of enterprise local PyPI repository. | "https://pypiProxyUrl" |

## CertificateAuthorityInjection properties

It can be used to inject certificate authority into the services, if the Helm chart in the catalog allows it you can bind this value to the Helm chart value to add some certificate authorities in the pod.

| Key | Default | Description | Example |
| --------------------- | ------- | ------------------------------------------------------------------ | ---- |
| `crts` | | List of encodedbase64 crt. |  Deprecated for cacerts|
| `cacerts` | | String of crts concatenated in base64 |  LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUZTRENDQkRDZy4uLi4uLi4uLi4uLi4uLi4uLi4uLi4uLi4uLi4uLi4uLi4uLi4uLi4KLS0tLS1FTkQgQ0VSVElGSUNBVEUtLS0tLQotLS0tLUJFR0lOIENFUlRJRklDQVRFLS0tLS0KTUlJRlNEQ0NCRENnYW5vdGhlcm9uZS4uLi4uLi4uLi4uLi4uLi4uLi4uLi4uLi4uLi4uLi4uLi4uLi4uLi4KLS0tLS1FTkQgQ0VSVElGSUNBVEUtLS0tLQ==|
| `pathToCaBundle` | | String path where a bundle is made or injected by third party solution | /etc/ssl/certs/ca-certificates.crt|
