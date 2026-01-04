package net.cytonic.cytosis.data.packet.packets;

import java.util.function.BiConsumer;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.messaging.NatsManager;

public abstract class Packet<P extends Packet<P>> {

    public static <P extends Packet<P>> Serializer<P> getSerializer(Class<P> clazz) {
        try {
            P instance = clazz.getDeclaredConstructor().newInstance();
            return instance.getSerializer();
        } catch (Exception e) {
            Logger.warn("OH NO %s",clazz.getSimpleName());
            return new DefaultGsonSerializer<>(clazz);
        }
    }

    protected abstract Serializer<P> getSerializer();

    public String getSubject() {
        return getClass().getSimpleName();
    }

    @SuppressWarnings("unchecked")
    public byte[] getData() {
        Serializer<Packet<P>> serializer = (Serializer<Packet<P>>) getSerializer();
        Logger.debug("Sending packet %s %s %s", getSubject(), getClass().getSimpleName(),
            serializer.serialize(getSubject(), this));
        return serializer.serialize(getSubject(), this).getBytes();
    }

    public void publish() {
        Cytosis.get(NatsManager.class).publish(getSubject(), getData());
    }

    public <R extends Packet<R>> void publishResponse(Class<R> responseType, BiConsumer<R, Throwable> consumer) {
        Cytosis.get(NatsManager.class).request(getSubject(), getData(), (message, throwable) -> {
            try {
                Serializer<R> responseSerializer = getSerializer(responseType);
                if (message == null) {
                    Logger.error("message is null for subject " + getSubject());
                    return;
                }
                Logger.debug("Received packet %s %s %s", getSubject(), responseType.getSimpleName(),
                    new String(message.getData()));
                R response = responseSerializer.deserialize(getSubject(), new String(message.getData()));
                consumer.accept(response, throwable);
            } catch (Exception e) {
                Logger.error("Failed to deserialize response from '" + getSubject() + "'!", e);
            }
        });
    }
}