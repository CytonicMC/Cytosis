package net.cytonic.cytosis.data.objects.preferences;

@FunctionalInterface
public interface JsonPreferenceDeserializer<T> {
    T deserialize(String data);
}
