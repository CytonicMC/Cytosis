package net.cytonic.cytosis.data.packet.publishers;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import lombok.NoArgsConstructor;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.bootstrap.annotations.CytosisComponent;
import net.cytonic.cytosis.data.packet.packets.parties.PartyInviteAcceptPacket;
import net.cytonic.cytosis.data.packet.packets.parties.PartyInviteSendPacket;
import net.cytonic.cytosis.data.packet.packets.parties.PartyLeavePacket;
import net.cytonic.cytosis.data.packet.packets.parties.PartyOnePlayerPacket;
import net.cytonic.cytosis.data.packet.packets.parties.PartyResponsePacket;
import net.cytonic.cytosis.data.packet.packets.parties.PartyStatePacket;
import net.cytonic.cytosis.data.packet.packets.parties.PartyTwoPlayerPacket;
import net.cytonic.cytosis.parties.Party;
import net.cytonic.cytosis.parties.PartyInvite;
import net.cytonic.cytosis.parties.PartyManager;


@NoArgsConstructor
@CytosisComponent(dependsOn = PartyManager.class)
public class PartyPacketsPublisher {

    private final PartyManager pm = Cytosis.get(PartyManager.class);

    public CompletableFuture<PartyResponsePacket> sendInvite(UUID sender, UUID recipient) {
        if (sender.equals(recipient)) {
            return CompletableFuture.completedFuture(new PartyResponsePacket(false, "ERR_SEND_TO_SELF"));
        }
        CompletableFuture<PartyResponsePacket> future = new CompletableFuture<>();
        Party party = pm.getPlayerParty(sender);
        UUID partyId = null;
        if (party != null) {
            partyId = party.getId();
        }

        new PartyInviteSendPacket(partyId, sender, recipient).publishResponse(PartyResponsePacket.class,
            (response, throwable) -> {
                if (throwable != null) {
                    future.completeExceptionally(throwable);
                    return;
                }
                future.complete(response);
            });

        return future;
    }

    public CompletableFuture<PartyResponsePacket> acceptInvite(UUID player, UUID sender) {
        CompletableFuture<PartyResponsePacket> future = new CompletableFuture<>();
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
            CompletableFuture.completedFuture(new PartyResponsePacket(false, "ERR_NOT_FOUND"));
        }
        new PartyInviteAcceptPacket(reqID).publishResponse(PartyResponsePacket.class, (response, throwable) -> {
            if (throwable != null) {
                future.completeExceptionally(throwable);
                return;
            }
            future.complete(response);
        });
        return future;
    }


    public CompletableFuture<PartyResponsePacket> leaveParty(UUID player) {
        CompletableFuture<PartyResponsePacket> future = new CompletableFuture<>();

        Party party = pm.getPlayerParty(player);
        if (party == null) {
            return CompletableFuture.completedFuture(new PartyResponsePacket(false, "NOT_IN_PARTY"));
        }

        new PartyLeavePacket(player).publishResponse(PartyResponsePacket.class, (response, throwable) -> {
            if (throwable != null) {
                future.completeExceptionally(throwable);
                return;
            }
            future.complete(response);
        });
        return future;
    }

    public CompletableFuture<PartyResponsePacket> sendOnePlayer(UUID sender,
        CompletableFuture<PartyResponsePacket> future, Party party, PartyOnePlayerPacket.Type type) {
        new PartyOnePlayerPacket(party.getId(), sender, type).publishResponse(PartyResponsePacket.class,
            (response, throwable) -> {
                if (throwable != null) {
                    future.completeExceptionally(throwable);
                    return;
                }
                future.complete(response);
            });
        return future;
    }

    public CompletableFuture<PartyResponsePacket> sendTwoPlayer(UUID sender, UUID player,
        CompletableFuture<PartyResponsePacket> future, Party party, PartyTwoPlayerPacket.Type type) {
        new PartyTwoPlayerPacket(party.getId(), player, sender, type).publishResponse(PartyResponsePacket.class,
            (response, throwable) -> {
                if (throwable != null) {
                    future.completeExceptionally(throwable);
                    return;
                }

                future.complete(response);
            });
        return future;
    }

    public CompletableFuture<PartyResponsePacket> sendState(UUID sender, UUID party, boolean state,
        PartyStatePacket.Type type) {
        CompletableFuture<PartyResponsePacket> future = new CompletableFuture<>();
        new PartyStatePacket(sender, party, state, type).publishResponse(PartyResponsePacket.class,
            (response, throwable) -> {
                if (throwable != null) {
                    future.completeExceptionally(throwable);
                    return;
                }
                future.complete(response);
            });
        return future;
    }
}
