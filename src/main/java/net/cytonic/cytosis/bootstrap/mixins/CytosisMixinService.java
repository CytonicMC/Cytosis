package net.cytonic.cytosis.bootstrap.mixins;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.launch.platform.container.ContainerHandleVirtual;
import org.spongepowered.asm.launch.platform.container.IContainerHandle;
import org.spongepowered.asm.logging.ILogger;
import org.spongepowered.asm.mixin.MixinEnvironment.Phase;
import org.spongepowered.asm.service.*;
import org.spongepowered.asm.util.IConsumer;

import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Callable;

public class CytosisMixinService extends MixinServiceAbstract {
    private static final CytosisRootClassLoader CLASSLOADER = CytosisRootClassLoader.getInstance();
    private final IClassBytecodeProvider bytecodeProvider = new IClassBytecodeProvider() {
        @Override
        public ClassNode getClassNode(String name) throws ClassNotFoundException {
            return this.getClassNode(name, false);
        }

        @Override
        public ClassNode getClassNode(String name, boolean runTransformers) throws ClassNotFoundException {
            return this.getClassNode(name, runTransformers, 0);
        }

        @Override
        public ClassNode getClassNode(String name, boolean runTransformers, int readerFlags) throws ClassNotFoundException {
            List<Exception> caughtExceptions = new ArrayList<>();

            @SuppressWarnings("unchecked")
            Callable<@NotNull ClassReader>[] suppliers = new Callable[4];

            int systemClassLoaderIndex;
            systemClassLoaderIndex = suppliers.length - 1;

            suppliers[systemClassLoaderIndex] = () -> {
                ClassLoader cl = this.getClass().getClassLoader();
                InputStream is;
                if (cl == null) {
                    is = ClassLoader.getSystemResourceAsStream(name.replace('.', '/') + ".class");
                } else {
                    try (InputStream stream = cl.getResourceAsStream(name.replace('.', '/') + ".class")) {
                        is = stream;
                    }
                }
                return new ClassReader(Objects.requireNonNull(is));
            };

            suppliers[0] = () -> new ClassReader(CytosisMixinService.CLASSLOADER.loadBytes(name, false));

            suppliers[1] = () -> new ClassReader(Objects.requireNonNull(CytosisMixinService.CLASSLOADER.getResourceAsStream(name.replace('.', '/') + ".class")));

            for (Callable<@NotNull ClassReader> supplier : suppliers) {
                try {
                    @SuppressWarnings("null")
                    ClassReader reader = supplier.call();
                    ClassNode node = new ClassNode();
                    reader.accept(node, readerFlags);
                    return node;
                } catch (Exception e) {
                    caughtExceptions.add(e);
                }
            }

            Exception causedBy;
            ListIterator<Exception> it = caughtExceptions.listIterator(caughtExceptions.size());
            if (it.hasPrevious()) {
                causedBy = it.previous();
                while (it.hasPrevious()) {
                    causedBy.addSuppressed(it.previous());
                }
            } else {
                causedBy = null;
            }

            ClassNotFoundException thrownException = new ClassNotFoundException("Could not load ClassNode with name " + name, causedBy);

            thrownException.fillInStackTrace();
            LoggerFactory.getLogger(CytosisMixinService.class).warn("Unable to call #getClassNode(): Couldn't load ClassNode for class with name '{}'.", name, thrownException);

            throw thrownException;
        }
    };

    private final IClassProvider classProvider = new IClassProvider() {

        @Override
        public Class<?> findAgentClass(String name, boolean initialize) {
            throw new RuntimeException("Agent class loading is not supported");
        }

        @Override
        public Class<?> findClass(String name) throws ClassNotFoundException {
            try {
                return CytosisMixinService.CLASSLOADER.findClass(name);
            } catch (ClassNotFoundException e) {
                LoggerFactory.getLogger(CytosisMixinService.class).warn("#findClass(String): Unable to find class '{}'", name, e);
                throw e;
            }
        }

        @Override
        public Class<?> findClass(String name, boolean initialize) throws ClassNotFoundException {
            try {
                return Class.forName(name, initialize, Thread.currentThread().getContextClassLoader());
            } catch (ClassNotFoundException e) {
                try {
                    return Class.forName(name, initialize, CytosisMixinService.class.getClassLoader());
                } catch (ClassNotFoundException e2) {
                    e2.addSuppressed(e);
                    LoggerFactory.getLogger(CytosisMixinService.class).warn("#findClass(String, boolean): Unable to find class '{}'", name, e2);
                    throw e2;
                }
            }
        }

        @Override
        public URL[] getClassPath() {
            return CytosisMixinService.CLASSLOADER.getURLs();
        }
    };

    @Override
    protected ILogger createLogger(String name) {
        return new CytosisMixinLogger(Objects.requireNonNull(name, "logger may not have a null name"));
    }

    @Override
    public IMixinAuditTrail getAuditTrail() {
        return null; // unsupported
    }

    @Override
    public IClassBytecodeProvider getBytecodeProvider() {
        return this.bytecodeProvider;
    }

    @Override
    public IClassProvider getClassProvider() {
        return this.classProvider;
    }

    @Override
    public IClassTracker getClassTracker() {
        return null; // unsupported
    }

    @Nullable
    public final <T extends IMixinInternal> T getMixinInternal(Class<T> type) {
        return this.getInternal(type);
    }

    @Override
    public String getName() {
        return "Cytosis Bootstrap";
    }

    @Override
    public Collection<String> getPlatformAgents() {
        return List.of("net.cytonic.cytosis.bootstrap.mixins.CytosisPlatformAgent");
    }

    @Override
    public IContainerHandle getPrimaryContainer() {
        return new ContainerHandleVirtual(this.getName());
    }

    @Override
    public InputStream getResourceAsStream(@NotNull String name) {
        return CLASSLOADER.getResourceAsStream(Objects.requireNonNull(name, "name may not be null"));
    }

    @Override
    public ITransformerProvider getTransformerProvider() {
        return null; // unsupported
    }

    @Override
    public void init() {
        super.init();
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void unwire() {
        super.unwire();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void wire(Phase phase, IConsumer<Phase> phaseConsumer) {
        super.wire(phase, phaseConsumer);
    }
}