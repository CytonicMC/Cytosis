package net.cytonic.cytosis;

import io.github.classgraph.ClassGraph;
import net.cytonic.cytosis.bootstrap.annotations.CytosisComponent;
import net.cytonic.cytosis.config.CytosisSettings;
import net.cytonic.cytosis.events.EventHandler;
import net.cytonic.cytosis.events.EventListener;
import net.cytonic.cytosis.events.api.Async;
import net.cytonic.cytosis.events.api.Listener;
import net.cytonic.cytosis.events.api.Priority;
import net.cytonic.cytosis.files.FileManager;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.messaging.NatsManager;
import net.cytonic.cytosis.metrics.MetricsHooks;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.plugins.loader.PluginClassLoader;
import net.cytonic.cytosis.utils.BlockPlacementUtils;
import net.minestom.server.Auth;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.event.Event;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.listener.manager.PacketListenerManager;
import net.minestom.server.network.packet.client.play.ClientCommandChatPacket;
import net.minestom.server.network.packet.client.play.ClientSignedCommandChatPacket;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

/**
 * Main bootstrap class responsible for initializing and starting the Cytosis server.
 * This class orchestrates the entire server start-up process, including configuration,
 * component registration, server initialization, and runtime settings.
 */
public class CytosisBootstrap {
    public static final String SCAN_PACKAGE_ROOT = "net.cytonic";
    private final CytosisContext cytosisContext;
    private final List<String> argList;

    public CytosisBootstrap(String[] args, CytosisContext context) {
        this.argList = List.of(args);
        this.cytosisContext = context;
    }

    public void run() {
        long startTime = System.currentTimeMillis();
        Logger.info("Starting Cytosis server...");

        cytosisContext.setFlags(argList);
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            try {
                Logger.error("Uncaught exception in thread " + t.getName(), e);
            } catch (Exception e1) {
                e1.printStackTrace(System.err);
            }
        });

        applySystemSettings();
        initMinestom();
        registerCytosisComponents();
        initWorld();

        Logger.info("Initializing view registry");
        Cytosis.VIEW_REGISTRY.enable();

        if (cytosisContext.isMetricsEnabled()) {
            Logger.info("Starting metric hooks");
            MetricsHooks.init();
        }

        // TODO: do only one of those , both are executing otherwise?
        Runtime.getRuntime().addShutdownHook(new Thread(cytosisContext::shutdownHandler));
        MinecraftServer.getSchedulerManager().buildShutdownTask(cytosisContext::shutdownHandler);

        registerListeners();
        cytosisContext.getComponent(EventHandler.class).init();

        startServer();

        long end = System.currentTimeMillis();
        Logger.info("Server started in " + (end - startTime) + "ms!");
        Logger.info("Server id = " + CytosisContext.SERVER_ID);

        if (cytosisContext.getFlags().contains("--ci-test")) {
            Logger.info("Stopping server due to '--ci-test' flag.");
            MinecraftServer.stopCleanly();
        }
    }

    /**
     * Applies system settings and loads environment.
     */
    private void applySystemSettings() {
        System.setProperty("org.jooq.no-logo", "true");
        System.setProperty("org.jooq.no-tips", "true");
        Logger.info("Creating file manager");
        Logger.info("Initializing file manager");
        cytosisContext.registerComponent(new FileManager());
        Logger.info("Loading Cytosis Settings");
        CytosisSettings.loadEnvironmentVariables();
        CytosisSettings.loadCommandArgs();
    }

    /**
     * Initializes and configures the Minestom server components required for the application.
     */
    private void initMinestom() {
        cytosisContext.registerComponent(MinecraftServer.init(new Auth.Velocity(CytosisSettings.SERVER_SECRET)));
        MinecraftServer.getConnectionManager().setPlayerProvider(CytosisPlayer::new);
        MinecraftServer.setBrandName("Cytosis");
        MinecraftServer.getBenchmarkManager().enable(Duration.ofSeconds(10L));

        Logger.info("Starting instance managers.");
        InstanceManager minestomInstanceManager = cytosisContext.registerComponent(MinecraftServer.getInstanceManager());
        Logger.info("Starting connection manager.");
        cytosisContext.registerComponent(MinecraftServer.getConnectionManager());
        Logger.info("Starting command manager.");
        CommandManager commandManager = cytosisContext.registerComponent(MinecraftServer.getCommandManager());
        Logger.info("Setting console command sender.");
        cytosisContext.registerComponent(commandManager.getConsoleSender());
        Logger.info("Creating instance container");
        cytosisContext.registerComponent(minestomInstanceManager.createInstanceContainer());
    }

    /**
     * Scan for annotated classes and register them as components in the cytosis context.
     * Using Kahn's Algorithm for Topological Sorting, sort the components by their dependencies.
     * Register them in the order they were sorted.
     *
     * @see <a href="https://en.wikipedia.org/wiki/Topological_sorting">Topological Sorting</a>
     */
    private void registerCytosisComponents() {
        Logger.info("Auto-registering Cytosis components...");
        List<ClassLoader> loaders = new ArrayList<>();
        loaders.add(Cytosis.class.getClassLoader());
        loaders.addAll(PluginClassLoader.loaders);

        ClassGraph graph = new ClassGraph()
            .acceptPackages(SCAN_PACKAGE_ROOT)
            .enableClassInfo()
            .enableAnnotationInfo()
            .overrideClassLoaders(loaders.toArray(new ClassLoader[0]));

        List<Class<?>> candidates = new ArrayList<>();
        // scan for annotated classes
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
        if (candidates.isEmpty()) return;

        // get annotated components and their annotations
        Map<Class<?>, CytosisComponent> annotatedComponents = new HashMap<>(candidates.size());
        for (Class<?> c : candidates) {
            annotatedComponents.put(c, c.getAnnotation(CytosisComponent.class));
        }

        // dependency graph
        Map<Class<?>, Set<Class<?>>> dependencies = new HashMap<>(candidates.size()); // candidate -> required candidate dependencies
        Map<Class<?>, List<Class<?>>> reverseDependencies = new HashMap<>(candidates.size()); // candidate -> dependencies
        Map<Class<?>, Integer> componentNeighbours = new HashMap<>(candidates.size());

        // to check if dependency is already satisfied externally
        Predicate<Class<?>> satisfiedExternally = d -> cytosisContext.getComponent(d) != null;

        for (Class<?> candidate : candidates) {
            CytosisComponent component = annotatedComponents.get(candidate);
            Set<Class<?>> required = new HashSet<>();
            if (component != null) {
                for (Class<?> d : component.dependsOn()) {
                    if (candidates.contains(d)) {
                        required.add(d);
                    } else if (!satisfiedExternally.test(d)) {
                        // should be kept unsatisfied as it does not pass the test
                        required.add(d);
                    }
                }
            }
            dependencies.put(candidate, required);
            componentNeighbours.put(candidate, 0);
        }

        for (Class<?> c : candidates) {
            for (Class<?> d : dependencies.get(c)) {
                if (candidates.contains(d)) {
                    componentNeighbours.put(c, componentNeighbours.get(c) + 1);
                    reverseDependencies.computeIfAbsent(d, k -> new ArrayList<>()).add(c);
                } else {
                    componentNeighbours.put(c, componentNeighbours.get(c) + 1);
                }
            }
        }

        Comparator<Class<?>> componentsComparator = (a, b) -> {
            int componentPriorityA = annotatedComponents.get(a).priority();
            int componentPriorityB = annotatedComponents.get(b).priority();
            int compared = Integer.compare(componentPriorityA, componentPriorityB);
            return compared != 0 ? compared : a.getName().compareTo(b.getName());
        };
        PriorityQueue<Class<?>> ready = new PriorityQueue<>(componentsComparator);
        for (Class<?> candidate : candidates) {
            if (componentNeighbours.get(candidate) == 0)
                ready.add(candidate);
        }

        Set<Class<?>> registered = new HashSet<>();
        while (!ready.isEmpty()) {
            Class<?> c = ready.poll();
            try {
                var ctor = c.getDeclaredConstructor();
                ctor.setAccessible(true);
                cytosisContext.registerComponent((Class) c, ctor.newInstance());
                registered.add(c);
                Logger.info("Auto-registered component: " + c.getSimpleName());
            } catch (Throwable t) {
                Logger.error("Failed to auto-register component: " + c.getName(), t);
                continue;
            }

            List<Class<?>> componentReverseDependencies = reverseDependencies.getOrDefault(c, Collections.emptyList());
            for (Class<?> dependency : componentReverseDependencies) {
                int mergeResult = componentNeighbours.merge(dependency, -1, Integer::sum);
                if (mergeResult == 0) ready.add(dependency);
            }
        }

        if (registered.size() < candidates.size()) {
            List<String> missing = new ArrayList<>();
            for (Class<?> candidate : candidates) {
                if (registered.contains(candidate)) continue;

                Set<Class<?>> reqs = dependencies.getOrDefault(candidate, Collections.emptySet());
                List<String> unmet = new ArrayList<>();
                for (Class<?> d : reqs) {
                    if (candidates.contains(d)) {
                        if (!registered.contains(d))
                            unmet.add(d.getName());
                    } else if (cytosisContext.getComponent(d) == null) {
                        unmet.add(d.getName());
                    }
                }
                missing.add(candidate.getName() + " -> missing: " + unmet);
            }
            Logger.error("Could not resolve dependencies for some Cytosis components: " + missing);
        } else {
            Logger.info("Finished auto-registering Cytosis components (" + registered.size() + ")");
        }
    }

    /**
     * Initializes the world by setting up the necessary components and configurations required
     * for the world to run properly.
     */
    private void initWorld() {
        Logger.info("Initializing block placements");
        BlockPlacementUtils.init();
        Logger.info("Adding a singed command packet handler");
        MinecraftServer.getPacketListenerManager().setPlayListener(ClientSignedCommandChatPacket.class, (packet, p) ->
            MinecraftServer.getPacketListenerManager()
                .processClientPacket(new ClientCommandChatPacket(packet.message()), p.getPlayerConnection(),
                    p.getPlayerConnection().getConnectionState()));

        Thread.ofVirtual().name("Cytosis-WorldLoader").start(Cytosis::loadWorld);
    }

    /**
     * Registers all annotated listeners in the cytosis plugin and external plugins.
     */
    private void registerListeners() {
        long start2 = System.currentTimeMillis();
        Logger.info("Scanning for listeners in plugins!");

        List<ClassLoader> loaders = new ArrayList<>();
        loaders.add(Cytosis.class.getClassLoader());
        loaders.addAll(PluginClassLoader.loaders);

        ClassGraph graph = new ClassGraph()
            .acceptPackages(SCAN_PACKAGE_ROOT)
            .enableAllInfo()
            .overrideClassLoaders(loaders.toArray(new ClassLoader[0]));

        AtomicInteger counter = new AtomicInteger(0);
        EventHandler eventHandler = cytosisContext.getComponent(EventHandler.class);
        try (var scanResult = graph.scan()) {
            scanResult
                .getClassesWithMethodAnnotation(Listener.class.getName())
                .forEach(classInfo -> {
                    Class<?> clazz = classInfo.loadClass();
                    Object instance;
                    try {
                        Constructor<?> constructor = clazz.getDeclaredConstructor();
                        constructor.setAccessible(true);
                        instance = constructor.newInstance();
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                             NoSuchMethodException e) {
                        Logger.error("The class " + clazz.getSimpleName() + " needs to have a public, no argument constructor to have an @Listener in it!", e);
                        return;
                    }

                    for (Method method : clazz.getDeclaredMethods()) {
                        if (method.isAnnotationPresent(Listener.class)) {
                            method.setAccessible(true);
                            int priority = method.isAnnotationPresent(Priority.class) ? method.getAnnotation(Priority.class).value() : 50;
                            boolean async = method.isAnnotationPresent(Async.class);

                            Class<? extends Event> eventClass;
                            try {
                                eventClass = (Class<? extends Event>) method.getParameterTypes()[0];
                            } catch (ClassCastException e) {
                                Logger.error("The parameter of a method annotated with @Listener must be a valid event!", e);
                                return;
                            } catch (ArrayIndexOutOfBoundsException e) {
                                Logger.error("Methods annotated with @Listener must have a valid event as a parameter!", e);
                                return;
                            }

                            eventHandler.registerListener(new EventListener<>(
                                "cytosis:annotation-listener-" + counter.getAndIncrement(),
                                async, priority, (Class<Event>) eventClass, event -> {
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
                            ));
                        }
                    }
                });
        }
        Logger.info("Finished scanning for listeners in plugins in " + (System.currentTimeMillis() - start2) + "ms!");
    }

    /**
     * Starts the minestom server.
     */
    private void startServer() {
        Logger.info("Server started on port " + CytosisSettings.SERVER_PORT);
        cytosisContext.getComponent(MinecraftServer.class).start("0.0.0.0", CytosisSettings.SERVER_PORT);
        MinecraftServer.getExceptionManager().setExceptionHandler(e -> Logger.error("Uncaught exception: ", e));

        cytosisContext.getComponent(NatsManager.class).sendStartup();
    }
}