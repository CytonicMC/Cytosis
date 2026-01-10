package net.cytonic.cytosis.data.packet.listeners;

import lombok.NoArgsConstructor;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.bootstrap.annotations.CytosisComponent;
import net.cytonic.cytosis.data.packet.packets.parties.PartyCreatePacket;
import net.cytonic.cytosis.data.packet.packets.parties.PartyInviteExpirePacket;
import net.cytonic.cytosis.data.packet.packets.parties.PartyInvitePacket;
import net.cytonic.cytosis.data.packet.packets.parties.PartyOnePlayerPacket;
import net.cytonic.cytosis.data.packet.packets.parties.PartyStatePacket;
import net.cytonic.cytosis.data.packet.packets.parties.PartyTwoPlayerPacket;
import net.cytonic.cytosis.data.packet.utils.PacketData;
import net.cytonic.cytosis.data.packet.utils.PacketHandler;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.messaging.Subjects;
import net.cytonic.cytosis.parties.PartyManager;

@NoArgsConstructor
@CytosisComponent(dependsOn = PartyManager.class)
public class PartyPacketsListener {

    private final PartyManager pm = Cytosis.get(PartyManager.class);

    @PacketHandler(subject = Subjects.PARTY_JOIN_NOTIFY)
    private void handlePartyJoin(PartyOnePlayerPacket packet) {
        pm.trackPlayerJoin(packet.getParty(), packet.getPlayer());
    }

    @PacketHandler(subject = Subjects.PARTY_CREATE_NOTIFY)
    private void handlePartyCreate(PartyCreatePacket packet) {
        pm.trackParty(packet.getParty());
    }

    @PacketHandler(subject = Subjects.PARTY_LEAVE_NOTIFY)
    private void handlePartyLeave(PartyOnePlayerPacket packet, PacketData packetData) {
        String subject = packetData.subject().split("\\.")[3];
        if (subject.equals("request")) return;

        switch (subject) {
            case "command" -> pm.trackPlayerLeave(packet.getParty(), packet.getPlayer());
            case "disconnected" -> pm.trackPlayerLeaveDisconnect(packet.getParty(), packet.getPlayer());
            default -> Logger.warn("Invalid leave subject '%s'", subject);
        }
    }

    @PacketHandler(subject = Subjects.PARTY_PROMOTE_NOTIFY)
    private void handlePartyPromote(PartyTwoPlayerPacket packet, PacketData packetData) {
        String subject = packetData.subject().split("\\.")[3];
        if (subject.equals("request")) return;

        switch (subject) {
            case "moderator" -> pm.trackPromotionToModerator(packet.getParty(), packet.getSender(), packet.getPlayer());
            case "leader" -> pm.trackPromotionToLeader(packet.getParty(), packet.getSender(), packet.getPlayer());
            default -> Logger.warn("Received invalid promote subject: %s %s", subject, packetData.subject());
        }
    }

    @PacketHandler(subject = Subjects.PARTY_KICK_NOTIFY)
    private void handlePartyKick(PartyTwoPlayerPacket packet) {
        pm.trackKick(packet.getParty(), packet.getSender(), packet.getPlayer());
    }

    @PacketHandler(subject = Subjects.PARTY_TRANSFER_NOTIFY)
    private void handlePartyTransfer(PartyTwoPlayerPacket packet, PacketData packetData) {
        String subject = packetData.subject().split("\\.")[3];
        if (subject.equals("request")) return;

        switch (subject) {
            case "command" -> pm.trackTransferCommand(packet.getParty(), packet.getSender(), packet.getPlayer());
            case "left" -> pm.trackTransferLeft(packet.getParty(), packet.getSender(), packet.getPlayer());
            case "disconnected" ->
                pm.trackTransferDisconnected(packet.getParty(), packet.getSender(), packet.getPlayer());
            default -> Logger.warn("Received invalid transfer subject: %s", subject);
        }
    }

    @PacketHandler(subject = Subjects.PARTY_STATE_NOTIFY)
    private void handlePartyStateChange(PartyStatePacket packet, PacketData packetData) {
        String subject = packetData.subject().split("\\.")[2];

        switch (subject) {
            case "mute" -> pm.trackPartyMuteChange(packet.getParty(), packet.getPlayer(), packet.isState());
            case "open" -> pm.trackPartyOpenChange(packet.getParty(), packet.getPlayer(), packet.isState());
            case "open_invites" ->
                pm.trackPartyOpenInviteChange(packet.getParty(), packet.getPlayer(), packet.isState());
            default -> Logger.warn("Received invalid state subject: %s", subject);
        }
    }

    @PacketHandler(subject = Subjects.PARTY_YOINK_NOTIFY)
    private void handlePartyYoink(PartyOnePlayerPacket packet) {
        pm.trackYoink(packet.getParty(), packet.getPlayer());
    }

    @PacketHandler(subject = Subjects.PARTY_DISBAND_NOTIFY)
    private void handlePartyDisband(PartyOnePlayerPacket packet, PacketData packetData) {
        String subject = packetData.subject().split("\\.")[3];

        switch (subject) {
            case "empty" -> pm.trackEmptyPartyDisband(packet.getParty());
            case "command" -> pm.trackPartyDisband(packet.getParty(), packet.getPlayer());
            default -> Logger.warn("Received invalid disband subject: %s", subject);
        }
    }

    @PacketHandler(subject = Subjects.PARTY_STATUS_NOTIFY)
    private void handlePartyStatusChange(PartyOnePlayerPacket packet, PacketData packetData) {
        String subject = packetData.subject().split("\\.")[2];

        switch (subject) {
            case "disconnect" -> pm.notifyPlayerDisconnect(packet.getParty(), packet.getPlayer());
            case "reconnect" -> pm.notifyPlayerReconnect(packet.getParty(), packet.getPlayer());
            default -> Logger.warn("Invalid status subject '%s'", subject);
        }
    }

    @PacketHandler(subject = Subjects.PARTY_INVITE_SEND_NOTIFY)
    private void handlePartyInvite(PartyInvitePacket packet) {
        pm.trackInviteSent(packet.getPartyInvite(), false);
    }

    @PacketHandler(subject = Subjects.PARTY_INVITE_EXPIRE_NOTIFY)
    private void handlePartyInviteExpire(PartyInviteExpirePacket packet) {
        pm.trackInviteExpired(packet.getRequest(), packet.getParty(), packet.getSender(), packet.getRecipient());
    }
}
