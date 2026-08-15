import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileType
import org.gradle.api.tasks.*
import org.gradle.work.ChangeType
import org.gradle.work.Incremental
import org.gradle.work.InputChanges

/**
 * Enforces the test naming scheme:
 *
 * - Classes declaring `@EnvTest` are named `*IntegrationTest`.
 * - Classes declaring `@RegistriesTest` are named `*RegistriesTest` and never use the `*IntegrationTest` suffix.
 * - The `*IntegrationTest` and `*RegistriesTest` suffixes are reserved for classes declaring the matching fixture.
 *
 * Classes are judged by their own declarations only; an inherited fixture annotation does not constrain the
 * subclass name.
 */
abstract class CheckTestNamingTask : DefaultTask() {

    private companion object {
        val ENV_TEST = Regex("(?m)^@EnvTest\\b")
        val REGISTRY_TEST = Regex("(?m)^@RegistriesTest\\b")
    }

    @get:Incremental
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sources: ConfigurableFileCollection

    init {
        // No file output, the check result itself is the outcome
        outputs.upToDateWhen { true }
    }

    @TaskAction
    fun run(inputChanges: InputChanges) {
        val violations = mutableListOf<String>()
        for (change in inputChanges.getFileChanges(sources)) {
            if (change.changeType == ChangeType.REMOVED) continue
            if (change.fileType != FileType.FILE || !change.file.name.endsWith(".java")) continue
            val name = change.file.name.removeSuffix(".java")
            val source = change.file.readText()
            val env = ENV_TEST.containsMatchIn(source)
            val registry = REGISTRY_TEST.containsMatchIn(source)

            if (env && !name.endsWith("IntegrationTest"))
                violations += "$name: @EnvTest classes must be named *IntegrationTest"
            if (registry && name.endsWith("IntegrationTest"))
                violations += "$name: *IntegrationTest is reserved for @EnvTest classes"
            if (registry && !name.endsWith("RegistriesTest"))
                violations += "$name: @RegistriesTest classes must be named *RegistriesTest"
            if (!env && name.endsWith("IntegrationTest"))
                violations += "$name: *IntegrationTest requires a declared @EnvTest"
            if (!registry && name.endsWith("RegistriesTest"))
                violations += "$name: *RegistriesTest requires a declared @RegistriesTest"
        }

        if (violations.isNotEmpty()) {
            throw GradleException("Test naming violations:\n" + violations.joinToString("\n"))
        }
    }
}
