/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.util;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.status.InfoStatus;
import ch.qos.logback.core.status.WarnStatus;
import java.lang.module.ModuleDescriptor;
import java.util.Optional;

public class VersionUtil {
    static final String DEPENDER_S_WAS_EXPECTING_PATTERN = "Depender [%s] was expecting version %s for dependency [%s] but found version %s";
    Context context;

    protected VersionUtil(Context context) {
        this.context = context;
    }

    public static String nonNull(String input) {
        if (input == null) {
            return "?";
        }
        return input;
    }

    private static String getVersionOfClassByModule(Class<?> aClass) {
        Module module = aClass.getModule();
        if (module == null) {
            return null;
        }
        ModuleDescriptor md = module.getDescriptor();
        if (md == null) {
            return null;
        }
        Optional<String> opt = md.rawVersion();
        return opt.orElse(null);
    }

    protected String getExpectedVersionOfDependencyByProperties(Class<?> dependerClass, String propertiesFileName, String dependencyNameAsKey) {
        return null;
    }

    public static void checkForVersionEquality(Context context, String dependerVersion, String dependencyVersion, String dependerName, String dependencyName) {
        VersionUtil.addFoundVersionStatus(context, dependerName, dependerVersion);
        dependerVersion = VersionUtil.nonNull(dependerVersion);
        if (dependerVersion.equals("?") || !dependerVersion.equals(dependencyVersion)) {
            VersionUtil.addFoundVersionStatus(context, dependencyName, dependencyVersion);
            String discrepancyMsg = String.format("Versions of %s and %s are different or unknown.", dependencyName, dependerVersion);
            context.getStatusManager().add(new WarnStatus(discrepancyMsg, context));
        }
    }

    protected static void addFoundVersionStatus(Context context, String name, String version) {
        String foundDependent = String.format("Found %s version %s", name, VersionUtil.nonNull(version));
        context.getStatusManager().add(new InfoStatus(foundDependent, context));
    }

    protected static String nameToPropertiesFilename(String name) {
        return name + "-dependencies.properties";
    }

    public void compareExpectedAndFoundVersion(String actualDependencyVersion, Class<?> dependerClass, String dependerVersion, String dependerName, String dependencyName) {
        String propertiesFileName = VersionUtil.nameToPropertiesFilename(dependerName);
        String expectedDependencyVersion = this.getExpectedVersionOfDependencyByProperties(dependerClass, propertiesFileName, dependencyName);
        String safeExpectedDependencyVersion = VersionUtil.nonNull(expectedDependencyVersion);
        VersionUtil.addFoundVersionStatus(this.context, dependencyName, actualDependencyVersion);
        VersionUtil.addFoundVersionStatus(this.context, dependerName, dependerVersion);
        if (!safeExpectedDependencyVersion.equals(actualDependencyVersion)) {
            String discrepancyMsg = String.format(DEPENDER_S_WAS_EXPECTING_PATTERN, dependerName, safeExpectedDependencyVersion, dependencyName, actualDependencyVersion);
            this.context.getStatusManager().add(new WarnStatus(discrepancyMsg, this.context));
            String seeMsg = "See also https://logback.qos.ch/codes.html#versionMismatch";
            this.context.getStatusManager().add(new WarnStatus(seeMsg, this.context));
        }
    }
}

