package net.cytonic.protocol.responses;

import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public record InstanceResponse(boolean success, String message) {

}
