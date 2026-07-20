import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileType
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.*
import org.gradle.work.ChangeType
import org.gradle.work.Incremental
import org.gradle.work.InputChanges

/**
 * Fails when a Java source file imports one of the forbidden packages.
 */
abstract class CheckImportsTask : DefaultTask() {

    @get:Incremental
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sources: ConfigurableFileCollection

    @get:Input
    abstract val forbiddenPackages: ListProperty<String>

    init {
        // No file output, the check result itself is the outcome
        outputs.upToDateWhen { true }
    }

    @TaskAction
    fun run(inputChanges: InputChanges) {
        val forbidden = forbiddenPackages.get()
        val violations = mutableListOf<String>()
        for (change in inputChanges.getFileChanges(sources)) {
            if (change.changeType == ChangeType.REMOVED) continue
            if (change.fileType != FileType.FILE || !change.file.name.endsWith(".java")) continue
            change.file.useLines { lines ->
                lines.forEachIndexed { index, line ->
                    if (line.startsWith("import ") && forbidden.any { it in line }) {
                        violations += "${change.file}:${index + 1}: $line"
                    }
                }
            }
        }

        if (violations.isNotEmpty()) {
            throw GradleException("Forbidden annotation imports, use org.jetbrains.annotations instead:\n"
                    + violations.joinToString("\n"))
        }
    }
}
