package net.cytonic.cytosis.data.packet.utils;

public abstract class ReusablePacket<T extends Packet<T>> extends Packet<T> {

    @Override
    @Deprecated(forRemoval = true) // trickery to get IntelliJ to yell at you
    public String getSubject() {
        throw new UnsupportedOperationException("getSubject() cannot be called on a reusable packet.");
    }

    @Override
    @Deprecated(forRemoval = true) // trickery to get IntelliJ to yell at you
    public final void publish() {
        throw new UnsupportedOperationException("publish() cannot be called on a reusable packet.");
    }
}
