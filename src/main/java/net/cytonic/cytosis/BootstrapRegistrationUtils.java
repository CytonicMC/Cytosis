package net.cytonic.cytosis;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import io.github.classgraph.ClassGraph;
import net.minestom.server.event.Event;

import net.cytonic.cytosis.bootstrap.annotations.CytosisComponent;
import net.cytonic.cytosis.events.EventHandler;
import net.cytonic.cytosis.events.EventListener;
import net.cytonic.cytosis.events.api.Async;
import net.cytonic.cytosis.events.api.Listener;
import net.cytonic.cytosis.events.api.Priority;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.plugins.loader.PluginClassLoader;

/**
 * Utility class for registering Cytosis components and listeners.
 */
public final class BootstrapRegistrationUtils {

    private BootstrapRegistrationUtils() {
    }

    /**
     * Scan for annotated classes and register them as components in the cytosis context. Using Kahn's Algorithm for
     * Topological Sorting, sort the components by their dependencies. Register them in the order they were sorted.
     *
     * @see <a href="https://en.wikipedia.org/wiki/Topological_sorting">Topological Sorting</a>
     */
    public static void registerCytosisComponents(CytosisContext cytosisContext) {
        Logger.info("Auto-registering Cytosis components...");

        List<Class<?>> candidates = scanAnnotatedComponents();
        if (candidates.isEmpty()) {
            return;
        }

        Map<Class<?>, CytosisComponent> annotatedComponents = extractAnnotations(candidates);

        Map<Class<?>, Set<Class<?>>> dependencies = new HashMap<>(candidates.size());
        Map<Class<?>, List<Class<?>>> reverseDependencies = new HashMap<>(candidates.size());
        Map<Class<?>, Integer> componentNeighbours = new HashMap<>(candidates.size());

        buildDependencyGraph(candidates, annotatedComponents, dependencies, reverseDependencies, componentNeighbours,
            cytosisContext);

        PriorityQueue<Class<?>> ready = initializeReadyQueue(candidates, annotatedComponents, componentNeighbours);

        Set<Class<?>> registered = processComponentRegistration(ready, reverseDependencies, componentNeighbours,
            cytosisContext);

        validateRegistration(candidates, registered, dependencies, cytosisContext);
    }

    /**
     * Scans the classpath for classes annotated with @CytosisComponent.
     *
     * @return list of candidate component classes
     */
    private static List<Class<?>> scanAnnotatedComponents() {
        List<Class<?>> candidates = new ArrayList<>();
        ClassGraph graph = new ClassGraph().acceptPackages(CytosisBootstrap.SCAN_PACKAGE_ROOT)
            .enableAnnotationInfo()
            .enableClassInfo();
        try (var scanResult = graph.scan()) {
            var classInfos = scanResult.getClassesWithAnnotation(CytosisComponent.class.getName());
            for (var classInfo : classInfos) {
                try {
                    candidates.add(classInfo.loadClass());
                } catch (Throwable t) {
                    Logger.error("Failed to load annotated component class " + classInfo.getName(), t);
                }
            }
        }
        return candidates;
    }

    /**
     * Extracts annotations from candidate component classes.
     *
     * @param candidates list of candidate classes
     * @return map of classes to their annotations
     */
    private static Map<Class<?>, CytosisComponent> extractAnnotations(List<Class<?>> candidates) {
        Map<Class<?>, CytosisComponent> annotatedComponents = new HashMap<>(candidates.size());
        for (Class<?> c : candidates) {
            annotatedComponents.put(c, c.getAnnotation(CytosisComponent.class));
        }
        return annotatedComponents;
    }

    /**
     * Builds the dependency graph for component registration.
     *
     * @param candidates          list of candidate classes
     * @param annotatedComponents map of classes to their annotations
     * @param dependencies        output map of dependencies
     * @param reverseDependencies output map of reverse dependencies
     * @param componentNeighbours output map of neighbour counts
     */
    private static void buildDependencyGraph(
        List<Class<?>> candidates,
        Map<Class<?>, CytosisComponent> annotatedComponents,
        Map<Class<?>, Set<Class<?>>> dependencies,
        Map<Class<?>, List<Class<?>>> reverseDependencies,
        Map<Class<?>, Integer> componentNeighbours,
        CytosisContext cytosisContext) {

        Predicate<Class<?>> satisfiedExternally = d -> cytosisContext.getComponent(d) != null;

        for (Class<?> candidate : candidates) {
            Set<Class<?>> required = computeRequiredDependencies(candidate, candidates, annotatedComponents,
                satisfiedExternally);
            dependencies.put(candidate, required);
            componentNeighbours.put(candidate, 0);
        }

        for (Class<?> c : candidates) {
            for (Class<?> d : dependencies.get(c)) {
                componentNeighbours.put(c, componentNeighbours.get(c) + 1);
                if (candidates.contains(d)) {
                    reverseDependencies.computeIfAbsent(d, k -> new ArrayList<>()).add(c);
                }
            }
        }
    }

    /**
     * Computes required dependencies for a component.
     *
     * @param candidate           the candidate class
     * @param candidates          all candidate classes
     * @param annotatedComponents map of classes to their annotations
     * @param satisfiedExternally predicate to check if dependency is satisfied externally
     * @return set of required dependencies
     */
    private static Set<Class<?>> computeRequiredDependencies(
        Class<?> candidate,
        List<Class<?>> candidates,
        Map<Class<?>, CytosisComponent> annotatedComponents,
        Predicate<Class<?>> satisfiedExternally) {

        CytosisComponent component = annotatedComponents.get(candidate);
        Set<Class<?>> required = new HashSet<>();

        if (component != null) {
            for (Class<?> d : component.dependsOn()) {
                if (candidates.contains(d)) {
                    required.add(d);
                } else if (!satisfiedExternally.test(d)) {
                    required.add(d);
                }
            }
        }

        return required;
    }

    /**
     * Initializes the priority queue with components that have no dependencies.
     *
     * @param candidates          list of candidate classes
     * @param annotatedComponents map of classes to their annotations
     * @param componentNeighbours map of neighbour counts
     * @return priority queue of ready components
     */
    private static PriorityQueue<Class<?>> initializeReadyQueue(
        List<Class<?>> candidates,
        Map<Class<?>, CytosisComponent> annotatedComponents,
        Map<Class<?>, Integer> componentNeighbours) {

        Comparator<Class<?>> componentsComparator = (a, b) -> {
            int componentPriorityA = annotatedComponents.get(a).priority();
            int componentPriorityB = annotatedComponents.get(b).priority();
            int compared = Integer.compare(componentPriorityA, componentPriorityB);
            return compared != 0 ? compared : a.getName().compareTo(b.getName());
        };

        PriorityQueue<Class<?>> ready = new PriorityQueue<>(componentsComparator);
        for (Class<?> candidate : candidates) {
            if (componentNeighbours.get(candidate) == 0) {
                ready.add(candidate);
            }
        }

        return ready;
    }

    /**
     * Processes component registration using topological sorting.
     *
     * @param ready               priority queue of ready components
     * @param reverseDependencies map of reverse dependencies
     * @param componentNeighbours map of neighbour counts
     * @return set of successfully registered components
     */
    private static Set<Class<?>> processComponentRegistration(
        PriorityQueue<Class<?>> ready,
        Map<Class<?>, List<Class<?>>> reverseDependencies,
        Map<Class<?>, Integer> componentNeighbours,
        CytosisContext cytosisContext) {

        Set<Class<?>> registered = new HashSet<>();

        while (!ready.isEmpty()) {
            Class<?> c = ready.poll();

            if (registerComponent(cytosisContext, c)) {
                registered.add(c);
                updateDependentComponents(c, reverseDependencies, componentNeighbours, ready);
            }
        }

        return registered;
    }

    /**
     * Registers a single component.
     *
     * @param componentClass the component class to register
     * @return true if registration was successful
     */
    private static boolean registerComponent(CytosisContext cytosisContext, Class<?> componentClass) {
        try {
            var ctor = componentClass.getDeclaredConstructor();
            ctor.setAccessible(true);
            cytosisContext.registerComponent((Class) componentClass, ctor.newInstance());
            Logger.info("Auto-registered component: " + componentClass.getSimpleName());
            return true;
        } catch (Throwable t) {
            Logger.error("Failed to auto-register component: " + componentClass.getName(), t);
            return false;
        }
    }

    /**
     * Updates dependent components after a component is registered.
     *
     * @param registeredComponent the component that was just registered
     * @param reverseDependencies map of reverse dependencies
     * @param componentNeighbours map of neighbour counts
     * @param ready               priority queue of ready components
     */
    private static void updateDependentComponents(
        Class<?> registeredComponent,
        Map<Class<?>, List<Class<?>>> reverseDependencies,
        Map<Class<?>, Integer> componentNeighbours,
        PriorityQueue<Class<?>> ready) {

        List<Class<?>> componentReverseDependencies = reverseDependencies.getOrDefault(registeredComponent,
            Collections.emptyList());
        for (Class<?> dependency : componentReverseDependencies) {
            int mergeResult = componentNeighbours.merge(dependency, -1, Integer::sum);
            if (mergeResult == 0) {
                ready.add(dependency);
            }
        }
    }

    /**
     * Validates the registration results and logs any unresolved dependencies.
     *
     * @param candidates   list of all candidate classes
     * @param registered   set of successfully registered components
     * @param dependencies map of dependencies
     */
    private static void validateRegistration(
        List<Class<?>> candidates,
        Set<Class<?>> registered,
        Map<Class<?>, Set<Class<?>>> dependencies,
        CytosisContext cytosisContext) {

        if (registered.size() < candidates.size()) {
            List<String> missing = collectUnregisteredComponents(candidates, registered, dependencies, cytosisContext);
            Logger.error("Could not resolve dependencies for some Cytosis components: " + missing);
        } else {
            Logger.info("Finished auto-registering Cytosis components (" + registered.size() + ")");
        }
    }

    /**
     * Collects information about unregistered components and their missing dependencies.
     *
     * @param candidates   list of all candidate classes
     * @param registered   set of successfully registered components
     * @param dependencies map of dependencies
     * @return list of error messages for unregistered components
     */
    private static List<String> collectUnregisteredComponents(
        List<Class<?>> candidates,
        Set<Class<?>> registered,
        Map<Class<?>, Set<Class<?>>> dependencies,
        CytosisContext cytosisContext) {

        List<String> missing = new ArrayList<>();

        for (Class<?> candidate : candidates) {
            if (registered.contains(candidate)) {
                continue;
            }

            Set<Class<?>> reqs = dependencies.getOrDefault(candidate, Collections.emptySet());
            List<String> unmet = new ArrayList<>();

            for (Class<?> d : reqs) {
                if (candidates.contains(d)) {
                    if (!registered.contains(d)) {
                        unmet.add(d.getName());
                    }
                } else if (cytosisContext.getComponent(d) == null) {
                    unmet.add(d.getName());
                }
            }

            missing.add(candidate.getName() + " -> missing: " + unmet);
        }

        return missing;
    }

    /**
     * Registers all annotated listeners in the cytosis plugin and external plugins.
     */
    public static void registerListeners(CytosisContext cytosisContext) {
        long start2 = System.currentTimeMillis();
        Logger.info("Scanning for listeners in plugins!");

        ClassGraph graph = createListenerClassGraph();
        AtomicInteger counter = new AtomicInteger(0);
        EventHandler eventHandler = cytosisContext.getComponent(EventHandler.class);

        scanAndRegisterListeners(graph, eventHandler, counter);

        Logger.info("Finished scanning for listeners in plugins in " + (System.currentTimeMillis() - start2) + "ms!");
    }

    /**
     * Creates and configures the ClassGraph for listener scanning.
     *
     * @return configured ClassGraph instance
     */
    private static ClassGraph createListenerClassGraph() {
        List<ClassLoader> loaders = new ArrayList<>();
        loaders.add(Cytosis.class.getClassLoader());
        loaders.addAll(PluginClassLoader.LOADERS);

        return new ClassGraph()
            .acceptPackages(CytosisBootstrap.SCAN_PACKAGE_ROOT)
            .enableAllInfo()
            .overrideClassLoaders(loaders.toArray(new ClassLoader[0]));
    }

    /**
     * Scans for and registers all listener methods.
     *
     * @param graph        the ClassGraph to scan with
     * @param eventHandler the event handler to register listeners with
     * @param counter      atomic counter for listener naming
     */
    private static void scanAndRegisterListeners(ClassGraph graph, EventHandler eventHandler, AtomicInteger counter) {
        try (var scanResult = graph.scan()) {
            scanResult
                .getClassesWithMethodAnnotation(Listener.class.getName())
                .forEach(classInfo -> processListenerClass(classInfo, eventHandler, counter));
        }
    }

    /**
     * Processes a class containing listener methods.
     *
     * @param classInfo    information about the class to process
     * @param eventHandler the event handler to register listeners with
     * @param counter      atomic counter for listener naming
     */
    private static void processListenerClass(
        io.github.classgraph.ClassInfo classInfo,
        EventHandler eventHandler,
        AtomicInteger counter) {

        Class<?> clazz = classInfo.loadClass();
        Object instance = createListenerInstance(clazz);
        if (instance == null) {
            return;
        }

        registerListenerMethods(clazz, instance, eventHandler, counter);
    }

    /**
     * Creates an instance of the listener class.
     *
     * @param clazz the class to instantiate
     * @return instance of the class, or null if instantiation failed
     */
    private static Object createListenerInstance(Class<?> clazz) {
        try {
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            Logger.error("The class " + clazz.getSimpleName()
                + " needs to have a public, no argument constructor to have an @Listener in it!", e);
            return null;
        }
    }

    /**
     * Registers all listener methods in a class.
     *
     * @param clazz        the class containing listener methods
     * @param instance     instance of the class
     * @param eventHandler the event handler to register listeners with
     * @param counter      atomic counter for listener naming
     */
    private static void registerListenerMethods(
        Class<?> clazz,
        Object instance,
        EventHandler eventHandler,
        AtomicInteger counter) {

        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Listener.class)) {
                registerListenerMethod(method, instance, eventHandler, counter);
            }
        }
    }

    /**
     * Registers a single listener method.
     *
     * @param method       the method to register
     * @param instance     instance containing the method
     * @param eventHandler the event handler to register the listener with
     * @param counter      atomic counter for listener naming
     */
    private static void registerListenerMethod(
        Method method,
        Object instance,
        EventHandler eventHandler,
        AtomicInteger counter) {

        method.setAccessible(true);
        int priority = extractListenerPriority(method);
        boolean async = method.isAnnotationPresent(Async.class);

        Class<? extends Event> eventClass = extractEventClass(method);
        if (eventClass == null) {
            return;
        }

        eventHandler.registerListener(createEventListener(
            method, instance, eventClass, async, priority, counter
        ));
    }

    /**
     * Extracts the priority from a listener method.
     *
     * @param method the method to extract priority from
     * @return the priority value, or 50 if not specified
     */
    private static int extractListenerPriority(Method method) {
        return method.isAnnotationPresent(Priority.class)
            ? method.getAnnotation(Priority.class).value()
            : 50;
    }

    /**
     * Extracts the event class from a listener method's parameter.
     *
     * @param method the method to extract the event class from
     * @return the event class, or null if extraction failed
     */
    private static Class<? extends Event> extractEventClass(Method method) {
        try {
            return (Class<? extends Event>) method.getParameterTypes()[0];
        } catch (ClassCastException e) {
            Logger.error(
                "The parameter of a method annotated with @Listener must be a valid event!", e);
            return null;
        } catch (ArrayIndexOutOfBoundsException e) {
            Logger.error("Methods annotated with @Listener must have a valid event as a parameter!", e);
            return null;
        }
    }

    /**
     * Creates an EventListener for a method.
     *
     * @param method     the listener method
     * @param instance   instance containing the method
     * @param eventClass the event class
     * @param async      whether the listener is async
     * @param priority   the listener priority
     * @param counter    atomic counter for listener naming
     * @return the created EventListener
     */
    @SuppressWarnings("unchecked")
    private static EventListener<Event> createEventListener(
        Method method,
        Object instance,
        Class<? extends Event> eventClass,
        boolean async,
        int priority,
        AtomicInteger counter) {

        return new EventListener<>(
            "cytosis:annotation-listener-" + counter.getAndIncrement(),
            async,
            priority,
            (Class<Event>) eventClass,
            event -> invokeListenerMethod(method, instance, event)
        );
    }

    /**
     * Invokes a listener method with the given event.
     *
     * @param method   the method to invoke
     * @param instance the instance to invoke the method on
     * @param event    the event to pass to the method
     */
    private static void invokeListenerMethod(Method method, Object instance, Event event) {
        try {
            method.invoke(instance, event);
        } catch (IllegalAccessException e) {
            Logger.error("Failed to call @Listener!", e);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause != null) {
                Logger.error("Exception in @Listener method: ", cause);
            } else {
                Logger.error("Unknown error in @Listener method.", e);
            }
        }
    }
}
