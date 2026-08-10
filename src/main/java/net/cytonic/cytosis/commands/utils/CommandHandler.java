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
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.metrics.Metrics;
import net.cytonic.cytosis.metrics.MetricsManager;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.protocol.utils.JandexUtils;

/**
 * A class that handles the commands, their execution, and allegedly a console.
 */
@NoArgsConstructor
@CytosisComponent(priority = 2, dependsOn = {CommandManager.class})
public class CommandHandler implements Bootstrappable {

    //todo: I (Foxikle) think we could use jandex for this?
    private final List<CytosisCommand> COMMANDS = JandexUtils.getExtendedClasses(CytosisCommand.class);
//    Utils.list(
//        new GamemodeCommand(), new RankCommand(), new BanCommand(), new ChatChannelCommand(),
//        new StopCommand(), new ServerCommand(), new CreateInstanceCommand(), new ShutdownInstancesCommand(),
//        new AllocationDetailsCommand(), new TeleportCommand(), new FindCommand(), new PreferenceCommand(),
//        new ServerAlertsCommand(), new FlyCommand(), new BroadcastCommand(), new HelpCommand(),
//        new AllChatCommand(), new TimeCommand(), new PingCommand(), new FriendCommand(), new ClearchatCommand(),
//        new VanishCommand(), new IgnoreChatChannelCommand(), new UnbanCommand(), new MuteCommand(),
//        new UnmuteCommand(), new KickCommand(), new MsgCommand(), new CooldownCommand(),
//        new LoopCommand(), new RecalculatePermissionsCommand(), new YoinkCommand(), new ReplyCommand(),
//        new SnooperCommand(), new PlayCommand(), new LobbyCommand(), new WhereAmiCommand(), new DisableCommand(),
//        new EnableCommand(), new UpdateInstancesCommand(), new NickCommand(),
//        new ImportWorld(), new ToggleChatPingCommand(), new ParticleCommand(), new OpMeCommand(),
//        new WhitelistCommand(), new PartyCommand(), new PartyChatCommand(), new TpsCommand(), new ReportCommand(),
//        new DummyCommand(), new LinkCommand(), new MiniMessageCommand()
//    );

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
            if (command.getClass().isAnnotationPresent(SubCommand.class)) continue;
            Logger.debug("Registering command: %s", command.getName());
            commandManager.register(command);
        }
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
