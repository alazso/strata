// Root holds no sources. Configuration lives in the two modules:
//   :strata-api    — published, Java-friendly public surface (consumers compileOnly this)
//   :strata-plugin — the shaded server deliverable (GitHub Releases)
// `group` and `version` propagate to both modules from gradle.properties.
//
// Plugins are declared here once (apply false) so their classpath loads a single time;
// the subprojects apply them. Avoids the "Kotlin plugin loaded multiple times" warning.
plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.shadow) apply false
}
