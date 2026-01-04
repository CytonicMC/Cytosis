package net.cytonic.cytosis.parties;

import net.cytonic.cytosis.Bootstrappable;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.bootstrap.annotations.CytosisComponent;
import net.cytonic.cytosis.data.packets.Packet;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.messaging.NatsManager;
import net.cytonic.cytosis.messaging.Subjects;
import net.cytonic.cytosis.parties.packets.PartyInviteExpirePacket;
import net.cytonic.cytosis.parties.packets.PartyOnePlayerPacket;
import net.cytonic.cytosis.parties.packets.PartyStatePacket;
import net.cytonic.cytosis.parties.packets.PartyTwoPlayerPacket;

@CytosisComponent(dependsOn = {NatsManager.class, PartyManager.class})
//@SuppressWarnings("unused")
public class PartyIpcHelper implements Bootstrappable {

    private final NatsManager nats = Cytosis.get(NatsManager.class);
    private final PartyManager pm = Cytosis.get(PartyManager.class);

    @Override
    public void init() {
        listenForPartyCreates();
        listenForPartyJoins();
        listenForPartyLeaves();
        listenForPartyPromotes();
        listenForPartyKicks();
        listenForPartyTransfers();
        listenForPartyStateChange();
        listenForPartyYoinks();
        listenForDisbands();
        listenForStatus();
        listenForInvite();
        listenForInviteExpiry();
    }

    private void listenForPartyJoins() {
        nats.subscribe(Subjects.PREFIX + "party.join.notify", msg -> {
            PartyOnePlayerPacket p = Packet.deserialize(msg.getData(), PartyOnePlayerPacket.class);
            pm.trackPlayerJoin(p.party(), p.player());
        });
    }

    private void listenForPartyCreates() {
        nats.subscribe(Subjects.PREFIX + "party.create.notify", msg -> pm.trackParty(Party.deserialize(msg.getData())));
    }

    private void listenForPartyLeaves() {
        nats.subscribe(Subjects.PREFIX + "party.leave.notify.*", msg -> {
            String subject = msg.getSubject().split("\\.")[3];
            if (subject.equals("request")) return;

            PartyOnePlayerPacket p = Packet.deserialize(msg.getData(), PartyOnePlayerPacket.class);

            switch (subject) {
                case "command" -> pm.trackPlayerLeave(p.party(), p.player());
                case "disconnected" -> pm.trackPlayerLeaveDisconnect(p.party(), p.player());
                default -> Logger.warn("Invalid leave subject '%s'", subject);
            }
        });
    }

    private void listenForPartyPromotes() {
        nats.subscribe(Subjects.PREFIX + "party.promote.*", msg -> {
            String subject = msg.getSubject().split("\\.")[2];
            if (subject.equals("request")) return;

            PartyTwoPlayerPacket packet = Packet.deserialize(msg.getData(), PartyTwoPlayerPacket.class);

            switch (subject) {
                case "moderator" -> pm.trackPromotionToModerator(packet.party(), packet.sender(), packet.player());
                case "leader" -> pm.trackPromotionToLeader(packet.party(), packet.sender(), packet.player());
                default -> Logger.warn("Received invalid promote subject: %s", subject);
            }
        });
    }

    private void listenForPartyKicks() {
        nats.subscribe(Subjects.PREFIX + "party.kick.notify", msg -> {
            PartyTwoPlayerPacket packet = Packet.deserialize(msg.getData(), PartyTwoPlayerPacket.class);
            pm.trackKick(packet.party(), packet.sender(), packet.player());
        });
    }

    private void listenForPartyTransfers() {
        nats.subscribe(Subjects.PREFIX + "party.transfer.*", msg -> {
            String subject = msg.getSubject().split("\\.")[2];
            if (subject.equals("request")) return;

            PartyTwoPlayerPacket packet = Packet.deserialize(msg.getData(), PartyTwoPlayerPacket.class);
            switch (subject) {
                case "command" -> pm.trackTransferCommand(packet.party(), packet.sender(), packet.player());
                case "left" -> pm.trackTransferLeft(packet.party(), packet.sender(), packet.player());
                case "disconnected" -> pm.trackTransferDisconnected(packet.party(), packet.sender(), packet.player());
                default -> Logger.warn("Received invalid transfer subject: %s", subject);
            }
        });
    }

    private void listenForPartyStateChange() {
        nats.subscribe(Subjects.PREFIX + "party.state.*.notify", msg -> {
            String subject = msg.getSubject().split("\\.")[2];

            PartyStatePacket packet = Packet.deserialize(msg.getData(), PartyStatePacket.class);

            switch (subject) {
                case "mute" -> pm.trackPartyMuteChange(packet.party(), packet.player(), packet.state());
                case "open" -> pm.trackPartyOpenChange(packet.party(), packet.player(), packet.state());
                case "open_invites" -> pm.trackPartyOpenInviteChange(packet.party(), packet.player(), packet.state());
                default -> Logger.warn("Received invalid state subject: %s", subject);
            }
        });
    }

    private void listenForPartyYoinks() {
        nats.subscribe(Subjects.PREFIX + "party.yoink.notify", msg -> {
            PartyOnePlayerPacket packet = Packet.deserialize(msg.getData(), PartyOnePlayerPacket.class);
            pm.trackYoink(packet.party(), packet.player());
        });
    }

    private void listenForDisbands() {
        nats.subscribe(Subjects.PREFIX + "party.disband.notify.*", msg -> {
            String subject = msg.getSubject().split("\\.")[3];

            PartyOnePlayerPacket packet = Packet.deserialize(msg.getData(), PartyOnePlayerPacket.class);

            switch (subject) {
                case "empty" -> pm.trackEmptyPartyDisband(packet.party());
                case "command" -> pm.trackPartyDisband(packet.party(), packet.player());
                default -> Logger.warn("Received invalid disband subject: %s", subject);
            }
        });
    }

    private void listenForStatus() {
        nats.subscribe(Subjects.PREFIX + "party.status.*", msg -> {
            String subject = msg.getSubject().split("\\.")[2];
            PartyOnePlayerPacket packet = Packet.deserialize(msg.getData(), PartyOnePlayerPacket.class);
            switch (subject) {
                case "disconnect" -> pm.notifyPlayerDisconnect(packet.party(), packet.player());
                case "reconnect" -> pm.notifyPlayerReconnect(packet.party(), packet.player());
                default -> Logger.warn("Invalid status subject '%s'", subject);
            }
        });
    }

    private void listenForInvite() {
        nats.subscribe(Subjects.PREFIX + "party.invites.send.notify", msg -> {
            PartyInvite inv = Cytosis.GSON.fromJson(new String(msg.getData()), PartyInvite.class);
            pm.trackInviteSent(inv, false);
        });
    }

    private void listenForInviteExpiry() {
        nats.subscribe(Subjects.PREFIX + "parties.invite.expire", msg -> {
            PartyInviteExpirePacket packet = Packet.deserialize(msg.getData(), PartyInviteExpirePacket.class);
            pm.trackInviteExpired(packet.request(), packet.party(), packet.sender(), packet.recipient());
        });
    }
}
