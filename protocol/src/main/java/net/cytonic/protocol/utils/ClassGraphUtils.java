package net.cytonic.protocol.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class ClassGraphUtils {

    public static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(
        125,
        Thread.ofVirtual()
            .name("graph-scan-", 0)
            .factory());

    public static <T> List<T> getImplementedClasses(Class<T> clazz, String packageName) {
        ClassGraph graph = new ClassGraph().acceptPackages(packageName).enableAllInfo();

        List<T> resultList = new ArrayList<>();
        try (ScanResult result = graph.scan(EXECUTOR, 125)) {
            result.getClassesImplementing(clazz).loadClasses().forEach(foundClass -> {
                try {
                    if (foundClass.isAnnotationPresent(ExcludeFromClassGraph.class)) return;
                    Constructor<?> constructor = foundClass.getDeclaredConstructor();
                    constructor.setAccessible(true);
                    //noinspection unchecked
                    T instance = (T) constructor.newInstance();
                    resultList.add(instance);
                } catch (Exception e) {
                    log.error("An error occurred whilst getting implemented classes for class '{}'!",
                        clazz.getName(), e);
                }
            });
        } catch (Exception e) {
            log.error("An error occurred whilst getting implemented classes for class '{}'!", clazz.getName(), e);
        }
        return resultList;
    }

    public static <T> List<T> getExtendedClasses(Class<T> clazz, String packageName) {
        ClassGraph graph = new ClassGraph().acceptPackages(packageName).enableAllInfo();

        List<T> resultList = new ArrayList<>();
        try (ScanResult result = graph.scan(EXECUTOR, 125)) {
            result.getSubclasses(clazz).loadClasses().forEach(foundClass -> {
                try {
                    if (foundClass.isAnnotationPresent(ExcludeFromClassGraph.class)) return;
                    if (!foundClass.getPackage().getName().startsWith(packageName)) {
                        return;
                    }
                    if (Modifier.isAbstract(foundClass.getModifiers()) || foundClass.isInterface()) {
                        return;
                    }
                    Constructor<?> constructor = foundClass.getDeclaredConstructor();
                    constructor.setAccessible(true);
                    //noinspection unchecked
                    T instance = (T) constructor.newInstance();
                    resultList.add(instance);
                } catch (Exception e) {
                    log.error("An error occurred whilst getting extended classes for class '{}'!", clazz.getName(), e);
                }
            });
        } catch (Exception e) {
            log.error("An error occurred whilst getting extended classes for class '{}'!", clazz.getName(), e);
        }
        return resultList;
    }

    public static <T extends Annotation> List<AnnotatedMethod<T>> getAnnotatedMethods(Class<T> clazz,
        String packageName) {
        ClassGraph graph = new ClassGraph().acceptPackages(packageName).enableAllInfo();

        List<AnnotatedMethod<T>> resultList = new ArrayList<>();
        try (ScanResult result = graph.scan(EXECUTOR, 125)) {
            result.getClassesWithMethodAnnotation(clazz).loadClasses().forEach(foundClass -> {
                try {
                    if (foundClass.isAnnotationPresent(ExcludeFromClassGraph.class)) return;
                    if (!foundClass.getPackage().getName().startsWith(packageName)) {
                        return;
                    }
                    if (Modifier.isAbstract(foundClass.getModifiers()) || foundClass.isInterface()) {
                        return;
                    }
                    for (Method method : foundClass.getDeclaredMethods()) {
                        if (method.isAnnotationPresent(clazz)) {
                            resultList.add(
                                new AnnotatedMethod<>(clazz, foundClass, method, method.getAnnotation(clazz)));
                        }
                    }
                } catch (Exception e) {
                    log.error("An error occurred whilst getting annotated methods for annotation '{}'!",
                        clazz.getName(), e);
                }
            });
        } catch (Exception e) {
            log.error("An error occurred whilst getting annotated methods for annotation '{}'!", clazz.getName(), e);
        }
        return resultList;
    }

    public record AnnotatedMethod<T extends Annotation>(
        Class<?> annotationClass, Class<?> foundClass, Method method, T annotation) {

    }
}
