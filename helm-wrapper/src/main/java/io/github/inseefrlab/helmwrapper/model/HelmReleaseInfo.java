package io.github.inseefrlab.helmwrapper.model;

public class HelmReleaseInfo {

    private String name;
    private String lastDeployed;
    private String namespace;
    private String status;
    private int revision;
    private String chart;
    private String version;
    private String appVersion;
    private String userSuppliedValues;
    private String computedValues;
    private String hooks;
    private String manifest;
    private String notes;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastDeployed() {
        return lastDeployed;
    }

    public void setLastDeployed(String lastDeployed) {
        this.lastDeployed = lastDeployed;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getRevision() {
        return revision;
    }

    public void setRevision(int revision) {
        this.revision = revision;
    }

    public String getChart() {
        return chart;
    }

    public void setChart(String chart) {
        this.chart = chart;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getUserSuppliedValues() {
        return userSuppliedValues;
    }

    public void setUserSuppliedValues(String userSuppliedValues) {
        this.userSuppliedValues = userSuppliedValues;
    }

    public String getComputedValues() {
        return computedValues;
    }

    public void setComputedValues(String computedValues) {
        this.computedValues = computedValues;
    }

    public String getHooks() {
        return hooks;
    }

    public void setHooks(String hooks) {
        this.hooks = hooks;
    }

    public String getManifest() {
        return manifest;
    }

    public void setManifest(String manifest) {
        this.manifest = manifest;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
