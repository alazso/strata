plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.dokka)
    alias(libs.plugins.dokka.javadoc)
    `maven-publish`
}

description = "Strata public API — Java-friendly surface for dependent plugins"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
    withSourcesJar()
    withJavadocJar()
}

dependencies {
    // Consumers compile against the API; stdlib is exported transitively via the POM
    // so a Kotlin consumer resolves it, and at runtime the Strata plugin provides it.
    api(libs.kotlin.stdlib)

    compileOnly(libs.paper.api)
    compileOnly(libs.jetbrains.annotations)
    // Kotlin-only ergonomic extensions (Exposed + coroutine wrappers) compile against these;
    // provided at runtime by the Strata plugin's loaded libraries. Java consumers never touch them.
    compileOnly(libs.coroutines.core)
    compileOnly(libs.exposed.core)
    compileOnly(libs.exposed.jdbc)
}

kotlin {
    explicitApi()
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

// strata-api is pure Kotlin, so the stock `javadoc` task produces nothing. Source the published
// `-javadoc` jar from Dokka's Javadoc-format output instead (it reads the KDoc on every public type).
tasks.named<Jar>("javadocJar") {
    from(tasks.named("dokkaGeneratePublicationJavadoc"))
}

tasks.withType<Javadoc>().configureEach {
    (options as StandardJavadocDocletOptions).apply {
        addStringOption("Xdoclint:none", "-quiet")
        encoding = "UTF-8"
    }
}

tasks.withType<Jar>().configureEach {
    from(rootProject.file("LICENSE")) {
        into("META-INF")
    }
}

publishing {
    publications {
        create<MavenPublication>("library") {
            artifactId = "strata-api"
            from(components["java"])
            pom {
                name.set("Strata API")
                description.set(project.description)
                url.set("https://github.com/alazso/strata")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
            }
        }
    }
    repositories {
        maven {
            name = "alazso"
            val isRelease = !project.version.toString().endsWith("-SNAPSHOT")
            url = uri(
                if (isRelease) "https://repo.alaz.so/releases"
                else "https://repo.alaz.so/snapshots"
            )
            credentials {
                username = System.getenv("ALAZSO_REPO_USER")
                password = System.getenv("ALAZSO_REPO_TOKEN")
            }
        }
    }
}
