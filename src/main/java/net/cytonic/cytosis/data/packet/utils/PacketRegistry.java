package net.cytonic.cytosis.data.packet.utils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import lombok.Getter;

import net.cytonic.cytosis.Bootstrappable;
import net.cytonic.cytosis.CytonicNetwork;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.bootstrap.annotations.CytosisComponent;
import net.cytonic.cytosis.logging.Logger;

@Getter
@CytosisComponent(dependsOn = CytonicNetwork.class, priority = Integer.MAX_VALUE)
public class PacketRegistry implements Bootstrappable {

    private final Map<String, Map<Class<? extends Packet<?>>, List<HandlerMethod>>> handlers = new HashMap<>();

    @Override
    public void init() {
        ClassGraph classGraph = new ClassGraph().acceptPackages("net.cytonic").enableAllInfo();
        try (ScanResult scanResult = classGraph.scan()) {
            scanResult.getClassesWithMethodAnnotation(PacketHandler.class).loadClasses().forEach(foundClass -> {
                for (Method method : foundClass.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(PacketHandler.class)) {
                        registerHandler(foundClass, method);
                    }
                }
            });
        } catch (Exception e) {
            Logger.error("Error scanning for packet handlers", e);
        }
    }

    private void registerHandler(Class<?> foundClass, Method method) {
        if (method.getParameterCount() != 1 && method.getParameterCount() != 2) {
            Logger.error("Method " + method.getName()
                + " must have exactly 1 or 2 parameters, with the second parameter being PacketData");
            return;
        }

        Class<?> packetParamType = method.getParameterTypes()[0];
        if (!Packet.class.isAssignableFrom(packetParamType)) {
            Logger.error("Method " + method.getName() + " parameter 1 must be a Packet type");
            return;
        }
        if (method.getParameterCount() == 2) {
            Class<?> packetDataParamType = method.getParameterTypes()[1];
            if (!PacketData.class.isAssignableFrom(packetDataParamType)) {
                Logger.error("Method " + method.getName() + " parameter 2 must be PacketData");
                return;
            }
        }

        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        Class<? extends Packet<?>> packetType = (Class<? extends Packet<?>>) packetParamType;

        PacketHandler annotation = method.getAnnotation(PacketHandler.class);
        String subject = annotation.subject();

        // If no subject specified, use the packet's default channel name
        if (subject.isEmpty()) {
            try {
                Packet<?> tempInstance = packetType.getDeclaredConstructor().newInstance();
                subject = tempInstance.getSubject();
            } catch (IllegalSubjectException e) {
                Logger.error("Failed to register PacketHandler due to registering a reusable packet without a subject");
            } catch (Exception e) {
                // Fallback to the simple class name
                Logger.warn("Could not instantiate packet handler: " + method.getName(), e);
                subject = packetType.getSimpleName();
            }
        }

        handlers.computeIfAbsent(subject, _ -> new HashMap<>())
            .computeIfAbsent(packetType, _ -> new ArrayList<>())
            .add(new HandlerMethod(foundClass, method, method.getParameterCount() == 2));

        Logger.info(
            "Registered packet handler: " + method.getName() + " for " + packetType.getSimpleName() + " on subject: "
                + subject);
    }

    public <P extends Packet<P>> void callHandlers(String subject, Packet<P> packet, PacketData packetData) {
        Map<Class<? extends Packet<?>>, List<HandlerMethod>> subjectHandlers = handlers.get(subject);
        if (subjectHandlers == null) {
            return;
        }

        List<HandlerMethod> handlerList = subjectHandlers.get(packet.getClass());
        if (handlerList == null) {
            return;
        }

        for (HandlerMethod handler : handlerList) {
            try {
                // Lazily fetch the instance from Cytosis
                Object instance = Cytosis.get(handler.handlerClass);
                if (instance == null) {
                    Logger.warn("Could not call handler " + handler.method.getName() + " because component "
                        + handler.handlerClass.getSimpleName() + " is not registered!");
                    continue;
                }
                if (handler.hasPacketData) {
                    handler.method.invoke(instance, packet, packetData);
                    return;
                }
                handler.method.invoke(instance, packet);
            } catch (Exception e) {
                Logger.error(
                    "Error calling packet handler for " + packet.getClass().getSimpleName() + " on subject " + subject,
                    e);
            }
        }
    }

    /**
     * Get all unique subjects that have handlers registered
     */
    public Set<String> getRegisteredSubjects() {
        return handlers.keySet();
    }

    /**
     * Get the packet class for a given subject (assumes one packet type per subject)
     */
    public Class<? extends Packet<?>> getPacketClassForSubject(String subject) {
        Map<Class<? extends Packet<?>>, List<HandlerMethod>> subjectHandlers = handlers.get(subject);
        if (subjectHandlers == null || subjectHandlers.isEmpty()) {
            return null;
        }
        return subjectHandlers.keySet().iterator().next();
    }

    private record HandlerMethod(Class<?> handlerClass, Method method, boolean hasPacketData) {

    }
}