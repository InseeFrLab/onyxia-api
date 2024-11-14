package fr.insee.onyxia.model.views;

public class Views {

    // A general view for all fields except deep-nested charts
    public static class General {}

    // A specific view to include only the first chart in the nested structure
    public static class FirstChartOnly extends General {}

    public static class Full extends General {}
}
