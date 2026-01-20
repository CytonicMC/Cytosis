package net.cytonic.cytosis.protocol.publishers;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import lombok.NoArgsConstructor;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.bootstrap.annotations.CytosisComponent;
import net.cytonic.cytosis.messaging.Subjects;
import net.cytonic.cytosis.parties.PartyManager;
import net.cytonic.protocol.data.objects.Party;
import net.cytonic.protocol.data.objects.PartyInvite;
import net.cytonic.protocol.impl.objects.parties.PartyInviteAcceptProtocolObject;
import net.cytonic.protocol.impl.objects.parties.PartyInviteProtocolObject;
import net.cytonic.protocol.impl.objects.parties.PartyLeaveProtocolObject;
import net.cytonic.protocol.impl.objects.parties.PartyOnePlayerProtocolObject;
import net.cytonic.protocol.impl.objects.parties.PartyStateProtocolObject;
import net.cytonic.protocol.impl.objects.parties.PartyTwoPlayerProtocolObject;
import net.cytonic.protocol.impl.responses.PartyResponse;


@NoArgsConstructor
@CytosisComponent(dependsOn = PartyManager.class)
public class PartyPacketsPublisher {

    private final PartyManager pm = Cytosis.get(PartyManager.class);

    public CompletableFuture<PartyResponse> sendInvite(UUID sender, UUID recipient) {
        if (sender.equals(recipient)) {
            return CompletableFuture.completedFuture(new PartyResponse(false, "ERR_SEND_TO_SELF"));
        }
        CompletableFuture<PartyResponse> future = new CompletableFuture<>();
        Party party = pm.getPlayerParty(sender);
        UUID partyId = null;
        if (party != null) {
            partyId = party.getId();
        }

        new PartyInviteProtocolObject.Packet(partyId, sender, recipient).request(Subjects.PARTY_INVITE_SEND_REQUEST,
            (response, throwable) -> {
                if (throwable != null) {
                    future.completeExceptionally(throwable);
                    return;
                }
                future.complete(response);
            });

        return future;
    }

    public CompletableFuture<PartyResponse> acceptInvite(UUID player, UUID sender) {
        CompletableFuture<PartyResponse> future = new CompletableFuture<>();
        UUID reqID = null;

        for (Party p : pm.getParties().values()) {
            for (PartyInvite i : p.getActiveInvites().values()) {
                if (i.sender().equals(sender) && i.recipient().equals(player)) {
                    reqID = i.id();
                    break;
                }
            }
        }
        if (reqID == null) {
            CompletableFuture.completedFuture(new PartyResponse(false, "ERR_NOT_FOUND"));
        }
        new PartyInviteAcceptProtocolObject.Packet(reqID).request((response, throwable) -> {
            if (throwable != null) {
                future.completeExceptionally(throwable);
                return;
            }
            future.complete(response);
        });
        return future;
    }


    public CompletableFuture<PartyResponse> leaveParty(UUID player) {
        CompletableFuture<PartyResponse> future = new CompletableFuture<>();

        Party party = pm.getPlayerParty(player);
        if (party == null) {
            return CompletableFuture.completedFuture(new PartyResponse(false, "NOT_IN_PARTY"));
        }

        new PartyLeaveProtocolObject.Packet(player).request(Subjects.PARTY_LEAVE_REQUEST, (response, throwable) -> {
            if (throwable != null) {
                future.completeExceptionally(throwable);
                return;
            }
            future.complete(response);
        });
        return future;
    }

    public CompletableFuture<PartyResponse> sendOnePlayer(UUID sender, String subj,
        CompletableFuture<PartyResponse> future, Party party) {
        new PartyOnePlayerProtocolObject.Packet(party.getId(), sender).request(subj, (response, throwable) -> {
            if (throwable != null) {
                future.completeExceptionally(throwable);
                return;
            }
            future.complete(response);
        });
        return future;
    }

    public CompletableFuture<PartyResponse> sendTwoPlayer(UUID sender, UUID player,
        CompletableFuture<PartyResponse> future, Party party, String subj) {
        new PartyTwoPlayerProtocolObject.Packet(party.getId(), player, sender).request(subj, (response, throwable) -> {
            if (throwable != null) {
                future.completeExceptionally(throwable);
                return;
            }

            future.complete(response);
        });
        return future;
    }

    public CompletableFuture<PartyResponse> sendState(UUID sender, UUID party, boolean state,
        String subj) {
        CompletableFuture<PartyResponse> future = new CompletableFuture<>();
        new PartyStateProtocolObject.Packet(party, sender, state).request(subj, (response, throwable) -> {
            if (throwable != null) {
                future.completeExceptionally(throwable);
                return;
            }
            future.complete(response);
        });
        return future;
    }
}
