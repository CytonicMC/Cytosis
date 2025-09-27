package net.cytonic.cytosis;

/**
 * Interface representing a bootstrappable component that can be initialized.
 */
public interface Bootstrappable {

    void init();

    default void shutdown() {
    }
}