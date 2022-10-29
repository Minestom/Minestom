plugins {
    id("net.kyori.indra")
    id("net.kyori.indra.publishing")
	id("maven-publish")
    // id("net.kyori.indra.publishing.sonatype")
}

indra {
    javaVersions {
        target(17)
        testWith(17)
    }

    github("Minestom", "Minestom") {
        ci(true)
    }
    apache2License()
	
	publishReleasesTo("tecno-repo", "https://repo.mrtecno.tk/repository/maven-release/")
	publishSnapshotsTo("tecno-repo", "https://repo.mrtecno.tk/repository/maven-snapshot/")

    configurePublications {
        pom {
            developers {
                developer {
                    id.set("TheMode")
                    name.set("TheMode")
                }
                developer {
                    id.set("jglrxavpok")
                    name.set("jglrxavpok")
                }
            }
        }
    }
}

publishing {
	repositories {
		maven {
			name = "tecno-repo"
			url = uri("https://repo.mrtecno.tk/repository/personal-hosted/")
		}
	}
}