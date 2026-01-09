package io.github.inseefrlab.helmwrapper.service;

public record HelmFlags(
        boolean dryRun,
        boolean skipTlsVerify,
        String timeout,
        String caFile,
        boolean reuseValues,
        boolean forceConflicts) {

    public static HelmFlags suspendAndResumeFlags(
            boolean dryRun,
            boolean skipTlsVerify,
            String timeout,
            String caFile,
            boolean forceConflicts) {
        return new HelmFlags(dryRun, skipTlsVerify, timeout, caFile, true, forceConflicts);
    }

    public static HelmFlags installFlags(
            boolean dryRun,
            boolean skipTlsVerify,
            String timeout,
            String caFile,
            boolean forceConflicts) {
        return new HelmFlags(dryRun, skipTlsVerify, timeout, caFile, false, forceConflicts);
    }

    /**
     * @return cli ready string to use for helm upgrade, ending with a space such that it is safe to
     *     further append on
     */
    public String toHelmUpgradeCliString() {
        StringBuilder result = new StringBuilder();
        if (forceConflicts) {
            result.append("--force-conflicts ");
        }

        if (timeout != null) {
            result.append("--timeout " + timeout + " ");
        }

        if (skipTlsVerify) {
            result.append("--insecure-skip-tls-verify ");
        } else if (caFile != null) {
            result.append("--ca-file " + System.getenv("CACERTS_DIR") + "/" + caFile + " ");
        }

        if (dryRun) {
            result.append("--dry-run ");
        }
        if (reuseValues) {
            result.append("--reuse-values ");
        }

        return result.toString();
    }
}
