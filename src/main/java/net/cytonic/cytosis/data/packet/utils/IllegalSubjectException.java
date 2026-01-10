package net.cytonic.cytosis.data.packet.utils;

public class IllegalSubjectException extends RuntimeException {

    public IllegalSubjectException(String message) {
        super(message);
    }

    public IllegalSubjectException() {
        super("");
    }
}
