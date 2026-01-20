package net.cytonic.cytosis.parties;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.reflect.TypeToken;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import net.cytonic.cytosis.Bootstrappable;
import net.cytonic.cytosis.CytonicNetwork;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.bootstrap.annotations.CytosisComponent;
import net.cytonic.cytosis.data.enums.PlayerRank;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.messaging.NatsManager;
import net.cytonic.cytosis.messaging.Subjects;
import net.cytonic.cytosis.protocol.publishers.PartyPacketsPublisher;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.protocol.data.objects.Party;
import net.cytonic.protocol.impl.responses.PartyResponse;

@CytosisComponent(dependsOn = {NatsManager.class, CytonicNetwork.class})
public class PartyManager implements Bootstrappable {

    public static final String LINE = "<#83cae4><st>                                                                               </st></#83cae4>";
    private static final Type PARTY_LIST = new TypeToken<List<Party>>() {
    }.getType();
    @Getter
    private final Map<UUID, Party> parties = new ConcurrentHashMap<>();
    private final CytonicNetwork cn = Cytosis.get(CytonicNetwork.class);

    public void trackParty(Party party) {
        parties.put(party.getId(), party);

        // track invites included too!
        party.getActiveInvites().forEach((_, invite) -> trackInviteSent(invite, true));
    }

    public void trackPlayerJoin(UUID partyID, UUID player) {
        if (!parties.containsKey(partyID)) {
            Logger.warn("STATE MISMATCH-- %s joined non-existent party '%s'", player.toString(), partyID.toString());
            return;
        }
        Party party = parties.get(partyID);

        for (UUID member : party.getAllPlayers()) {
            Cytosis.getPlayer(member).ifPresent(
                p -> p.sendMessage(Msg.mm("%s\n%s <white>joined the party!\n%s", LINE, cn.getMiniName(player), LINE)));
        }
        party.getMembers().add(player);
        Cytosis.getPlayer(player)
            .ifPresent(p -> p.sendMessage(Msg.mm("%s\nYou joined the party!\n%s", LINE, LINE)));
    }

    public void trackPlayerLeave(UUID partyID, UUID player) {
        if (!parties.containsKey(partyID)) {
            Logger.warn("STATE MISMATCH-- %s left non-existent party '%s'", player.toString(), partyID.toString());
            return;
        }
        Party party = parties.get(partyID);

        party.getMembers().remove(player);
        for (UUID member : party.getAllPlayers()) {
            Cytosis.getPlayer(member).ifPresent(
                p -> p.sendMessage(Msg.mm("%s\n%s <white>left the party.\n%s", LINE, cn.getMiniName(player), LINE)));
        }
        Cytosis.getPlayer(player)
            .ifPresent(p -> p.sendMessage(Msg.mm("%s\nYou left the party.\n%s", LINE, LINE)));
    }

    public void trackPlayerLeaveDisconnect(UUID partyID, UUID player) {
        if (!parties.containsKey(partyID)) {
            Logger.warn("STATE MISMATCH-- %s disconnected from a non-existent party '%s'", player.toString(),
                partyID.toString());
            return;
        }
        Party party = parties.get(partyID);

        party.getMembers().remove(player);
        for (UUID member : party.getAllPlayers()) {
            Cytosis.getPlayer(member).ifPresent(
                p -> p.sendMessage(
                    Msg.mm("%s\n%s <white>was removed from the party because they disconnected.\n%s", LINE,
                        cn.getMiniName(player),
                        LINE)));
        }
        Cytosis.getPlayer(player)
            .ifPresent(p -> p.sendMessage(
                Msg.mm("%s\nYou were removed from the party because you disconnected.\n%s", LINE, LINE)));
    }

    public void notifyPlayerDisconnect(UUID partyID, UUID player) {
        if (!parties.containsKey(partyID)) {
            Logger.warn("STATE MISMATCH-- %s disconnected from a non-existent party '%s'", player.toString(),
                partyID.toString());
            return;
        }
        Party party = parties.get(partyID);

        for (UUID member : party.getAllPlayers()) {
            Cytosis.getPlayer(member).ifPresent(p -> p.sendMessage(
                Msg.mm(
                    "%s\n%s <white>disconnected. They have 5 minutes to reconnect before they are removed from the party.\n%s",
                    LINE, cn.getMiniName(player), LINE)));
        }
        Cytosis.getPlayer(player)
            .ifPresent(
                p -> p.sendMessage(Msg.mm("%s\nYou disconnected and have 5 minutes to reconnect.\n%s", LINE, LINE)));
    }

    public void notifyPlayerReconnect(UUID partyID, UUID player) {
        if (!parties.containsKey(partyID)) {
            Logger.warn("STATE MISMATCH-- %s reconnected to a non-existent party '%s'", player.toString(),
                partyID.toString());
            return;
        }
        Party party = parties.get(partyID);

        for (UUID member : party.getAllPlayers()) {
            if (member.equals(player)) continue;
            Cytosis.getPlayer(member).ifPresent(p -> p.sendMessage(
                Msg.mm("%s\n%s <white>reconnected. \n%s", LINE, cn.getMiniName(player), LINE)));
        }
    }

    public void trackPromotionToModerator(UUID partyId, UUID sender, UUID recipient) {
        if (!parties.containsKey(partyId)) {
            Logger.warn("STATE MISMATCH-- %s was promoted in a non-existent party '%s'", recipient.toString(),
                partyId.toString());
            return;
        }
        Party party = parties.get(partyId);
        party.getMembers().remove(recipient);
        party.getModerators().add(recipient);
        for (UUID p : party.getAllPlayers()) {
            if (p.equals(sender)) {
                Cytosis.getPlayer(p).ifPresent(cp ->
                    cp.sendMessage(Msg.mm("%s\n<white>You promoted %s<white> to party moderator!\n%s", LINE,
                        cn.getMiniName(recipient), LINE)));
                continue;
            }
            if (p.equals(recipient)) {
                Cytosis.getPlayer(p).ifPresent(cp ->
                    cp.sendMessage(Msg.mm("%s\n%s <white>promoted you to party moderator!\n%s", LINE,
                        cn.getMiniName(sender), LINE)));
                continue;
            }
            Cytosis.getPlayer(p).ifPresent(cp ->
                cp.sendMessage(Msg.mm("%s\n%s <white>promoted %s<white> to party moderator!\n%s", LINE,
                    cn.getMiniName(sender), cn.getMiniName(recipient), LINE)));
        }
    }

    public void trackPromotionToLeader(UUID partyId, UUID sender, UUID recipient) {
        if (!parties.containsKey(partyId)) {
            Logger.warn("STATE MISMATCH-- %s was promoted in a non-existent party '%s'", recipient.toString(),
                partyId.toString());
            return;
        }
        Party party = parties.get(partyId);
        party.setLeader(recipient);
        party.getModerators().remove(recipient);
        party.getModerators().add(sender);
        for (UUID p : party.getAllPlayers()) {
            if (p.equals(sender)) {
                Cytosis.getPlayer(p).ifPresent(cp ->
                    cp.sendMessage(
                        Msg.mm("%s\n<white>You promoted %s <white>to party leader! You are now a party moderator.\n%s",
                            LINE, cn.getMiniName(recipient), LINE)));
                continue;
            }
            if (p.equals(recipient)) {
                Cytosis.getPlayer(p).ifPresent(cp ->
                    cp.sendMessage(Msg.mm(
                        "%s\n%s <white>promoted you to party leader! They are now a party moderator.\n%s", LINE,
                        cn.getMiniName(sender), LINE)));
                continue;
            }
            Cytosis.getPlayer(p).ifPresent(cp ->
                cp.sendMessage(
                    Msg.mm("%s\n%s <white>promoted %s <white>to party leader and is now a party moderator.\n%s", LINE,
                        cn.getMiniName(sender), cn.getMiniName(recipient), LINE)));
        }
    }

    public void trackKick(UUID partyId, UUID sender, UUID recipient) {
        if (!parties.containsKey(partyId)) {
            Logger.warn("STATE MISMATCH-- %s was kicked from a non-existent party '%s'", recipient.toString(),
                partyId.toString());
            return;
        }
        Party party = parties.get(partyId);
        party.getModerators().remove(recipient);
        party.getMembers().remove(recipient);

        Cytosis.getPlayer(recipient).ifPresent(cp -> cp.sendMessage(Msg.mm(
            "%s\n%s <white>kicked you from the party.\n%s", LINE, cn.getMiniName(sender), LINE)));

        for (UUID p : party.getAllPlayers()) {
            if (p.equals(sender)) {
                Cytosis.getPlayer(p).ifPresent(cp ->
                    cp.sendMessage(
                        Msg.mm("%s\n<white>You kicked %s <white>from the party.\n%s",
                            LINE, cn.getMiniName(recipient), LINE)));
                continue;
            }
            Cytosis.getPlayer(p).ifPresent(cp ->
                cp.sendMessage(Msg.mm("%s\n%s <white>kicked %s <white>from the party.\n%s", LINE,
                    cn.getMiniName(sender), cn.getMiniName(recipient), LINE)));
        }
    }

    public void trackTransferCommand(UUID partyId, UUID sender, UUID recipient) {
        if (!parties.containsKey(partyId)) {
            Logger.warn("STATE MISMATCH-- %s was transferred leadership to a non-existent party '%s'",
                recipient.toString(),
                partyId.toString());
            return;
        }
        Party party = parties.get(partyId);
        party.setLeader(recipient);
        party.getModerators().remove(recipient);
        party.getMembers().remove(recipient);
        party.getModerators().add(sender);

        for (UUID p : party.getAllPlayers()) {
            if (p.equals(sender)) {
                Cytosis.getPlayer(p).ifPresent(cp ->
                    cp.sendMessage(
                        Msg.mm("%s\n<white>You transferred the party to %s<white>.\n%s",
                            LINE, cn.getMiniName(recipient), LINE)));
                continue;
            }
            if (p.equals(recipient)) {
                Cytosis.getPlayer(p).ifPresent(cp ->
                    cp.sendMessage(Msg.mm(
                        "%s\n%s <white>transferred the party to you.\n%s", LINE,
                        cn.getMiniName(sender), LINE)));
                continue;
            }
            Cytosis.getPlayer(p).ifPresent(cp ->
                cp.sendMessage(Msg.mm("%s\n%s <white>transferred the party to %s<white>.\n%s", LINE,
                    cn.getMiniName(sender), cn.getMiniName(recipient), LINE)));
        }
    }

    public void trackTransferLeft(UUID partyId, UUID sender, UUID recipient) {
        if (!parties.containsKey(partyId)) {
            Logger.warn("STATE MISMATCH-- %s was transferred leadership a to non-existent party '%s'",
                recipient.toString(), partyId.toString());
            return;
        }

        Party party = parties.get(partyId);
        party.setLeader(recipient);
        party.getModerators().remove(recipient);
        party.getMembers().remove(recipient);

        Cytosis.getPlayer(sender).ifPresent(cp -> cp.sendMessage(
            Msg.mm("%s\n<white>The party was transferred to %s <white>because you left.\n%s",
                LINE, cn.getMiniName(recipient), LINE)));

        for (UUID p : party.getAllPlayers()) {
            if (p.equals(recipient)) {
                Cytosis.getPlayer(p).ifPresent(cp -> cp.sendMessage(
                    Msg.mm("%s\n<white>The party was transferred to you because %s<white> left.\n%s",
                        LINE, cn.getMiniName(sender), LINE)));
                continue;
            }
            Cytosis.getPlayer(p).ifPresent(cp -> cp.sendMessage(Msg.mm(
                "%s\n<white>The party was transferred to %s<white> because the previous leader, %s<white>, left.\n%s",
                LINE, cn.getMiniName(recipient), cn.getMiniName(sender), LINE)));
        }
    }

    public void trackTransferDisconnected(UUID partyId, UUID sender, UUID recipient) {
        if (!parties.containsKey(partyId)) {
            Logger.warn("STATE MISMATCH-- %s was transferred leadership to a non-existent party '%s'",
                recipient.toString(),
                partyId.toString());
            return;
        }
        Party party = parties.get(partyId);
        party.setLeader(recipient);
        party.getModerators().remove(recipient);
        party.getModerators().add(sender);

        for (UUID p : party.getAllPlayers()) {
            if (p.equals(sender)) {
                // not possible
                Cytosis.getPlayer(p).ifPresent(cp ->
                    cp.sendMessage(
                        Msg.mm("%s\n<white>The party was transferred to %s <white>because you disconnected.\n%s",
                            LINE, cn.getMiniName(recipient), LINE)));
                continue;
            }
            if (p.equals(recipient)) {
                Cytosis.getPlayer(p).ifPresent(cp ->
                    cp.sendMessage(Msg.mm(
                        "%s\n%s <white>The party was transferred to you because %s<white> disconnected.\n%s", LINE,
                        cn.getMiniName(sender), LINE)));
                continue;
            }
            Cytosis.getPlayer(p).ifPresent(cp -> cp.sendMessage(Msg.mm(
                "%s\n<white>The party was transferred to %s<white> because the previous leader, %s<white>, disconnected.\n%s",
                LINE, cn.getMiniName(recipient), cn.getMiniName(sender), LINE)));
        }
    }

    public void trackPartyMuteChange(UUID partyId, UUID sender, boolean state) {
        if (!parties.containsKey(partyId)) {
            Logger.warn("STATE MISMATCH-- %s updated the state of a non-existent party '%s'",
                sender.toString(),
                partyId.toString());
            return;
        }
        Party party = parties.get(partyId);
        party.setMuted(state);
        String verb = state ? "muted" : "unmuted";

        for (UUID p : party.getAllPlayers()) {
            if (p.equals(sender)) {
                Cytosis.getPlayer(p).ifPresent(cp ->
                    cp.sendMessage(
                        Msg.mm("%s\n<white>You %s the party.\n%s",
                            LINE, verb, LINE)));
                continue;
            }
            Cytosis.getPlayer(p).ifPresent(cp -> cp.sendMessage(Msg.mm(
                "%s\n%s <white>%s the party. \n%s", LINE, cn.getMiniName(sender), verb, LINE)));
        }
    }

    public void trackPartyOpenChange(UUID partyId, UUID sender, boolean state) {
        if (!parties.containsKey(partyId)) {
            Logger.warn("STATE MISMATCH-- %s updated the state of a non-existent party '%s'",
                sender.toString(),
                partyId.toString());
            return;
        }
        Party party = parties.get(partyId);
        party.setMuted(state);
        String verb = state ? "opened the party. Anyone can now join with /party join." : "closed the party.";

        for (UUID p : party.getAllPlayers()) {
            if (p.equals(sender)) {
                Cytosis.getPlayer(p).ifPresent(cp ->
                    cp.sendMessage(Msg.mm("%s\n<white>You %s\n%s", LINE, verb, LINE)));
                continue;
            }
            Cytosis.getPlayer(p).ifPresent(cp -> cp.sendMessage(Msg.mm("%s\n%s <white>%s \n%s",
                LINE, cn.getMiniName(sender), verb, LINE)));
        }
    }

    public void trackPartyOpenInviteChange(UUID partyId, UUID sender, boolean state) {
        if (!parties.containsKey(partyId)) {
            Logger.warn("STATE MISMATCH-- %s updated the state of a non-existent party '%s'",
                sender.toString(),
                partyId.toString());
            return;
        }
        Party party = parties.get(partyId);
        party.setMuted(state);
        String verb =
            state ? "opened the party's invites. Anyone can now send invites." : "closed the party's invites.";

        for (UUID p : party.getAllPlayers()) {
            if (p.equals(sender)) {
                Cytosis.getPlayer(p).ifPresent(cp ->
                    cp.sendMessage(Msg.mm("%s\n<white>You %s\n%s", LINE, verb, LINE)));
                continue;
            }
            Cytosis.getPlayer(p).ifPresent(cp -> cp.sendMessage(Msg.mm(
                "%s\n%s <white>%s\n%s", LINE, cn.getMiniName(sender), verb, LINE)));
        }
    }

    public void trackYoink(UUID partyId, UUID sender) {
        if (!parties.containsKey(partyId)) {
            Logger.warn("STATE MISMATCH-- %s updated the state of a non-existent party '%s'",
                sender.toString(),
                partyId.toString());
            return;
        }
        if (!cn.getCachedPlayerRanks().get(sender).isHigherOrEqualTo(PlayerRank.ADMIN)) {
            Logger.warn("Non admin player %s attempted to yoink a party", sender.toString());
        }

        Party party = parties.get(partyId);
        party.getModerators().add(party.getLeader());
        party.setLeader(sender);
        party.getModerators().remove(sender);
        party.getMembers().remove(sender);

        for (UUID p : party.getAllPlayers()) {
            if (p.equals(sender)) {
                Cytosis.getPlayer(p).ifPresent(cp ->
                    cp.sendMessage(
                        Msg.mm("%s\n<#65c6ea><b>Yoink!</b> <white>You assumed leadership of the party.\n%s", LINE,
                            LINE)));
                continue;
            }
            Cytosis.getPlayer(p).ifPresent(cp -> cp.sendMessage(Msg.mm(
                "%s\n<#65c6ea><b>Yoinked!</b> %s <white>assumed leadership of the party.\n%s",
                LINE, cn.getMiniName(sender), LINE)));
        }

    }

    public void trackEmptyPartyDisband(UUID partyId) {
        if (!parties.containsKey(partyId)) {
            Logger.warn("STATE MISMATCH-- empty disbanded a non-existent party '%s'", partyId.toString());
            return;
        }

        Party party = parties.get(partyId);
        parties.remove(partyId);

        for (UUID player : party.getAllPlayers()) {
            Cytosis.getPlayer(player).ifPresent(p -> p.sendMessage(
                Msg.mm("%s\n<red>The party was disbanded as it was empty and all invites had expired.\n%s",
                    LINE, LINE)));
        }
    }

    public void trackPartyDisband(UUID partyId, UUID playerId) {
        if (!parties.containsKey(partyId)) {
            Logger.warn("STATE MISMATCH-- %s disbanded a non-existent party '%s'", playerId.toString(),
                partyId.toString());
            return;
        }

        Party party = parties.get(partyId);
        parties.remove(partyId);

        for (UUID player : party.getAllPlayers()) {
            if (player.equals(playerId)) {
                Cytosis.getPlayer(player).ifPresent(p -> p.sendMessage(
                    Msg.mm("%s\n<white>You disbanded the party.\n%s", LINE, LINE)));
                continue;
            }
            Cytosis.getPlayer(player).ifPresent(p -> p.sendMessage(
                Msg.mm("%s\n%s <white>disbanded the party.\n%s", LINE, cn.getMiniName(playerId), LINE)));
        }
    }

    public void trackInviteSent(net.cytonic.protocol.data.objects.PartyInvite invite, boolean skipTrack) {
        if (!skipTrack && !parties.containsKey(invite.partyId())) {
            Logger.warn("STATE MISMATCH-- a player was invited to a non-existent party '%s'",
                invite.partyId().toString());
            return;
        }

        Party party = parties.get(invite.partyId());
        if (!skipTrack) {
            party.getActiveInvites().put(invite.id(), invite);
        }

        Cytosis.getPlayer(invite.recipient()).ifPresent(p -> p.sendMessage(Msg.mm("""
                %s
                %s<white> invited you to join their party. You have 60 seconds to accept.\
                 <green><b><click:run_command:/party accept %s>[ACCEPT]</b>\s
                %s""", LINE, cn.getMiniName(invite.sender()), invite.sender().toString(), LINE)
            )
        );

        for (UUID uuid : party.getAllPlayers()) {
            if (uuid.equals(invite.sender())) {
                Cytosis.getPlayer(uuid).ifPresent(p -> p.sendMessage(
                    Msg.mm("%s\n<white>You invited %s <white>to %s<white> party. They have 60 seconds to accept.\n%s",
                        LINE, cn.getMiniName(invite.recipient()), skipTrack ? "a" : "the", LINE)));
                continue;
            }

            Cytosis.getPlayer(uuid).ifPresent(p -> p.sendMessage(Msg.mm("""
                    %s
                    %s<white> invited %s<white> to the party. They have 60 seconds to accept.
                    %s""", LINE, cn.getMiniName(invite.sender()), cn.getMiniName(invite.recipient()), LINE)
                )
            );
        }
    }

    public void trackInviteExpired(UUID invite, UUID partyId, UUID sender, UUID recipient) {
        if (!parties.containsKey(partyId)) {
            Logger.warn("STATE MISMATCH-- a player's invite to a non-existent party expired '%s'",
                partyId.toString());
            return;
        }

        Party party = parties.get(partyId);
        party.getActiveInvites().remove(invite);

        Cytosis.getPlayer(recipient).ifPresent(p -> p.sendMessage(Msg.mm("""
                %s
                %s<white>'s invitation to join their party has expired.
                %s""", LINE, cn.getMiniName(sender), LINE)
            )
        );

        for (UUID uuid : party.getAllPlayers()) {
            if (uuid.equals(sender)) {
                Cytosis.getPlayer(uuid).ifPresent(p -> p.sendMessage(
                    Msg.mm("%s\n<white>Your party invitation to %s<white> has expired.\n%s",
                        LINE, cn.getMiniName(recipient), LINE)));
                continue;
            }

            Cytosis.getPlayer(uuid).ifPresent(p -> p.sendMessage(Msg.mm("""
                    %s
                    %s<white>'s invitation for %s<white> to join the party has expired.
                    %s""", LINE, cn.getMiniName(sender), cn.getMiniName(recipient), LINE)
                )
            );
        }
    }

    public CompletableFuture<PartyResponse> joinParty(UUID player, UUID sender) {
        CompletableFuture<PartyResponse> future = new CompletableFuture<>();

        Party party = getPlayerParty(player);
        if (party == null) {
            return CompletableFuture.completedFuture(new PartyResponse(false, "TARGET_NOT_IN_PARTY"));
        }

        if (Cytosis.getPlayer(sender).isEmpty()) {
            return CompletableFuture.completedFuture(new PartyResponse(false, "SENDER_NOT_FOUND"));
        }
        String subj = Cytosis.getPlayer(sender).get().isAdmin() ? Subjects.PARTY_JOIN_REQUEST_BYPASS
            : Subjects.PARTY_JOIN_REQUEST_COMMAND;

        return Cytosis.get(PartyPacketsPublisher.class).sendOnePlayer(sender, subj, future, party);
    }

    public CompletableFuture<PartyResponse> kickPlayer(UUID sender, UUID player) {
        CompletableFuture<PartyResponse> future = new CompletableFuture<>();

        Party party = getPlayerParty(sender);
        if (party == null) {
            return CompletableFuture.completedFuture(new PartyResponse(false, "NOT_IN_PARTY"));
        }

        return Cytosis.get(PartyPacketsPublisher.class).sendTwoPlayer(sender, player, future, party,
            Subjects.PARTY_KICK_REQUEST);
    }

    public CompletableFuture<PartyResponse> transferPlayer(UUID sender, UUID player) {
        CompletableFuture<PartyResponse> future = new CompletableFuture<>();

        Party party = getPlayerParty(sender);
        if (party == null) {
            return CompletableFuture.completedFuture(new PartyResponse(false, "NOT_IN_PARTY"));
        }

        return Cytosis.get(PartyPacketsPublisher.class)
            .sendTwoPlayer(sender, player, future, party, Subjects.PARTY_TRANSFER_REQUEST);
    }

    public CompletableFuture<PartyResponse> promotePlayer(UUID sender, UUID player) {
        CompletableFuture<PartyResponse> future = new CompletableFuture<>();

        Party party = getPlayerParty(sender);
        if (party == null) {
            return CompletableFuture.completedFuture(new PartyResponse(false, "NOT_IN_PARTY"));
        }

        return Cytosis.get(PartyPacketsPublisher.class).sendTwoPlayer(sender, player, future, party,
            Subjects.PARTY_PROMOTE_REQUEST);
    }

    public CompletableFuture<PartyResponse> disbandParty(UUID sender) {
        CompletableFuture<PartyResponse> future = new CompletableFuture<>();

        Party party = getPlayerParty(sender);
        if (party == null) {
            return CompletableFuture.completedFuture(new PartyResponse(false, "NOT_IN_PARTY"));
        }

        return Cytosis.get(PartyPacketsPublisher.class)
            .sendOnePlayer(sender, Subjects.PARTY_DISBAND_REQUEST, future, party);
    }

    public CompletableFuture<PartyResponse> muteParty(UUID sender, boolean state) {
        Party party = getPlayerParty(sender);
        if (party == null) {
            return CompletableFuture.completedFuture(new PartyResponse(false, "NOT_IN_PARTY"));
        }

        return Cytosis.get(PartyPacketsPublisher.class)
            .sendState(sender, party.getId(), state, Subjects.PARTY_STATE_MUTE_REQUEST);
    }

    public CompletableFuture<PartyResponse> openParty(UUID sender, boolean state) {
        Party party = getPlayerParty(sender);
        if (party == null) {
            return CompletableFuture.completedFuture(new PartyResponse(false, "NOT_IN_PARTY"));
        }

        return Cytosis.get(PartyPacketsPublisher.class)
            .sendState(sender, party.getId(), state, Subjects.PARTY_STATE_OPEN_REQUEST);
    }

    public CompletableFuture<PartyResponse> openPartyInvites(UUID sender, boolean state) {
        Party party = getPlayerParty(sender);
        if (party == null) {
            return CompletableFuture.completedFuture(new PartyResponse(false, "NOT_IN_PARTY"));
        }

        return Cytosis.get(PartyPacketsPublisher.class)
            .sendState(sender, party.getId(), state, Subjects.PARTY_STATE_OPEN_INVITES_REQUEST);
    }

    public CompletableFuture<PartyResponse> yoinkParty(UUID sender) {
        CompletableFuture<PartyResponse> future = new CompletableFuture<>();

        Party party = getPlayerParty(sender);
        if (party == null) {
            return CompletableFuture.completedFuture(new PartyResponse(false, "NOT_IN_PARTY"));
        }

        return Cytosis.get(PartyPacketsPublisher.class)
            .sendOnePlayer(sender, Subjects.PARTY_YOINK_REQUEST, future, party);
    }

    @Nullable
    public Party getPlayerParty(UUID player) {
        for (Party party : parties.values()) {
            if (party.getAllPlayers().contains(player))
                return party;
        }
        return null;
    }

    @Override
    public void init() {
        Cytosis.get(NatsManager.class)
            .request(Subjects.PREFIX + "party.fetch.request", new byte[0], (message, throwable) -> {
                if (throwable != null) {
                    Logger.error("Failed to fetch active party list: ", throwable);
                    return;
                }
                List<Party> partyList = Cytosis.GSON.fromJson(new String(message.getData()), PARTY_LIST);
                parties.putAll(partyList.stream().collect(Collectors.toMap(Party::getId, Function.identity())));
                Logger.info("Loaded active parties");
            });
    }
}
