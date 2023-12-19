package io.github.inseefrlab.helmwrapper.utils;

import io.github.inseefrlab.helmwrapper.model.HelmReleaseInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HelmReleaseInfoParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(HelmReleaseInfoParser.class);

    private static final Pattern RELEASE_INFO_PATTERN = Pattern.compile("^NAME: (?<name>.*)\\RLAST DEPLOYED: (?<lastDeployed>.*)\\RNAMESPACE: (?<namespace>.*)\\RSTATUS: (?<status>.*)$",Pattern.DOTALL);

    public HelmReleaseInfo parseReleaseInfo(String releaseInfo) throws IllegalArgumentException {
        Matcher matcher = RELEASE_INFO_PATTERN.matcher(releaseInfo);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Unparsable release info");
        }
        HelmReleaseInfo parsedReleaseInfo = new HelmReleaseInfo();
        parsedReleaseInfo.setName(matcher.group("name"));
        System.out.println(matcher.group("lastDeployed"));
        System.out.println(matcher.group("namespace"));
        return parsedReleaseInfo;
    }
}
