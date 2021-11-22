# Onyxia API Documentation

Onyxia's API is stateless in itself, using Keycloak for users management and helm for infrastructure management. It was built using [Spring Framework](https://spring.io/), it is recommended to use [Maven](https://maven.apache.org/) to test and build the application, given the already generated `pom.xml` files. 

The Onyxia API documentation is in development and divided into the following sections:
- [Onyxia API Documentation](./README.md)
- [Onyxia API Architecture](./ARCHITECTURE.md)
- [Onyxia API Specification](./SPECIFICATION.md)

## :warning: Disclaimer :warning:
The documentation is still in development.

## Main Routes

Onyxia API has two main routes of interaction:
- `/my-lab/`: which handles authenticated requests to the Data Lab itself running on Kubernetes. For example, to request a service to be launched in a self-service basis, or to delete an owned running service.
- `/public/`: which handles public requests from unauthenticated users, providing information that is not protected. For example, listing a catalog service (package) offerings.

### Example: Request a Service
This request is usually done through the Onyxia Web interface. However, if you're an authenticated user with an access token and desire to do it programatically, you can also do it through API calls.

To request a service, it is necessary to indicate a which service package we want to install from a catalog. So a normal flow would be to, first know the existing catalogs:
```
GET /public/catalog
```
Then from the catalog select a service package by its `name`, and provide the configuration `options` following the catalog specification on your service request. For example, requesting a `jupyter` service with the default configurations would be done as follows:
```
PUT /my-lab/app
```
```json
{
  "catalogId": "inseefrlab-helm-charts-datascience",
  "packageName": "jupyter",
  "packageVersion": "4.0.0",
  "name": "jupyter",
  "options": {},
  "dryRun": false
}
```