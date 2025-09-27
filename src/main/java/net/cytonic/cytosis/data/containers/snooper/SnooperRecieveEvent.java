package net.cytonic.cytosis.data.containers.snooper;

@FunctionalInterface
public interface SnooperRecieveEvent {

    void onReceive(SnooperChannel channel, SnooperContainer container);
}
