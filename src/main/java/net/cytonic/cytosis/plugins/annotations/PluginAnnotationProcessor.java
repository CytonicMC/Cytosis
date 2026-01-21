package net.cytonic.cytosis.plugins.annotations;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.google.auto.service.AutoService;
import javax.annotation.processing.Completion;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import net.cytonic.cytosis.Cytosis;

import static com.google.common.base.Preconditions.checkNotNull;

@AutoService(Processor.class)
@SupportedAnnotationTypes({"net.cytonic.cytosis.plugins.annotations.Plugin"})
@SupportedSourceVersion(SourceVersion.RELEASE_25)
@SupportedOptions({})
public class PluginAnnotationProcessor implements Processor {

    private ProcessingEnvironment environment;
    private String pluginClassFound;
    private boolean warnedAboutMultiplePlugins;
    private boolean initialized = false;

    @Override
    public Set<String> getSupportedOptions() {
        return Set.of();
    }

    /**
     * If the processor class is annotated with {@link SupportedAnnotationTypes}, return an unmodifiable set with the
     * same set of strings as the annotation.  If the class is not so annotated, an empty set is returned.
     * <p>
     * If the {@linkplain ProcessingEnvironment#getSourceVersion source version} does not support modules, in other
     * words if it is less than or equal to {@link SourceVersion#RELEASE_8 RELEASE_8}, then any leading
     * {@linkplain Processor#getSupportedAnnotationTypes module prefixes} are stripped from the names.
     *
     * @return the names of the annotation interfaces supported by this processor, or an empty set if none
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        SupportedAnnotationTypes sat = this.getClass().getAnnotation(SupportedAnnotationTypes.class);
        boolean initialized = isInitialized();
        if (sat == null) {
            if (initialized) {
                environment.getMessager().printMessage(Diagnostic.Kind.WARNING,
                    "No SupportedAnnotationTypes annotation " + "found on " + this.getClass().getName()
                        + ", returning an empty set.");
            }
            return Set.of();
        } else {
            boolean stripModulePrefixes =
                initialized && environment.getSourceVersion().compareTo(SourceVersion.RELEASE_8) <= 0;
            return arrayToSet(sat.value(), stripModulePrefixes, "annotation interface", "@SupportedAnnotationTypes");
        }
    }

    /**
     * If the processor class is annotated with {@link SupportedSourceVersion}, return the source version in the
     * annotation.  If the class is not so annotated, {@link SourceVersion#RELEASE_6} is returned.
     *
     * @return the latest source version supported by this processor
     */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        SupportedSourceVersion ssv = this.getClass().getAnnotation(SupportedSourceVersion.class);
        return ssv.value();
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
            "Cytosis plugin annotation processor version " + PluginAnnotationProcessor.class.getPackage()
                .getImplementationVersion());
        this.environment = processingEnv;
        checkNotNull(processingEnv, "processingEnv");
        initialized = true;
    }

    @Override
    public synchronized boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        environment.getMessager().printMessage(Diagnostic.Kind.WARNING, "Processing!!!!");
        if (roundEnv.processingOver()) {
            return false;
        }

        for (Element element : roundEnv.getElementsAnnotatedWith(Plugin.class)) {
            if (!isClassElement(element)) {
                reportInvalidElement(element);
                return false;
            }

            TypeElement typeElement = (TypeElement) element;
            Name qualifiedName = typeElement.getQualifiedName();

            if (isDuplicatePlugin(qualifiedName)) {
                warnAboutMultiplePlugins();
                return false;
            }

            Plugin plugin = element.getAnnotation(Plugin.class);
            if (!validatePlugin(plugin, qualifiedName)) {
                return false;
            }

            SerializedDescription description = SerializedDescription.from(plugin, qualifiedName.toString());
            if (!writePluginFile(description, qualifiedName)) {
                return false;
            }

            pluginClassFound = qualifiedName.toString();
        }

        return false;
    }

    @Override
    public Iterable<? extends Completion> getCompletions(Element element, AnnotationMirror annotation,
        ExecutableElement member, String userText) {
        return null;
    }

    private boolean isClassElement(Element element) {
        return element.getKind() == ElementKind.CLASS;
    }

    private void reportInvalidElement(Element element) {
        environment.getMessager().printMessage(Diagnostic.Kind.ERROR,
            "Only classes can be annotated with " + Plugin.class.getCanonicalName());
    }

    private boolean isDuplicatePlugin(Name qualifiedName) {
        return Objects.equals(pluginClassFound, qualifiedName.toString());
    }

    private void warnAboutMultiplePlugins() {
        if (!warnedAboutMultiplePlugins) {
            environment.getMessager().printMessage(Diagnostic.Kind.WARNING,
                "Cytosis does not yet currently support multiple plugins. We are using "
                    + pluginClassFound + " for your plugin's main class.");
            warnedAboutMultiplePlugins = true;
        }
    }

    private boolean validatePlugin(Plugin plugin, Name qualifiedName) {
        if (!SerializedDescription.ID_PATTERN.matcher(plugin.name()).matches()) {
            environment.getMessager().printMessage(Diagnostic.Kind.ERROR,
                "Invalid ID for plugin " + qualifiedName + ". IDs must start alphabetically, " +
                    "have lowercase alphanumeric characters, and can contain dashes or underscores.");
            return false;
        }

        for (Dependency dependency : plugin.dependencies()) {
            if (!SerializedDescription.ID_PATTERN.matcher(dependency.id()).matches()) {
                environment.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "Invalid dependency ID '" + dependency.id() + "' for plugin " + qualifiedName
                        + ". IDs must start alphabetically, have lowercase alphanumeric characters, and can contain " +
                        "dashes or underscores.");
                return false;
            }
        }

        return true;
    }

    private boolean writePluginFile(SerializedDescription description, Name qualifiedName) {
        try {
            FileObject object = environment.getFiler()
                .createResource(StandardLocation.CLASS_OUTPUT, "", "cytosis-plugin.json");
            try (Writer writer = new BufferedWriter(object.openWriter())) {
                Cytosis.GSON.toJson(description, writer);
            }
            return true;
        } catch (IOException e) {
            environment.getMessager().printMessage(Diagnostic.Kind.ERROR,
                "Unable to generate plugin file for " + qualifiedName);
            return false;
        }
    }

    protected synchronized boolean isInitialized() {
        return initialized;
    }

    private Set<String> arrayToSet(String[] array, boolean stripModulePrefixes, String contentType,
        String annotationName) {
        assert array != null;
        Set<String> set = new HashSet<>();
        for (String s : array) {
            boolean stripped = false;
            if (stripModulePrefixes) {
                int index = s.indexOf('/');
                if (index != -1) {
                    s = s.substring(index + 1);
                    stripped = true;
                }
            }
            boolean added = set.add(s);
            // Don't issue a duplicate warning when the module name is
            // stripped off to avoid spurious warnings in a case like
            // "foo/a.B", "bar/a.B".
            if (!added && !stripped && isInitialized()) {
                environment.getMessager().printMessage(Diagnostic.Kind.WARNING,
                    "Duplicate " + contentType + " ``" + s + "'' for processor " + this.getClass().getName()
                        + " in its " + annotationName + "annotation.");
            }
        }
        return Collections.unmodifiableSet(set);
    }
}
