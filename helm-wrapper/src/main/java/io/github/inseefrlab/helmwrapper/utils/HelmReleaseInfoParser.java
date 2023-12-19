package io.github.inseefrlab.helmwrapper.utils;

import io.github.inseefrlab.helmwrapper.model.HelmReleaseInfo;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelmReleaseInfoParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(HelmReleaseInfoParser.class);

    private static final Pattern RELEASE_INFO_PATTERN =
            Pattern.compile(
                    "^NAME: (?<name>.*)\\RLAST DEPLOYED: (?<lastDeployed>.*)\\RNAMESPACE: (?<namespace>.*)\\RSTATUS: (?<status>.*)\\RREVISION: (?<revision>.*)\\RCHART: (?<chart>.*)\\RVERSION: (?<version>.*)\\RAPP_VERSION:(?<appVersion>.*)\\RUSER-SUPPLIED VALUES:\\R(?<userSuppliedValues>.*)COMPUTED VALUES:\\R(?<computedValues>.*)HOOKS:\\R(?<hooks>.*)MANIFEST:\\R(?<end>.*)?$",
                    Pattern.DOTALL);

    public HelmReleaseInfo parseReleaseInfo(String releaseInfo) throws IllegalArgumentException {
        Matcher matcher = RELEASE_INFO_PATTERN.matcher(releaseInfo);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Unparsable release info");
        }
        HelmReleaseInfo parsedReleaseInfo = new HelmReleaseInfo();
        parsedReleaseInfo.setName(matcher.group("name"));
        parsedReleaseInfo.setLastDeployed(matcher.group("lastDeployed"));
        parsedReleaseInfo.setNamespace(matcher.group("namespace"));
        parsedReleaseInfo.setStatus(matcher.group("status"));
        parsedReleaseInfo.setRevision(Integer.parseInt(matcher.group("revision")));
        parsedReleaseInfo.setChart(matcher.group("chart"));
        parsedReleaseInfo.setVersion(matcher.group("version"));
        parsedReleaseInfo.setAppVersion(StringUtils.trim(matcher.group("appVersion")));
        parsedReleaseInfo.setUserSuppliedValues(matcher.group("userSuppliedValues"));
        parsedReleaseInfo.setComputedValues(matcher.group("computedValues"));
        parsedReleaseInfo.setHooks(matcher.group("hooks"));
        String end = matcher.group("end");
        String[] splittedEnd = end.split(Pattern.quote("NOTES:"));
        if (splittedEnd.length > 1) {
            parsedReleaseInfo.setNotes(splittedEnd[0]);
            parsedReleaseInfo.setNotes(splittedEnd[1]);
        } else {
            parsedReleaseInfo.setManifest(end);
        }
        return parsedReleaseInfo;
    }
}
