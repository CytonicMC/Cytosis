package net.cytonic.protocol.responses;

import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public record PartyResponse(boolean success, String message) {

}
