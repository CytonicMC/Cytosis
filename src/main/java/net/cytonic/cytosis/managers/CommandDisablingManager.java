package net.cytonic.cytosis.managers;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import net.minestom.server.command.CommandManager;
import net.minestom.server.command.builder.Command;
import org.jetbrains.annotations.Nullable;

import net.cytonic.cytosis.Bootstrappable;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.bootstrap.annotations.CytosisComponent;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.data.RedisDatabase;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.protocol.impl.notify.CommandDisableNotifyPacket;
import net.cytonic.protocol.utils.NotifyHandler;

@CytosisComponent(priority = 1, dependsOn = {CommandManager.class, RedisDatabase.class})
public class CommandDisablingManager implements Bootstrappable {

    private RedisDatabase redis;

    @Override
    public void init() {
        this.redis = Cytosis.get(RedisDatabase.class);
    }

    @NotifyHandler
    public void onEnable(CommandDisableNotifyPacket.Packet packet) {
        if (!packet.enable()) return;
        String command = packet.command();
        Logger.info("Enabling command: " + command);
        CytosisCommand cytosisCommand = parseCommand(command);
        if (cytosisCommand != null) {
            cytosisCommand.setDisabled(false);
            forAllSubcommands(cytosisCommand, subCommand -> subCommand.setDisabled(false));
        } else {
            Logger.warn("Failed to parse and enable command: " + command);
        }
    }

    @NotifyHandler
    public void onDisable(CommandDisableNotifyPacket.Packet packet) {
        if (packet.enable()) return;
        String command = packet.command();
        Logger.info("Disabling command: " + command);
        CytosisCommand cytosisCommand = parseCommand(command);
        if (cytosisCommand != null) {
            cytosisCommand.setDisabled(true);
            forAllSubcommands(cytosisCommand, subCommand -> subCommand.setDisabled(true));
        } else {
            Logger.warn("Failed to parse and disable command: " + command);
        }
    }

    public void forAllSubcommands(CytosisCommand cytosisCommand, Consumer<CytosisCommand> consumer) {
        for (Command subcommand : cytosisCommand.getSubcommands()) {
            if (subcommand instanceof CytosisCommand cytosisSubCommand) {
                consumer.accept(cytosisSubCommand);
                forAllSubcommands(cytosisSubCommand, consumer);
            }
        }
    }

    /**
     * Disables a command locally, so it's only disabled on this server.
     *
     * @param cmd The command to disable
     */
    public void disableCommandLocally(CytosisCommand cmd) {
        cmd.setDisabled(true);
    }

    /**
     * Enables a command locally, so it is enabled on this server.
     *
     * @param cmd the command to re-enable
     */
    public void enableCommandLocally(CytosisCommand cmd) {
        cmd.setDisabled(false);
    }

    /**
     * Globally enables this command, meaning it will be re-enabled on every server.
     *
     * @param command the command to re-enable globally
     */
    public void enableCommandGlobally(String command) {
        new CommandDisableNotifyPacket.Packet(command, true).publish();
        redis.removeValue("cytosis-disabled-commands", command);
    }

    /**
     * Globally disables the given command. Normal players will not be able to use the command on any server.
     * Administrators can bypass this, though.
     *
     * @param command the command to disable everywhere
     */
    public void disableCommandGlobally(String command) {
        new CommandDisableNotifyPacket.Packet(command, false).publish();
        redis.addValue("cytosis-disabled-commands", command);
    }

    @Nullable
    public CytosisCommand parseCommand(String rawCommand) {
        String[] parts = rawCommand.split(" ", -1);

        if (!(Cytosis.get(CommandManager.class).getCommand(parts[0]) instanceof CytosisCommand command)) {
            return null;
        }

        for (int i = 1; i < parts.length; i++) {
            String part = parts[i];
            CytosisCommand next = null;
            for (Command subcommand : command.getSubcommands()) {
                if (Arrays.asList(subcommand.getNames()).contains(part) && subcommand instanceof CytosisCommand sub) {
                    next = sub;
                    break;
                }
            }
            if (next == null) return null;
            command = next;
        }

        return command;
    }

    public void loadRemotes() {
        CompletableFuture.supplyAsync(() -> {

            Set<String> commands = redis.getSet("cytosis-disabled-commands");

            for (String cmd : commands) {
                CytosisCommand command = parseCommand(cmd);
                if (command != null) {
                    Logger.info("Disabling command: " + cmd);
                    command.setDisabled(true);
                } else {
                    Logger.warn("Failed to disable command: " + cmd);
                }
            }

            return null;
        });
    }

    public boolean isDisabledGlobally(String command) {
        return redis.getSet("cytosis-disabled-commands")
            .contains(command);
    }
}
