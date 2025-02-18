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

### Authentication Configuration

Below is an overview of all available authentication-related configuration options.  
Scroll down for detailed examples for specific OIDC providers.

> **Note:** This is the OIDC configuration for Onyxia itself.  
> If you're looking for details on configuring OIDC-enabled services that Onyxia connects to  
> (e.g., S3, Vault, Kubernetes API), see the section at the end of this document.

`values.yaml`
```yaml
onyxia:
  api:
    env:
      # Mandatory and no other mode is currently supported.
      authentication.mode: "openidconnect"

      # Mandatory: The issuer URI of the OIDC provider.  
      # See specific examples below for commonly used OIDC providers.
      oidc.issuer-uri: "..."

      # Mandatory: The client ID of the OIDC client that represents the Onyxia Web Application.
      oidc.clientID: "..."

      # Mandatory: Defines which claim in the Access Token's JWT serves as the unique user identifier.  
      # This identifier must contain only lowercase alphanumeric characters and `-`. (`[a-z0-9-]+`)  
      #
      # - If your usernames conform to this constraint, you can use `"preferred_username"` 
      #   for a more human-readable identifier.
      # - If your usernames may contain special characters, use another claim like `"sub"` 
      #   (Make sure the `sub` values matches the above regex).  
      #
      oidc.username-claim: "..."

      # Optional: Defaults to `"groups"`. Defines which claim represents user groups.
      oidc.groups-claim: "..."

      # Optional: Defaults to `"roles"`. Defines which claim represents user roles.
      oidc.roles-claim: "..."

      # Optional: Additional query parameters to append to the OIDC provider login URL.  
      # Example: If using Keycloak with Google OAuth as an identity provider, you might want  
      # to preselect Google as the login option using `"kc_idp_hint=google"`.  
      # 
      # ⚠️ This string is appended as-is. Ensure it's properly URI-encoded.  
      # If adding multiple parameters, separate them with `&`.  
      # Example: `"foo=foo%20value&bar=bar%20value"`
      oidc.extra-query-params: "..."

      # Optional: Specifies the expected audience value in the Access Token.  
      # If provided, Onyxia will validate the `aud` claim in the token and reject requests  
      # where it does not match.
      oidc.audience: "..."

      # Optional: Specifies the OIDC scopes requested by the Onyxia client.  
      # Defaults to `"openid profile"`.  
      # This is a space-separated list. `"openid"` is always requested, regardless of this setting.
      oidc.scope: "..."

      # Onyxia API fetches `<issuer-uri>/.well-known/openid-configuration` to retrieve JWKs  
      # for validating Access Tokens (used as Authorization Bearers).  
      #
      # ⚠️ In development, if you lack proper root certificates, you can disable TLS verification.  
      # However, in production, it's recommended to mount the correct `cacerts` instead.
      oidc.skip-tls-verify: "true|false"
```

---

#### Authentication Configuration: Using Keycloak as the OIDC Provider

For detailed Keycloak-specific instructions, refer to the  
[Keycloak Configuration Guide](https://docs.oidc-spa.dev/resources/keycloak-configuration).

You can also refer to [the official Onyxia installation guide](https://docs.onyxia.sh/admin-doc/readme/user-authentication).

`values.yaml`
```yaml
onyxia:
  api:
    env:
      authentication.mode: "openidconnect"
      # Example values if following the official installation guide:
      # - <KC_DOMAIN> = auth.lab.my-domain.net (replace my-domain.net with your actual domain)
      # - <KC_RELATIVE_PATH> = /auth
      # - <REALM> = datalab
      #
      # Expected result: "https://auth.lab.my-domain.net/auth/realms/datalab"
      oidc.issuer-uri: "https://<KC_DOMAIN><KC_RELATIVE_PATH>/realms/<REALM_NAME>"

      # Default "onyxia" if following the official installation guide.
      oidc.clientID: "<ONYXIA_CLIENT_ID>"

      # Use "preferred_username" if your Keycloak realm enforces a username regex constraint  
      # (as per the Onyxia Installation Guide).  
      # Otherwise, use `"sub"` if integrating with an existing Keycloak instance.
      oidc.username-claim: "preferred_username"
```

---

#### Authentication Configuration: Using Google OAuth as the OIDC Provider

For detailed Google OAuth-specific instructions, refer to the  
[Google OAuth Configuration Guide](https://docs.oidc-spa.dev/resources/google-oauth).

- **Authorized Redirect URIs:** The home URL of your Onyxia instance (e.g., `https://datalab.my-domain.net/`)
- **Authorized JavaScript Origins:** Example `https://datalab.my-domain.net`

`values.yaml`
```yaml
onyxia:
  api:
    env:
      authentication.mode: "openidconnect"
      # Always use this exact issuer URI:
      oidc.issuer-uri: "https://accounts.google.com"

      # Example client ID format:
      # "000000000000-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx.apps.googleusercontent.com"
      oidc.clientID: "<Client ID>"

      # ⚠️ Do **not** use `"email"` or other claims that may contain special characters (e.g., `@`).  
      # Use `"sub"` instead.
      oidc.username-claim: "sub"

      # Example client secret format: "GOCSPX-_xxxxxxxxxxxxxxxxxxxxxxxxxxx"
      #
      # ⚠️ Google OAuth requires providing a client secret for public clients,  
      # even though it has no security implications.
      # While this feels misleading, it is how Google expects it to be configured.
      clientSecret: "<Client Secret>"
```

---

#### Authentication Configuration: Using Microsoft Entra ID as the OIDC Provider

Microsoft Entra ID requires additional configuration to behave like a standard OIDC provider.  
By default, it issues opaque access tokens that cannot be validated without calling the Microsoft Graph API.  

Follow the steps in the [Microsoft Entra ID Configuration Guide](https://docs.oidc-spa.dev/resources/entraid)  
to configure Entra ID correctly.

`values.yaml`
```yaml
onyxia:
  api:
    env:
      authentication.mode: "openidconnect"
      # <Directory (tenant) ID> should look like: "71a0a621-363a-4182-8209-86364aa6de03"
      oidc.issuer-uri: "https://login.microsoftonline.com/<Directory (tenant) ID>/v2.0"

      # <Application (client) ID> should look like: "ea067b46-d482-4d5e-b1b4-61d3dbf9527c"
      oidc.clientID: "<Application (client) ID>"

      # ⚠️ Do **not** use `"sub"` or `"upn"` since they may contain non-alphanumeric characters.  
      oidc.username-claim: "oid"

      # Provide a custom scope as explained in the guide to ensure the access token is a valid JWT.
      # Example format: `"api://onyxia-api/default"`
      oidc.scope: "<Custom Scope>"
```

---

#### OIDC Configuration for Services Onyxia Connects To

Onyxia uses an OIDC client for authentication, but it also connects to other OIDC-enabled services.  
Each of these services **can** have its own OIDC configuration, allowing Onyxia to authenticate  
using a separate client identity.

In the **region configuration**, you can specify an optional `oidcConfiguration` object for  
each service:

- **S3 (MinIO STS)** → `onyxia.api.regions[].data.S3.sts.oidcConfiguration`
- **Vault** → `onyxia.api.regions[].vault.oidcConfiguration`
- **Kubernetes API** → `onyxia.api.regions[].services.k8sPublicEndpoint.oidcConfiguration`

Each configuration follows this structure:

```ts
type OidcConfiguration = {
    issuerURI?: string;
    clientID?: string;
    extraQueryParams?: string;
    scope?: string[];
    clientSecret?: string; // WARNING: Only for Google OAuth
};
```

If no `oidcConfiguration` is provided for a service, Onyxia will **reuse its own OIDC client**  
and the same Access Token for authentication. However, it is **recommended** to provide  
a **separate client ID** for each service to improve access control and security.

Example configuration in `values.yaml`:

```yaml
onyxia:
  api:
    env:
      authentication.mode: "openidconnect"
      oidc.issuer-uri: "https://auth.lab.my-domain.net/auth/realms/datalab"
      oidc.clientID: "onyxia"
    regions: 
      [
        {
          data: {
            S3: {
              sts: {
                oidcConfiguration: {
                  clientID: "onyxia-minio",
                }
              }
            }
          },
          vault: {
            oidcConfiguration: {
              clientID: "onyxia-vault"
            }
          },
          services: {
            k8sPublicEndpoint: {
              oidcConfiguration: {
                clientID: "onyxia-k8s"
              }
            }
          }
        }
      ]
```

⚠ ️ Important: Consistency of Claims Across Services:  

When configuring OIDC for Onyxia, you define specific claims that indicate where to find  
the **user identifier**, **groups**, and **roles** within the Access Token's JWT.  

These claims **cannot be configured separately for each service** Onyxia interacts with (e.g., S3, Vault, Kubernetes API).  
They must remain **consistent across all OIDC-enabled services** to ensure proper authentication and authorization.


When a user logs in, the OIDC provider issues an Access Token for the `onyxia` client.  
This token includes claims such as:

```json
{
  "sub": "abcd1234",
  "preferred_username": "jhondoe",
  "groups": [ "funathon", "spark-lab" ],
  "roles": [ "vip", "admin-keycloak" ],
}
```

If you have configured `oidc.username-claim: "preferred_username"` in the main Onyxia configuration,  
Onyxia expects that all other services it interacts with—such as `onyxia-minio`, `onyxia-vault`, and `onyxia-k8s`—  
will also receive Access Tokens where the **same claim (`preferred_username`) exists and holds the same value**.

To avoid any issue, **all OIDC clients** (`onyxia`, `onyxia-minio`, `onyxia-vault`, `onyxia-k8s`)  
should be configured within **the same SSO realm** in your OIDC provider.  
This ensures that each issued Access Token follows the same claim structure and contains  
consistent values for the same user.

If you're unsure whether your setup meets this requirement, **check the JWT of each Access Token**  
issued for different clients and confirm that the claims are aligned.

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
