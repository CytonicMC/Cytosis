package net.cytonic.cytosis.managers;

import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import net.minestom.server.command.CommandManager;
import org.jetbrains.annotations.Nullable;

import net.cytonic.cytosis.Bootstrappable;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.data.RedisDatabase;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.messaging.NatsManager;

public class CommandDisablingManager implements Bootstrappable {

    private NatsManager nats;
    private RedisDatabase redis;

    @Override
    public void init() {
        this.nats = Cytosis.CONTEXT.getComponent(NatsManager.class);
        this.redis = Cytosis.CONTEXT.getComponent(RedisDatabase.class);
        loadRemotes();
        setupConsumers();
    }

    public void setupConsumers() {
        nats.subscribe("cytosis.commands.disabled", (msg) -> {
            String toDisable = new String(msg.getData());
            Logger.info("Disabling command: " + toDisable);
            CytosisCommand cc = parseCommand(toDisable);
            if (cc != null) {
                cc.setDisabled(true);
            } else {
                Logger.warn("Failed to parse and disable command: " + toDisable);
            }
        });

        nats.subscribe("cytosis.commands.enabled", (msg) -> {
            String toEnable = new String(msg.getData());
            Logger.info("Enabling command: " + toEnable);
            CytosisCommand cc = parseCommand(toEnable);
            if (cc != null) {
                cc.setDisabled(false);
            } else {
                Logger.warn("Failed to parse and enable command: " + toEnable);
            }
        });
    }

    /**
     * Disables a command locally, so it's only disabled on this server.
     *
     * @param cmd The command to disable
     * @return if the command was successfully disabled.
     */
    public boolean disableCommandLocally(CytosisCommand cmd) {
        if (cmd.isDisabled()) {
            return false;
        }
        cmd.setDisabled(true);
        return true;
    }

    /**
     * Enables a command locally, so it is enabled on this server.
     *
     * @param cmd the command to re-enable
     * @return if the command was successfully enabled again
     */
    public boolean enableCommandLocally(CytosisCommand cmd) {
        if (!cmd.isDisabled()) {
            return false;
        }
        cmd.setDisabled(false);
        return true;
    }

    /**
     * Globally disables the given command. Normal players will not be able to use the command on any server.
     * Administrators can bypass this, though.
     *
     * @param cmd the command to disable everywhere
     * @return if the command was successfully disabled
     */
    public boolean disableCommandGlobally(CytosisCommand cmd) {
        sendCommandDisable(cmd.getName().getBytes(StandardCharsets.UTF_8));
        redis.addValue("cytosis-disabled-commands", cmd.getName());
        return true;
    }

    private void sendCommandDisable(byte[] message) {
        Cytosis.CONTEXT.getComponent(NatsManager.class).publish("cytosis.commands.disabled", message);
    }

    /**
     * Globally enables this command, meaning it will be re-enabled on every server.
     *
     * @param cmd the command to re-enable globally
     * @return if the command was re-enabled.
     */
    public boolean enableCommandGlobally(CytosisCommand cmd) {
        sendCommandEnable(cmd.getName().getBytes(StandardCharsets.UTF_8));
        redis.removeValue("cytosis-disabled-commands", cmd.getName());
        return true;
    }

    @Nullable
    private CytosisCommand parseCommand(String rawCommand) {
        if (Cytosis.CONTEXT.getComponent(CommandManager.class).getCommand(rawCommand) instanceof CytosisCommand cc) {
            return cc;
        }
        return null;
    }

    public CompletableFuture<Void> loadRemotes() {
        return CompletableFuture.supplyAsync(() -> {

            Set<String> cmds = redis.getSet("cytosis-disabled-commands");

            for (String cmd : cmds) {
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

    private void sendCommandEnable(byte[] message) {
        nats.publish("cytosis.commands.enabled", message);
    }

    public boolean isDisabledGlobally(CytosisCommand cmd) {
        return redis.getSet("cytosis-disabled-commands")
            .contains(cmd.getName());
    }

    public boolean isDisabledLocally(CytosisCommand cmd) {
        return cmd.isDisabled();
    }
}
