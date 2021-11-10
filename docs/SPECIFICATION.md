# Onyxia API Specification
<p style="text-align: right;"> (back to <a href='./README.md'>Onyxia API Documentation</a>) <p>

This is a non-exhaustive documentation on the Onyxia API specification, refer to the Open API Specification to the current state of the Onyxia API.

Access the Open API Specification document on a running instance of Onyxia: [SSP Cloud - Swagger UI](https://datalab.sspcloud.fr/api/swagger-ui/index.html?configUrl=https://datalab.sspcloud.fr/api/v3/api-docs) with the specification url `https://datalab.sspcloud.fr/api/v3/api-docs`.

## Table of Contents
1. [Terminology](#terminology)
2. [Endpoints](#endpoints)
3. [Data model](#data-model)

## Terminology

The following documentation will have mentions to the different concepts with the subsequent terminology.  
- *Region*: the configuration block for the deployment of Onyxia, it was originally created when migrating Onyxia from Marathon (Mesos DC/OS) to Kubernetes.
- *Service*: a package that can be installed in behalf of an user, so that the later can freely use it as his own.
- *Task*: a component of the Service that runs the logic, i.e., translated to a Pod within Kuberenetes.
- *Catalog*: a compilation of Services that are available for launching by Onyxia.
- *Group*: the grouping of Services for a Project, which the underlying translation of the concept is a Kubernetes namespace where the services are launched.
- *Project*: the logical construct of a group of users, with a one-to-one relationship with a Group, i.e., each project has its own Kubernetes namespace.
- *Quota*: the request and limitation of resources per user (or group) namespace.


## Endpoints 
This endpoints specification is covered in the Open API Specification document accessible in the `/api/` endpoint. In this documentation we go into further detail with explanation of utility and backend processes.

### Headers
For authenticated requests the headers, besides the `Authorization` header, the `ONYXIA-REGION` and `ONYXIA-PROJECT` can be set:
- `ONYXIA-REGION`: defines which region the API should do requests to, defaults to the first region in the API configuration.
- `ONYXIA-PROJECT`: defines the namespace (Project) for the request, defaults to the user's personal project.


### /user/

The user route is for users to fetch their own information.

### /my-lab/
My Lab endpoints are available to authenticated users, offering a programmatic way to manage the available services in Onyxia. They endpoints routed by `fr.insee.onyxia.api.controller.api.mylab.MyLabController` allow users to:
- `PUT /my-lab/app` request a new service creation using the `helm-wrapper` in the backend to perform an install from one of the registered catalog
- `DELETE /my-lab/app?path=:string&bulk=:boolean` to either delete a specific service or do bulk delete of services in the namespace
- `GET /my-lab/app?serviceId=:string` get descriptions on running services
- `GET /my-lab/services` list the running services of a user, or of a project using `ONYXIA-PROJECT` header (*CURRENTLY groupId request param is not supported*)
- `GET /my-lab/app/logs?serviceId=:string&taskId=:string` get the raw logs of a pod.

The endpoints routed by `fr.insee.onyxia.api.controller.api.mylab.QuotaController`, are only functional if a quota feature is set on the Region, and allow users to:
- `GET /my-lab/quota` get their quota on the namespace, which is implemented by a quota resource with the name `onyxia-quota`
- `POST /my-lab/quota` change their quota on the namepsace, if quota changing is enabled
- `POST /my-lab/quota/reset` reset their quota to the default one on the namespace

### /onboarding/

The request to onboard process entails the creation of the Kubernetes namespace for the Group and the creation of a RoleBinding to that namespace giving the default ServiceAccount of that namespace admin permissions. There is a check in whether the user that made the request belongs to the group, based on the OIDC token, and the group onboarding process is triggered if so.

*NOTE: this onboarding process is being done behind the scenes when requesting a first service, and the endpoint use is not the default behaviour.*  

If no group is given in the body, it is assumed that it is a user onboard, i.e., a namespace for a single user rather than a Group (Project), and the creation of the namespace and RoleBinding remains the same. However, in the user use-case, there is also the possibility to set Quotas on the namespace during creation, and these are obtained in the Region definition configuration.


### /public/
Public endpoints are available to all, offering a programmatic way to comprehend the current configuration and the available services in Onyxia. They allow users to:
- `GET /public/regions` get the configuration of this Onyxia API instance Regions
- `GET /public/ip` get the IP addresses of the service caller if it exists on the forwarding headers, otherwise it returns the remote address of the servlet request
- `GET /public/healthcheck` *endpoint for machine-to-machine health check* 
- `GET /public/configuration` get the full configuration of this Onyxia API instance
- `GET /public/catalog` get catalogs associated with this Onyxia API instace
- `GET /public/catalog/:catalogId` get a complete catalog with all its packages, fetched by his id
- `GET /public/catalog/:catalogId/:packageName` get a package specification from a catalog, fetched by their ids

## Data model
### Class diagram
The relationship between the different request objects is represented in the following class diagram.

![failed-to-load-capt](./assets/onyxia-api-model-uml.jpg)

