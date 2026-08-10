package net.cytonic.cytosis.commands.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import lombok.NoArgsConstructor;
import net.minestom.server.command.CommandManager;
import net.minestom.server.entity.Player;

import net.cytonic.cytosis.Bootstrappable;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.bootstrap.annotations.CytosisComponent;
import net.cytonic.cytosis.commands.HelpCommand;
import net.cytonic.cytosis.commands.chatting.AllChatCommand;
import net.cytonic.cytosis.commands.chatting.ChatChannelCommand;
import net.cytonic.cytosis.commands.chatting.IgnoreChatChannelCommand;
import net.cytonic.cytosis.commands.chatting.MsgCommand;
import net.cytonic.cytosis.commands.chatting.PartyChatCommand;
import net.cytonic.cytosis.commands.chatting.ReplyCommand;
import net.cytonic.cytosis.commands.chatting.ToggleChatPingCommand;
import net.cytonic.cytosis.commands.debug.cooldowns.CooldownCommand;
import net.cytonic.cytosis.commands.debug.particles.ParticleCommand;
import net.cytonic.cytosis.commands.debug.preferences.PreferenceCommand;
import net.cytonic.cytosis.commands.defaultMinecraft.GamemodeCommand;
import net.cytonic.cytosis.commands.defaultMinecraft.TeleportCommand;
import net.cytonic.cytosis.commands.disabling.DisableCommand;
import net.cytonic.cytosis.commands.disabling.EnableCommand;
import net.cytonic.cytosis.commands.friends.FriendCommand;
import net.cytonic.cytosis.commands.moderation.BanCommand;
import net.cytonic.cytosis.commands.moderation.ClearchatCommand;
import net.cytonic.cytosis.commands.moderation.KickCommand;
import net.cytonic.cytosis.commands.moderation.MuteCommand;
import net.cytonic.cytosis.commands.moderation.UnbanCommand;
import net.cytonic.cytosis.commands.moderation.UnmuteCommand;
import net.cytonic.cytosis.commands.moderation.VanishCommand;
import net.cytonic.cytosis.commands.movement.LobbyCommand;
import net.cytonic.cytosis.commands.movement.PlayCommand;
import net.cytonic.cytosis.commands.nicknames.NickCommand;
import net.cytonic.cytosis.commands.party.PartyCommand;
import net.cytonic.cytosis.commands.server.BroadcastCommand;
import net.cytonic.cytosis.commands.server.LinkCommand;
import net.cytonic.cytosis.commands.server.PingCommand;
import net.cytonic.cytosis.commands.server.RecalculatePermissionsCommand;
import net.cytonic.cytosis.commands.server.ReportCommand;
import net.cytonic.cytosis.commands.server.ServerCommand;
import net.cytonic.cytosis.commands.server.StopCommand;
import net.cytonic.cytosis.commands.server.TimeCommand;
import net.cytonic.cytosis.commands.server.TpsCommand;
import net.cytonic.cytosis.commands.server.WhereAmiCommand;
import net.cytonic.cytosis.commands.server.YoinkCommand;
import net.cytonic.cytosis.commands.server.nomad.AllocationDetailsCommand;
import net.cytonic.cytosis.commands.server.nomad.CreateInstanceCommand;
import net.cytonic.cytosis.commands.server.nomad.ShutdownInstancesCommand;
import net.cytonic.cytosis.commands.server.nomad.UpdateInstancesCommand;
import net.cytonic.cytosis.commands.server.whitelist.WhitelistCommand;
import net.cytonic.cytosis.commands.server.worlds.ImportWorld;
import net.cytonic.cytosis.commands.staff.FindCommand;
import net.cytonic.cytosis.commands.staff.FlyCommand;
import net.cytonic.cytosis.commands.staff.LoopCommand;
import net.cytonic.cytosis.commands.staff.OpMeCommand;
import net.cytonic.cytosis.commands.staff.RankCommand;
import net.cytonic.cytosis.commands.staff.ServerAlertsCommand;
import net.cytonic.cytosis.commands.staff.snooper.SnooperCommand;
import net.cytonic.cytosis.metrics.Metrics;
import net.cytonic.cytosis.metrics.MetricsManager;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.cytosis.utils.Utils;

/**
 * A class that handles the commands, their execution, and allegedly a console.
 */
@NoArgsConstructor
@CytosisComponent(priority = 2, dependsOn = {CommandManager.class})
public class CommandHandler implements Bootstrappable {

    //todo: I (Foxikle) think we could use jandex for this?
    private static final List<CytosisCommand> COMMANDS = Utils.list(
        new GamemodeCommand(), new RankCommand(), new BanCommand(), new ChatChannelCommand(),
        new StopCommand(), new ServerCommand(), new CreateInstanceCommand(), new ShutdownInstancesCommand(),
        new AllocationDetailsCommand(), new TeleportCommand(), new FindCommand(), new PreferenceCommand(),
        new ServerAlertsCommand(), new FlyCommand(), new BroadcastCommand(), new HelpCommand(),
        new AllChatCommand(), new TimeCommand(), new PingCommand(), new FriendCommand(), new ClearchatCommand(),
        new VanishCommand(), new IgnoreChatChannelCommand(), new UnbanCommand(), new MuteCommand(),
        new UnmuteCommand(), new KickCommand(), new MsgCommand(), new CooldownCommand(),
        new LoopCommand(), new RecalculatePermissionsCommand(), new YoinkCommand(), new ReplyCommand(),
        new SnooperCommand(), new PlayCommand(), new LobbyCommand(), new WhereAmiCommand(), new DisableCommand(),
        new EnableCommand(), new UpdateInstancesCommand(), new NickCommand(),
        new ImportWorld(), new ToggleChatPingCommand(), new ParticleCommand(), new OpMeCommand(),
        new WhitelistCommand(), new PartyCommand(), new PartyChatCommand(), new TpsCommand(), new ReportCommand(),
        new DummyCommand(), new LinkCommand(), new MiniMessageCommand()
    );

    private final Map<Class<? extends CytosisCommand>, CytosisCommand> commandMap = new HashMap<>();

    private CommandManager commandManager;

    @Override
    public void init() {
        this.commandManager = Cytosis.get(CommandManager.class);
    }

    /**
     * Registers the default Cytosis commands
     */
    public void registerCommands() {
        commandManager.setUnknownCommandCallback((commandSender, s) -> {
            if (!(commandSender instanceof CytosisPlayer player)) return;
            player.sendMessage(Msg.redSplash("UNKNOWN COMMAND!", "The command '/%s' does not exist.", s));
            Cytosis.get(MetricsManager.class).addToLongCounter(Metrics.UNKNOWN_COMMANDS, 1, Attributes.of(
                //todo: (Foxikle) This seems like it might be an invasion of privacy, and should be disclosed, or possibly even disabled.
                AttributeKey.stringKey("uuid"), player.getUuid().toString(),
                AttributeKey.stringKey("rank"), player.getRank().name().toLowerCase()
            ));
        });
        for (CytosisCommand command : COMMANDS) {
            commandMap.put(command.getClass(), command);
            commandManager.register(command);
        }
    }

    /**
     * Adds a command as a subcommand of an existing command. Use case: Adding implementation specific subcommands. i.e.
     * attaching more commands to the debug command.
     *
     * @return if the sub command was successfully registered to the parent
     */
    public boolean attachSubCommand(Class<? extends CytosisCommand> parent, CytosisCommand sub) {
        if (!commandMap.containsKey(parent)) return false;
        commandMap.get(parent).addSubcommand(sub);
        return true;
    }

    /**
     * Adds commands as subcommands of an existing command. Use case: Adding implementation specific subcommands. i.e.
     * attaching more commands to the debug command.
     *
     * @return if the sub commands were successfully registered to the parent
     */
    public boolean attachSubCommands(Class<? extends CytosisCommand> parent, CytosisCommand... subs) {
        if (!commandMap.containsKey(parent)) return false;
        commandMap.get(parent).addSubcommands(subs);
        return true;
    }

    /**
     * Sends a packet to the player to recalculate command permissions
     *
     * @param player The player to send the packet to
     */
    public void recalculateCommands(Player player) {
        player.sendPacket(commandManager.createDeclareCommandsPacket(player));
    }
}
