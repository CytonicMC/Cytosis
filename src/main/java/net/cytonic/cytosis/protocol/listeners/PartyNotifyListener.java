package net.cytonic.cytosis.protocol.listeners;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.bootstrap.annotations.CytosisComponent;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.messaging.Subjects;
import net.cytonic.cytosis.parties.PartyManager;
import net.cytonic.protocol.NotifyData;
import net.cytonic.protocol.impl.notifyPackets.parties.PartyCreateNotifyPacket;
import net.cytonic.protocol.impl.notifyPackets.parties.PartyInviteExpireNotifyPacket;
import net.cytonic.protocol.impl.notifyPackets.parties.PartyInviteNotifyPacket;
import net.cytonic.protocol.impl.objects.parties.PartyOnePlayerProtocolObject;
import net.cytonic.protocol.impl.objects.parties.PartyStateProtocolObject;
import net.cytonic.protocol.impl.objects.parties.PartyTwoPlayerProtocolObject;
import net.cytonic.protocol.utils.NotifyHandler;

@CytosisComponent(dependsOn = PartyManager.class)
public class PartyNotifyListener {

    private final PartyManager pm = Cytosis.get(PartyManager.class);

    @NotifyHandler(subject = Subjects.PARTY_JOIN_NOTIFY)
    private void handlePartyJoin(PartyOnePlayerProtocolObject.Packet packet) {
        pm.trackPlayerJoin(packet.party(), packet.player());
    }

    @NotifyHandler
    private void handlePartyCreate(PartyCreateNotifyPacket.Packet packet) {
        pm.trackParty(packet.party());
    }

    @NotifyHandler(subject = Subjects.PARTY_LEAVE_NOTIFY)
    private void handlePartyLeave(PartyOnePlayerProtocolObject.Packet packet, NotifyData notifyData) {
        String subject = notifyData.subject().split("\\.")[3];
        if (subject.equals("request")) return;

        switch (subject) {
            case "command" -> pm.trackPlayerLeave(packet.party(), packet.player());
            case "disconnected" -> pm.trackPlayerLeaveDisconnect(packet.party(), packet.player());
            default -> Logger.warn("Invalid leave subject '%s'", subject);
        }
    }

    @NotifyHandler(subject = Subjects.PARTY_PROMOTE_NOTIFY)
    private void handlePartyPromote(PartyTwoPlayerProtocolObject.Packet packet, NotifyData notifyData) {
        String subject = notifyData.subject().split("\\.")[3];
        if (subject.equals("request")) return;

        switch (subject) {
            case "moderator" -> pm.trackPromotionToModerator(packet.party(), packet.sender(), packet.player());
            case "leader" -> pm.trackPromotionToLeader(packet.party(), packet.sender(), packet.player());
            default -> Logger.warn("Received invalid promote subject: %s %s", subject, notifyData.subject());
        }
    }

    @NotifyHandler(subject = Subjects.PARTY_KICK_NOTIFY)
    private void handlePartyKick(PartyTwoPlayerProtocolObject.Packet packet) {
        pm.trackKick(packet.party(), packet.sender(), packet.player());
    }

    @NotifyHandler(subject = Subjects.PARTY_TRANSFER_NOTIFY)
    private void handlePartyTransfer(PartyTwoPlayerProtocolObject.Packet packet, NotifyData notifyData) {
        String subject = notifyData.subject().split("\\.")[3];
        if (subject.equals("request")) return;

        switch (subject) {
            case "command" -> pm.trackTransferCommand(packet.party(), packet.sender(), packet.player());
            case "left" -> pm.trackTransferLeft(packet.party(), packet.sender(), packet.player());
            case "disconnected" -> pm.trackTransferDisconnected(packet.party(), packet.sender(), packet.player());
            default -> Logger.warn("Received invalid transfer subject: %s", subject);
        }
    }

    @NotifyHandler(subject = Subjects.PARTY_STATE_NOTIFY)
    private void handlePartyStateChange(PartyStateProtocolObject.Packet packet, NotifyData notifyData) {
        String subject = notifyData.subject().split("\\.")[2];

        switch (subject) {
            case "mute" -> pm.trackPartyMuteChange(packet.party(), packet.player(), packet.state());
            case "open" -> pm.trackPartyOpenChange(packet.party(), packet.player(), packet.state());
            case "open_invites" -> pm.trackPartyOpenInviteChange(packet.party(), packet.player(), packet.state());
            default -> Logger.warn("Received invalid state subject: %s", subject);
        }
    }

    @NotifyHandler(subject = Subjects.PARTY_YOINK_NOTIFY)
    private void handlePartyYoink(PartyOnePlayerProtocolObject.Packet packet) {
        pm.trackYoink(packet.party(), packet.player());
    }

    @NotifyHandler(subject = Subjects.PARTY_DISBAND_NOTIFY)
    private void handlePartyDisband(PartyOnePlayerProtocolObject.Packet packet, NotifyData notifyData) {
        String subject = notifyData.subject().split("\\.")[3];

        switch (subject) {
            case "empty" -> pm.trackEmptyPartyDisband(packet.party());
            case "command" -> pm.trackPartyDisband(packet.party(), packet.player());
            default -> Logger.warn("Received invalid disband subject: %s", subject);
        }
    }

    @NotifyHandler(subject = Subjects.PARTY_STATUS_NOTIFY)
    private void handlePartyStatusChange(PartyOnePlayerProtocolObject.Packet packet, NotifyData notifyData) {
        String subject = notifyData.subject().split("\\.")[2];

        switch (subject) {
            case "disconnect" -> pm.notifyPlayerDisconnect(packet.player(), packet.player());
            case "reconnect" -> pm.notifyPlayerReconnect(packet.player(), packet.player());
            default -> Logger.warn("Invalid status subject '%s'", subject);
        }
    }

    @NotifyHandler
    private void handlePartyInvite(PartyInviteNotifyPacket.Packet packet) {
        pm.trackInviteSent(packet.invite(), false);
    }

    @NotifyHandler
    private void handlePartyInviteExpire(PartyInviteExpireNotifyPacket.Packet packet) {
        pm.trackInviteExpired(packet.recipient(), packet.party(), packet.sender(), packet.recipient());
    }
}
