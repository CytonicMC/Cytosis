package net.cytonic.cytosis.commands.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import lombok.NoArgsConstructor;
import net.minestom.server.command.CommandManager;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.Nullable;

import net.cytonic.cytosis.Bootstrappable;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.bootstrap.annotations.CytosisComponent;
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

    private final List<CytosisCommand> COMMANDS = JandexUtils.getExtendedClasses(CytosisCommand.class);
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
            commandManager.register(command);
        }
        // special case
        commandManager.register(DummyCommand.INSTANCE);
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
        commandManager.unregister(DummyCommand.INSTANCE);
        commandManager.register(DummyCommand.INSTANCE);
        return true;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T extends CytosisCommand> T getCommand(Class<T> clazz) {
        return (T) commandMap.getOrDefault(clazz, null);
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
