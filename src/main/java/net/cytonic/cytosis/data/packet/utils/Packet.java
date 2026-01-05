package net.cytonic.cytosis.data.packet.utils;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.messaging.NatsManager;

public abstract class Packet<P extends Packet<P>> {

    public static final NatsManager NATS_MANAGER = Cytosis.get(NatsManager.class);

    public static <P extends Packet<P>> Serializer<P> getSerializer(Class<P> clazz) {
        try {
            P instance = clazz.getDeclaredConstructor().newInstance();
            return instance.getSerializer();
        } catch (Exception e) {
            Logger.warn("Failed to get serializer for packet type: %s. Resorting to default json serializer.",
                clazz.getSimpleName());
            return new DefaultGsonSerializer<>(clazz);
        }
    }

    protected abstract Serializer<P> getSerializer();

    public String getSubject() {
        return getClass().getSimpleName();
    }

    @SuppressWarnings("unchecked")
    public byte[] getData() {
        return ((Serializer<Packet<P>>) getSerializer()).serialize(this).getBytes();
    }

    public void publish() {
        publish(getSubject());
    }

    public void publish(String subject) {
        NATS_MANAGER.publish(subject, getData());
    }
}