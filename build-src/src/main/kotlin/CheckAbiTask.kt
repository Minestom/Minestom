import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.lang.classfile.*
import java.lang.reflect.AccessFlag
import java.util.jar.JarFile

abstract class CheckAbiTask : DefaultTask() {

    @get:InputFile
    @get:Optional
    abstract val oldJar: RegularFileProperty

    @get:InputFile
    abstract val newJar: RegularFileProperty

    @TaskAction
    fun run() {
        if (!oldJar.isPresent) {
            logger.lifecycle("Skipping ABI check: Baseline JAR is not specified or could not be resolved (this is normal for new/unpublished modules).")
            return
        }
        val oldFile = oldJar.get().asFile
        if (!oldFile.exists()) {
            logger.lifecycle("Skipping ABI check: Baseline JAR file does not exist: ${oldFile.absolutePath}")
            return
        }
        val newFile = newJar.get().asFile

        val oldApis = extractPublicApi(oldFile)
        val newApis = extractPublicApi(newFile)

        var violations = 0

        // Check if any old public API is missing or modified in the new build
        for ((className, oldClass) in oldApis) {
            val newClass = newApis[className]
            if (newClass == null) {
                if (oldClass.internal) {
                    logger.warn("Ignored experimental/internal class removal: $className")
                } else {
                    logger.error("Class removed or made package-private: $className")
                    violations++
                }
                continue
            }

            // Check methods
            for ((methodName, oldMethod) in oldClass.methods) {
                val newMethod = newClass.methods[methodName]
                if (newMethod == null) {
                    if (oldMethod.internal || oldClass.internal) {
                        logger.warn("Ignored experimental/internal method removal/change: $className#$methodName")
                    } else {
                        logger.error("Public/protected method removed or signature changed: $className#$methodName")
                        violations++
                    }
                }
            }

            // Check fields
            for ((fieldName, oldField) in oldClass.fields) {
                val newField = newClass.fields[fieldName]
                if (newField == null) {
                    if (oldField.internal || oldClass.internal) {
                        logger.warn("Ignored experimental/internal field removal/change: $className#$fieldName")
                    } else {
                        logger.error("Public/protected field removed or type changed: $className#$fieldName")
                        violations++
                    }
                }
            }
        }

        if (violations > 0) {
            throw GradleException("ABI Check failed with $violations binary compatibility violations.")
        }
    }

    private data class ElementApi(
            val descriptor: String,
            val internal: Boolean
    )

    private data class ClassApiDump(
            val internal: Boolean,
            val methods: Map<String, ElementApi>,
            val fields: Map<String, ElementApi>
    )

    private fun extractPublicApi(jarFile: File): Map<String, ClassApiDump> {
        val apis = mutableMapOf<String, ClassApiDump>()
        val classFileParser = ClassFile.of()

        JarFile(jarFile).use { jar ->
            for (entry in jar.entries().asSequence()) {
                if (!entry.name.endsWith(".class")) continue

                jar.getInputStream(entry).use { stream ->
                    val bytes = stream.readAllBytes()
                    val classModel: ClassModel = classFileParser.parse(bytes)

                    // Skip non-public / non-protected classes
                    if (!isPublicOrProtected(classModel.flags().flags())) return@use

                    val className = classModel.thisClass().asInternalName()
                    val isClassInternal = hasIgnoredAnnotation(classModel)
                    val methods = mutableMapOf<String, ElementApi>()
                    val fields = mutableMapOf<String, ElementApi>()

                    // Inspect methods
                    for (method: MethodModel in classModel.methods()) {
                        if (isPublicOrProtected(method.flags().flags())) {
                            val methodName = method.methodName().stringValue()
                            val descriptor = method.methodType().stringValue()
                            val isMethodInternal = hasIgnoredAnnotation(method)
                            methods["$methodName$descriptor"] = ElementApi(descriptor, isMethodInternal)
                        }
                    }

                    // Inspect fields
                    for (field: FieldModel in classModel.fields()) {
                        if (isPublicOrProtected(field.flags().flags())) {
                            val fieldName = field.fieldName().stringValue()
                            val descriptor = field.fieldType().stringValue()
                            val isFieldInternal = hasIgnoredAnnotation(field)
                            fields["$fieldName:$descriptor"] = ElementApi(descriptor, isFieldInternal)
                        }
                    }

                    apis[className] = ClassApiDump(isClassInternal, methods, fields)
                }
            }
        }
        return apis
    }

    private fun isPublicOrProtected(flags: Set<AccessFlag>): Boolean {
        return AccessFlag.PUBLIC in flags || AccessFlag.PROTECTED in flags
    }

    // We don't care about internal or experimental marked methods
    private fun hasIgnoredAnnotation(element: AttributedElement): Boolean {
        val visible = element.findAttribute(Attributes.runtimeVisibleAnnotations()).orElse(null)
        val invisible = element.findAttribute(Attributes.runtimeInvisibleAnnotations()).orElse(null)

        val annotations = (visible?.annotations() ?: emptyList()) + (invisible?.annotations() ?: emptyList())
        for (anno in annotations) {
            val desc = anno.classSymbol().descriptorString()
            if (desc == $$"Lorg/jetbrains/annotations/ApiStatus$Internal;" ||
                    desc == $$"Lorg/jetbrains/annotations/ApiStatus$Experimental;") {
                return true
            }
        }
        return false
    }
}
