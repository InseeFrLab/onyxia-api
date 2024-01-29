package io.github.inseefrlab.helmwrapper.model;

import java.util.ArrayList;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

public class HelmInstaller {

    private String name;

    private Object info;

    private Object chart;

    private Object config;

    private ArrayList<Object> manifest;

    private Object hooks;

    private String version;

    private String namespace;

    public String getName() {
        return name;
    }

    public Object getConfig() {
        return config;
    }

    public void setConfig(Object config) {
        this.config = config;
    }

    public void setName(String name) {
        this.name = name;
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

    public ArrayList<Object> getManifest() {
        return manifest;
    }

    public void setManifest(String manifest) {
        ArrayList<Object> res = new ArrayList<>();
        Yaml yaml = new Yaml(new SafeConstructor(new LoaderOptions()));
        yaml.loadAll(manifest).forEach(file -> res.add(file));
        this.manifest = res;
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
