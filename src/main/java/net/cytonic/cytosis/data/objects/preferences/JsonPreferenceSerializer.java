package net.cytonic.cytosis.data.objects.preferences;

@FunctionalInterface
public interface JsonPreferenceSerializer<T> {

    JsonPreferenceSerializer<?> DEFAULT = Object::toString;

    String serialize(T object);
}
