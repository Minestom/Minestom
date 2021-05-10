private val prefix = "minestom"

rootProject.name = "$prefix-parent"

// core modules
listOf("core", "demo").forEach {
    include(it)
    findProject(":$it")?.name = "$prefix-$it"
}

// extras
listOf("lwjgl").forEach {
    include(it)
    findProject(":$it")?.name = "$prefix-extras-$it"
}

// features
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")