package io.github.inseefrlab.helmwrapper.model;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.util.ArrayList;

public class HelmInstaller {

    private String name;

    private Object info;

    private Object chart;

    private Object config;

    private String manifest;

    private Object hooks;

    private String version;

    private String namespace;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getConfig() {
        return config;
    }

    public void setConfig(Object config) {
        this.config = config;
    }

    public Object getInfo() {
        return info;
    }

    public void setInfo(Object info) {
        this.info = info;
    }

    public Object getChart() {
        return chart;
    }

    public void setChart(Object chart) {
        this.chart = chart;
    }

    public String getManifest() {
        return manifest;
    }

    public ArrayList<Object> getParsedManifest() {
        ArrayList<Object> res = new ArrayList<>();
        Yaml yaml = new Yaml(new SafeConstructor());
        yaml.loadAll(manifest).forEach(file -> res.add(file));
        return res;
    }

    public Object getHooks() {
        return hooks;
    }

    public void setHooks(Object hooks) {
        this.hooks = hooks;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
}
