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

            if (oldClass.internal) {
                logger.warn("Ignored experimental/internal class changes: $className")
                continue
            }

            // Check class level changes
            if (oldClass.`interface` != newClass.`interface`) {
                logger.error("Class type changed (class <-> interface): $className")
                violations++
            }
            if (!oldClass.final && newClass.final) {
                logger.error("Class made final: $className (breaks subclasses)")
                violations++
            }
            if (!oldClass.abstract && newClass.abstract) {
                logger.error("Class made abstract: $className (breaks instantiation)")
                violations++
            }
            if (!oldClass.protected && newClass.protected) {
                logger.error("Class visibility narrowed to protected: $className")
                violations++
            }
            for (supertype in oldClass.supertypes) {
                if (supertype !in newClass.supertypes) {
                    logger.error("Class no longer implements/extends $supertype: $className")
                    violations++
                }
            }

            // Check methods
            for ((methodName, oldMethod) in oldClass.methods) {
                val newMethod = newClass.methods[methodName]
                if (newMethod == null) {
                    if (oldMethod.internal) {
                        logger.warn("Ignored experimental/internal method removal/change: $className#$methodName")
                    } else {
                        logger.error("Public/protected method removed or signature changed: $className#$methodName")
                        violations++
                    }
                    continue
                }

                if (oldMethod.internal) continue

                if (!oldMethod.protected && newMethod.protected) {
                    logger.error("Method visibility narrowed to protected: $className#$methodName")
                    violations++
                }
                if (oldMethod.static != newMethod.static) {
                    logger.error("Method static status changed: $className#$methodName")
                    violations++
                }
                if (!oldMethod.final && newMethod.final) {
                    logger.error("Method made final: $className#$methodName (breaks subclasses)")
                    violations++
                }
                if (!oldMethod.abstract && newMethod.abstract) {
                    logger.error("Method made abstract: $className#$methodName (breaks implementing classes/subclasses)")
                    violations++
                }
            }

            // Check fields
            for ((fieldName, oldField) in oldClass.fields) {
                val newField = newClass.fields[fieldName]
                if (newField == null) {
                    if (oldField.internal) {
                        logger.warn("Ignored experimental/internal field removal/change: $className#$fieldName")
                    } else {
                        logger.error("Public/protected field removed or type changed: $className#$fieldName")
                        violations++
                    }
                    continue
                }

                if (oldField.internal) continue

                if (!oldField.protected && newField.protected) {
                    logger.error("Field visibility narrowed to protected: $className#$fieldName")
                    violations++
                }
                if (oldField.static != newField.static) {
                    logger.error("Field static status changed: $className#$fieldName")
                    violations++
                }
                if (!oldField.final && newField.final) {
                    logger.error("Field made final: $className#$fieldName")
                    violations++
                }
            }
        }

        // Check for newly added abstract methods in interfaces and non final classes
        for ((className, newClass) in newApis) {
            val oldClass = oldApis[className] ?: continue // New classes are always compatible
            if (newClass.internal || oldClass.internal) continue

            // Determine if we need to check this class/interface
            val checkInterface = newClass.`interface` && !newClass.nonExtendable
            val checkClass = !newClass.`interface` && !newClass.final

            if (!checkInterface && !checkClass) continue

            for ((methodName, newMethod) in newClass.methods) {
                // If the new method is abstract, not internal, and did not exist in the baseline version
                if (!newMethod.abstract) continue
                if (newMethod.internal) continue
                if (oldClass.methods.containsKey(methodName)) continue

                if (newClass.`interface`) {
                    logger.error("New abstract method added to interface: $className#$methodName (breaks implementing classes)")
                } else {
                    logger.error("New abstract method added to extendable class: $className#$methodName (breaks subclasses)")
                }
                violations++
            }
        }

        if (violations > 0) {
            throw GradleException("ABI Check failed with $violations binary compatibility violations.")
        }
    }

    private data class ElementApi(
            val descriptor: String,
            val internal: Boolean,
            val abstract: Boolean,
            val static: Boolean,
            val final: Boolean,
            val protected: Boolean
    )

    private data class ClassApiDump(
            val internal: Boolean,
            val `interface`: Boolean, // Annoying reserved keyword ugh
            val final: Boolean,
            val abstract: Boolean,
            val nonExtendable: Boolean,
            val protected: Boolean,
            val supertypes: Set<String>,
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

                    val classFlags = classModel.flags().flags()
                    // Skip non-public / non-protected classes
                    if (!isPublicOrProtected(classFlags)) return@use

                    val className = classModel.thisClass().asInternalName()
                    val isClassInternal = isInternal(classModel)
                    val isClassInterface = AccessFlag.INTERFACE in classFlags
                    val isClassFinal = AccessFlag.FINAL in classFlags
                    val isClassAbstract = AccessFlag.ABSTRACT in classFlags
                    val isClassNonExtendable = isNonExtendable(classModel)
                    val isClassProtected = AccessFlag.PROTECTED in classFlags

                    val supertypes = mutableSetOf<String>()
                    classModel.superclass().ifPresent { supertypes.add(it.asInternalName()) }
                    classModel.interfaces().forEach { supertypes.add(it.asInternalName()) }

                    val methods = mutableMapOf<String, ElementApi>()
                    val fields = mutableMapOf<String, ElementApi>()

                    // Inspect methods
                    for (method: MethodModel in classModel.methods()) {
                        val flags = method.flags().flags()
                        if (!isPublicOrProtected(flags)) continue

                        val methodName = method.methodName().stringValue()
                        val descriptor = method.methodType().stringValue()
                        val isMethodInternal = isInternal(method)
                        val isMethodAbstract = AccessFlag.ABSTRACT in flags
                        val isMethodStatic = AccessFlag.STATIC in flags
                        val isMethodFinal = AccessFlag.FINAL in flags
                        val isMethodProtected = AccessFlag.PROTECTED in flags
                        methods["$methodName$descriptor"] = ElementApi(descriptor, isMethodInternal, isMethodAbstract, isMethodStatic, isMethodFinal, isMethodProtected)
                    }

                    // Inspect fields
                    for (field: FieldModel in classModel.fields()) {
                        val flags = field.flags().flags()
                        if (!isPublicOrProtected(flags)) continue

                        val fieldName = field.fieldName().stringValue()
                        val descriptor = field.fieldType().stringValue()
                        val isFieldInternal = isInternal(field)
                        val isFieldStatic = AccessFlag.STATIC in flags
                        val isFieldFinal = AccessFlag.FINAL in flags
                        val isFieldProtected = AccessFlag.PROTECTED in flags
                        fields["$fieldName:$descriptor"] = ElementApi(descriptor, isFieldInternal, false, isFieldStatic, isFieldFinal, isFieldProtected)
                    }

                    apis[className] = ClassApiDump(
                            isClassInternal, isClassInterface, isClassFinal, isClassAbstract, isClassNonExtendable, isClassProtected, supertypes, methods, fields
                    )
                }
            }
        }
        return apis
    }

    private fun isPublicOrProtected(flags: Set<AccessFlag>): Boolean {
        return AccessFlag.PUBLIC in flags || AccessFlag.PROTECTED in flags
    }

    // We don't care about internal or experimental marked methods
    private fun isInternal(element: AttributedElement): Boolean {
        return hasAnnotation(element, $$"Lorg/jetbrains/annotations/ApiStatus$Internal;", $$"Lorg/jetbrains/annotations/ApiStatus$Experimental;")
    }

    private fun isNonExtendable(element: AttributedElement): Boolean {
        if (hasAnnotation(element, $$"Lorg/jetbrains/annotations/ApiStatus$NonExtendable;")) return true
        if (element !is ClassModel) return false
        return element.findAttribute(Attributes.permittedSubclasses()).isPresent
    }

    private fun hasAnnotation(element: AttributedElement, vararg descriptors: String): Boolean {
        val visible = element.findAttribute(Attributes.runtimeVisibleAnnotations()).orElse(null)
        val invisible = element.findAttribute(Attributes.runtimeInvisibleAnnotations()).orElse(null)

        val annotations = (visible?.annotations() ?: emptyList()) + (invisible?.annotations() ?: emptyList())
        for (anno in annotations) {
            val desc = anno.classSymbol().descriptorString()
            if (desc in descriptors) {
                return true
            }
        }
        return false
    }
}
