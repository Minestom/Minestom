rootProject.name = "minestom"

includeBuild("build-src")

include("code-generators")
include("testing")

include("jmh-benchmarks")
//include("jcstress-tests") //TODO (jdk25) broken since 9.0.0 deprecations

include("demo")
