plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.shadow)
    jacoco
}

description = "Strata server plugin — implementations, lifecycle, and runtime library loader"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

dependencies {
    // The API surface — bundled into the plugin jar (see shadowJar whitelist below).
    implementation(project(":strata-api"))

    // Metrics libraries — shaded + relocated into the jar (bStats requires relocation).
    // Strata holds one shared copy; consumers route through it instead of each shading their own.
    implementation(libs.bstats.bukkit)
    implementation(libs.faststats.bukkit)

    compileOnly(libs.paper.api)
    compileOnly(libs.jetbrains.annotations)
    compileOnly(libs.placeholderapi)
    // Integration APIs — provided at runtime by the respective server plugins (soft-depend),
    // guarded by class-presence checks. Never bundled, never runtime-loaded.
    compileOnly(libs.luckperms.api)
    // VaultAPI: only the Economy interface is needed; drop its transitive (old Bukkit) deps.
    compileOnly(libs.vault.api) { isTransitive = false }

    // Runtime libraries — fetched by StrataLoader at startup, so compileOnly here:
    // compiled against, but NEVER bundled into the jar.
    compileOnly(libs.kotlin.stdlib)
    compileOnly(libs.coroutines.core)
    compileOnly(libs.exposed.core)
    compileOnly(libs.exposed.dao)
    compileOnly(libs.exposed.jdbc)
    compileOnly(libs.hikari)
    compileOnly(libs.sqlite.jdbc)
    compileOnly(libs.mariadb.jdbc)

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj.core)
    testImplementation(libs.mockk)
    testRuntimeOnly(libs.junit.platform.launcher)
}

// Tests need the compileOnly runtime libraries (stdlib, coroutines, etc.) on their classpath.
configurations {
    testImplementation.get().extendsFrom(compileOnly.get())
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-Xlint:all,-processing,-serial")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
    finalizedBy(tasks.withType<JacocoReport>())
}

tasks.withType<JacocoReport>().configureEach {
    dependsOn(tasks.withType<Test>())
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

tasks.named<ProcessResources>("processResources") {
    val props = mapOf("version" to project.version)
    inputs.properties(props)
    filesMatching("paper-plugin.yml") {
        expand(props)
    }
}

tasks.withType<Jar>().configureEach {
    from(rootProject.file("LICENSE")) {
        into("META-INF")
    }
}

// The shadow jar is the server deliverable: strata-plugin-<version>.jar, renamed to
// strata-<version>.jar at release. Only the API project is bundled; all heavy libs are
// runtime-loaded by StrataLoader, so the jar stays small and ships no kotlin-stdlib.
tasks.shadowJar {
    archiveClassifier.set("")
    dependencies {
        include(project(":strata-api"))
        include(dependency("org.bstats:.*"))
        include(dependency("dev.faststats.*:.*"))
    }
    // bStats mandates relocation; FastStats recommends it. Rewrites the libs and our references.
    relocate("org.bstats", "${project.group}.libs.bstats")
    relocate("dev.faststats", "${project.group}.libs.faststats")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

// The plain jar is replaced by the shadow jar (empty classifier); disable it.
tasks.named<Jar>("jar") {
    enabled = false
}

tasks.named("assemble") {
    dependsOn(tasks.shadowJar)
}
