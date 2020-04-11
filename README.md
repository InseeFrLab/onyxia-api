# Onyxia API

This app is used at Insee (https://insee.fr).  
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

### Using Helm (experimental)  

An experimental [helm](helm.sh) package is available at [inseefrlab](https://github.com/InseeFrLab/helm-charts)

```
helm repo add inseefrlab https://inseefrlab.github.io/helm-charts
helm install inseefrlab/onyxia
```  

Note that this install both the API and the [UI](https://github.com/inseefrlab/onyxia-ui)

## Configuration

Main configuration file is [onyxia-api/src/main/resources/application.properties](onyxia-api/src/main/resources/application.properties).  
Each variable can be overriden using environment variables.  



## Onyxia Universe package format extension

Onyxia extends the official Universe format (see https://github.com/mesosphere/universe) to enhance it.  
This format extension is **fully interoperable** with the official Universe format meaning **Onyxia works with any Universe** and **Universes using Onyxia's extension should be usable in other apps**.

The specification is defined [here](docs/specification/README.md).

An example of a Universe using this extension is available [here](https://github.com/inseefrlab/Universe-Datascience).
