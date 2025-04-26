package net.cytonic.cytosis.commands.util;

import lombok.NoArgsConstructor;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.FriendCommand;
import net.cytonic.cytosis.commands.HelpCommand;
import net.cytonic.cytosis.commands.chatting.*;
import net.cytonic.cytosis.commands.debug.CooldownCommand;
import net.cytonic.cytosis.commands.debug.PreferenceCommand;
import net.cytonic.cytosis.commands.defaultMinecraft.GamemodeCommand;
import net.cytonic.cytosis.commands.defaultMinecraft.TeleportCommand;
import net.cytonic.cytosis.commands.disabling.DisableCommand;
import net.cytonic.cytosis.commands.disabling.EnableCommand;
import net.cytonic.cytosis.commands.moderation.*;
import net.cytonic.cytosis.commands.movement.LobbyCommand;
import net.cytonic.cytosis.commands.movement.PlayCommand;
import net.cytonic.cytosis.commands.nicknames.NickCommand;
import net.cytonic.cytosis.commands.nicknames.NickRevealCommand;
import net.cytonic.cytosis.commands.server.*;
import net.cytonic.cytosis.commands.server.nomad.AllocationDetailsCommand;
import net.cytonic.cytosis.commands.server.nomad.CreateInstanceCommand;
import net.cytonic.cytosis.commands.server.nomad.ShutdownInstancesCommand;
import net.cytonic.cytosis.commands.server.nomad.UpdateInstancesCommand;
import net.cytonic.cytosis.commands.staff.*;
import net.cytonic.cytosis.commands.staff.snooper.SnooperCommand;
import net.minestom.server.command.CommandManager;
import net.minestom.server.entity.Player;

/**
 * A class that handles the commands, their execution, and allegedly a console.
 */
@NoArgsConstructor
public class CommandHandler {

    /**
     * Registers the default Cytosis commands
     */
    public void registerCytosisCommands() {
        CommandManager cm = Cytosis.getCommandManager();
        cm.register(
                new GamemodeCommand(),
                new RankCommand(),
                new BanCommand(),
                new ChatChannelCommand(),
                new StopCommand(),
                new ServerCommand(),
                new CreateInstanceCommand(),
                new ShutdownInstancesCommand(),
                new AllocationDetailsCommand(),
                new TeleportCommand(),
                new FindCommand(),
                new PreferenceCommand(),
                new ServerAlertsCommand(),
                new FlyCommand(),
                new BroadcastCommand(),
                new HelpCommand(),
                new AllChatCommand(),
                new TimeCommand(),
                new VersionCommand(),
                new PingCommand(),
                new FriendCommand(),
                new ClearchatCommand(),
                new VanishCommand(),
                new IgnoreChatChannelCommand(),
                new UnbanCommand(),
                new MuteCommand(),
                new UnmuteCommand(),
                new KickCommand(),
                new MsgCommand(),
                new WarnCommand(),
                new CooldownCommand(Cytosis.getNetworkCooldownManager()),
                new LoopCommand(),
                new RecalculatePermissionsCommand(),
                new YoinkCommand(),
                new ReplyCommand(),
                new SnooperCommand(),
                new PlayCommand(),
                new LobbyCommand(),
                new WhereAmiCommand(),
                new DisableCommand(),
                new EnableCommand(),
                new UpdateInstancesCommand(),
                new NickCommand(),
                new NickRevealCommand()
        );
    }

    /**
     * Sends a packet to the player to recalculate command permissions
     *
     * @param player The player to send the packet to
     */
    public void recalculateCommands(Player player) {
        player.sendPacket(Cytosis.getCommandManager().createDeclareCommandsPacket(player));
    }
}
