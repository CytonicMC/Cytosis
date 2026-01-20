package net.cytonic.protocol.responses;

import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public record SendPlayerResponse(boolean success, String message) {

}
