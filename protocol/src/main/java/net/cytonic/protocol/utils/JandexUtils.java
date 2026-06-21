package net.cytonic.protocol.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.jboss.jandex.AnnotationTarget.Kind;
import org.jboss.jandex.ClassInfo;

@Slf4j
@UtilityClass
public class JandexUtils {

    public static <T> List<T> getExtendedClasses(Class<T> clazz) {
        List<T> list = new ArrayList<>();
        IndexHolder.get().getAllKnownSubclasses(clazz).stream()
            .filter(ci -> !ci.isAbstract() && !ci.isInterface())
            .filter(ci -> !ci.hasAnnotation(ExcludeFromIndex.class))
            .forEach(ci -> resolve(ci, list));
        return list;
    }

    public static <T> List<Class<T>> getExtendedClassesClass(Class<T> clazz) {
        List<Class<T>> list = new ArrayList<>();
        IndexHolder.get().getAllKnownSubclasses(clazz).stream()
            .filter(ci -> !ci.isAbstract() && !ci.isInterface())
            .filter(ci -> !ci.hasAnnotation(ExcludeFromIndex.class))
            .forEach(ci -> resolveClass(ci, list));
        return list;
    }

    public static <T> List<T> getImplementedClasses(Class<T> clazz) {
        List<T> list = new ArrayList<>();
        IndexHolder.get().getAllKnownImplementations(clazz).stream()
            .filter(ci -> !ci.isAbstract() && !ci.isInterface())
            .filter(ci -> !ci.hasAnnotation(ExcludeFromIndex.class))
            .forEach(ci -> resolve(ci, list));
        return list;
    }

    public static <T> List<Class<T>> getImplementedClassesClass(Class<T> clazz) {
        List<Class<T>> list = new ArrayList<>();
        IndexHolder.get().getAllKnownImplementations(clazz).stream()
            .filter(ci -> !ci.isAbstract() && !ci.isInterface())
            .filter(ci -> !ci.hasAnnotation(ExcludeFromIndex.class))
            .forEach(ci -> resolveClass(ci, list));
        return list;
    }

    public static <A extends Annotation, T> List<T> getAnnotatedClasses(Class<A> annotationClass) {
        List<T> list = new ArrayList<>();
        IndexHolder.get().getAnnotations(annotationClass).stream()
            .filter(ai -> ai.target().kind() == Kind.CLASS)
            .filter(ai -> !ai.target().hasAnnotation(ExcludeFromIndex.class))
            .map(ai -> ai.target().asClass())
            .filter(ci -> !ci.isAbstract() && !ci.isInterface())
            .forEach(ci -> resolve(ci, list));
        return list;
    }

    public static <A extends Annotation, T> List<Class<T>> getAnnotatedClassesClass(Class<A> annotationClass) {
        List<Class<T>> list = new ArrayList<>();
        IndexHolder.get().getAnnotations(annotationClass).stream()
            .filter(ai -> ai.target().kind() == Kind.CLASS)
            .filter(ai -> !ai.target().hasAnnotation(ExcludeFromIndex.class))
            .map(ai -> ai.target().asClass())
            .filter(ci -> !ci.isAbstract() && !ci.isInterface())
            .forEach(ci -> resolveClass(ci, list));
        return list;
    }

    public static <T extends Annotation> List<Method> getAnnotatedMethods(Class<T> clazz) {
        List<Method> list = new ArrayList<>();
        IndexHolder.get().getAnnotations(clazz).stream()
            .filter(ai -> ai.target().kind() == Kind.METHOD)
            .filter(ai -> !ai.target().hasAnnotation(ExcludeFromIndex.class))
            .map(ai -> ai.target().asMethod())
            .forEach(methodInfo -> {
                try {
                    Method method =
                        Class.forName(methodInfo.declaringClass().name().toString())
                            .getDeclaredMethod(
                                methodInfo.name(),
                                methodInfo.parameterTypes().stream()
                                    .map(type -> {
                                        try {
                                            return Class.forName(type.name().toString(), true,
                                                Thread.currentThread().getContextClassLoader());
                                        } catch (ClassNotFoundException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }).toArray(Class[]::new));
                    list.add(method);
                } catch (Exception e) {
                    log.error("An error occurred while trying to resolve the method {}", methodInfo.name(), e);
                }
            });
        return list;
    }

    private static <T> void resolveClass(ClassInfo ci, List<Class<T>> list) {
        try {
            //noinspection unchecked
            list.add((Class<T>) Class.forName(ci.name().toString()));
        } catch (Exception e) {
            log.error("An error occurred while trying to resolve the class {}", ci.name(), e);
        }
    }

    private static <T> void resolve(ClassInfo ci, List<T> list) {
        try {
            //noinspection unchecked
            Class<T> found = (Class<T>) Class.forName(ci.name().toString());
            T instance = InstanceResolver.INSTANCE.resolve(found);
            list.add(instance);
        } catch (Exception e) {
            log.error("An error occurred while trying to resolve the class {}", ci.name(), e);
        }
    }
}
