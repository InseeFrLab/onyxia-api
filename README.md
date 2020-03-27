# Onyxia API

This app is used at Insee (https://insee.fr).  
The opensourcing (and documentation) is still a work in progress, please be patient :)

## Quick start

### Using docker

```
docker run -p 8080:8080 inseefrlab/onyxia-api
```

### Using Java (maven)

```
mvn spring-boot:run
```

## Configuration

Main configuration file is [onyxia-api/src/main/resources/application.properties](onyxia-api/src/main/resources/application.properties).  
Each variable can be overriden using env variable.

## Onyxia Universe package format extension

Onyxia extends the official Universe format (see https://github.com/mesosphere/universe) to enhance it.  
This format extension is **fully interoperable** with the official Universe format meaning **Onyxia works with any Universe** and **Universes using Onyxia's extension should be usable in other apps**.

The specification is defined [here](docs/specification/README.md).

An example of a Universe using this extension is available [here](https://github.com/inseefrlab/Universe-Datascience).
