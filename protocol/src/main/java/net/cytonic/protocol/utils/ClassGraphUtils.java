/*
 * MIT License
 *
 * Copyright (c) 2025 Webhead1104
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.cytonic.protocol.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import net.cytonic.protocol.ExcludeFromClassGraph;

@Slf4j
@UtilityClass
public class ClassGraphUtils {

    public static <T> List<T> getImplementedClasses(Class<T> clazz, String packageName) {
        ClassGraph graph = new ClassGraph().acceptPackages(packageName).enableAllInfo();

        List<T> resultList = new ArrayList<>();
        try (ScanResult result = graph.scan()) {
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
        try (ScanResult result = graph.scan()) {
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

    public static <T extends Annotation> List<AnnotatedMethod<?>> getAnnotatedMethods(Class<T> clazz,
        String packageName) {
        ClassGraph graph = new ClassGraph().acceptPackages(packageName).enableAllInfo();

        List<AnnotatedMethod<?>> resultList = new ArrayList<>();
        try (ScanResult result = graph.scan()) {
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
