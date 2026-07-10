rootProject.name = "minestom"

includeBuild("build-src")

include("lib")
include("framework")

include("code-generators")
include("testing")

include("jmh-benchmarks")
include("jcstress-tests")

include("demo")
