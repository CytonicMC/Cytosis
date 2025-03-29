package net.cytonic.cytosis.commands.debug;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.CommandUtils;
import net.cytonic.cytosis.commands.CytosisCommand;
import net.cytonic.cytosis.managers.NetworkCooldownManager;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.DurationParser;
import net.cytonic.cytosis.utils.Msg;
import net.kyori.adventure.key.Key;
import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;

import java.time.Instant;

/**
 * A command to debug cooldowns
 */
public class CooldownCommand extends CytosisCommand {

    /**
     * A command to debug cooldowns
     *
     * @param network the network manager
     */
    public CooldownCommand(NetworkCooldownManager network) {
        super("cooldown");

        setCondition(CommandUtils.IS_ADMIN);
        setDefaultExecutor((sender, cmdc) -> sender.sendMessage(Msg.mm("<red>Invalid syntax! Use '/cooldown help' for more information.")));

        var action = ArgumentType.Enum("action", CooldownAction.class).setFormat(ArgumentEnum.Format.LOWER_CASED);

        var nodeArg = ArgumentType.Word("node");
        nodeArg.setSuggestionCallback((cmds, cmdc, suggestion) -> {
            for (Key preference : network.getAllKeys()) {
                suggestion.addEntry(new SuggestionEntry(preference.asString()));
            }
        });

        var durationArg = ArgumentType.StringArray("duration").setDefaultValue(new String[0]);

        addSyntax((sender, context) -> {
            if (!(sender instanceof CytosisPlayer player)) {
                sender.sendMessage("You cannot do this!");
                return;
            }
            CooldownAction ac = context.get(action);

            if (ac == CooldownAction.HELP) {
                sender.sendMessage(Msg.greenSplash("Cooldown Help", "<newline><i>A debug command for testing the Cytosis cooldown api.</i></gray><newline><newline> <newline> <gold>/cooldown set_personal <node> <expiry></gold><gray><i> Sets a personal cooldown</i></gray> <newline> <gold>/cooldown set_global <node> <expiry></gold><gray><i> Sets a global cooldown</i></gray> <newline> <gold>/cooldown clear_personal <node></gold><gray><i> Resets a personal cooldown</i></gray> <newline> <gold>/cooldown clear_global <node></gold><gray><i> Resets a global cooldown</i></gray><newline> <gold>/cooldown help</gold><gray><i> Displays this message</i></gray><newline> <gold>/cooldown get_global <node></gold><gray><i> Gets the expiry of a global cooldown node</i></gray><newline> <gold>/cooldown get_personal <node></gold><gray><i> Gets the expiry of a personal cooldown node</i></gray>"));
            } else {
                player.sendMessage(Msg.whoops("Invalid syntax! Use '/cooldown help' for more information!"));
            }
        }, action);

        addSyntax((sender, context) -> {
            if (!(sender instanceof CytosisPlayer player)) {
                sender.sendMessage("You cannot do this!");
                return;
            }
            CooldownAction ac = context.get(action);

            if (ac == CooldownAction.HELP) {
                Cytosis.getCommandManager().getDispatcher().execute(player, "cooldown help");
                return;
            }

            Key node = Key.key(context.get(nodeArg));

            if (ac == CooldownAction.CLEAR_GLOBAL) {
                network.resetGlobalCooldown(node);
                player.sendMessage(Msg.success("Reset the global cooldown '<yellow>" + node.asString() + "</yellow>'."));
            } else if (ac == CooldownAction.CLEAR_PERSONAL) {
                network.resetGlobalCooldown(node);
                player.sendMessage(Msg.success("Reset your personal cooldown '<yellow>" + node.asString() + "</yellow>'."));
            } else if (ac == CooldownAction.GET_GLOBAL) {
                if (network.isOnGlobalCooldown(node)) {
                    Instant expires = network.getGlobalExpiry(node);
                    player.sendMessage(Msg.yellowSplash("TICK TOCK!", "The global cooldown '<yellow>" + node.asString() + "</yellow>' is set to expire in " + DurationParser.unparseFull(expires) + "."));
                    return;
                }
                player.sendMessage(Msg.whoops("The global cooldown '<yellow>" + node.asString() + "</yellow>' isn't active!"));
                return;
            } else if (ac == CooldownAction.GET_PERSONAL) {
                if (network.isOnPersonalCooldown(player.getUuid(), node)) {
                    Instant expires = network.getPersonalExpiry(player.getUuid(), node);
                    player.sendMessage(Msg.yellowSplash("TICK TOCK!", "Your personal cooldown '<yellow>" + node.asString() + "</yellow>' is set to expire in " + DurationParser.unparseFull(expires) + "."));
                    return;
                }
                player.sendMessage(Msg.whoops("Your personal cooldown '<yellow>" + node.asString() + "</yellow>' isn't active!"));
                return;
            }

            String[] duration = context.get(durationArg);

            if (duration.length == 0) {
                player.sendMessage(Msg.whoops("You must provide a duration!"));
                return;
            }

            Instant expiry = DurationParser.parse(String.join(" ", duration));

            switch (ac) {
                case SET_GLOBAL -> {
                    network.setGlobal(node, expiry);
                    player.sendMessage(Msg.success("Set the global cooldown '<yellow>" + node.asString() + "</yellow>' to expire in " + DurationParser.unparseFull(expiry) + "."));
                }
                case SET_PERSONAL -> {
                    network.setPersonal(player.getUuid(), node, expiry);
                    player.sendMessage(Msg.success("Set your personal cooldown '<yellow>" + node.asString() + "</yellow>' to expire in " + DurationParser.unparseFull(expiry)));
                }
            }
        }, action, nodeArg, durationArg);
    }


    enum CooldownAction {
        SET_GLOBAL,
        SET_PERSONAL,
        CLEAR_PERSONAL,
        CLEAR_GLOBAL,
        HELP,
        GET_PERSONAL,
        GET_GLOBAL,
    }
}
