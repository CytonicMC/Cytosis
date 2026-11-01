package net.cytonic.cytosis.commands.staff.snooper;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.minestom.server.command.builder.arguments.ArgumentStringArray;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CommandUtils;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.commands.utils.SubCommand;
import net.cytonic.cytosis.managers.SnooperManager;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.snooper.SnooperChannel;
import net.cytonic.cytosis.utils.Msg;

@SubCommand(SnooperCommand.class)
class TestCommand extends CytosisCommand {

    TestCommand() {
        super("test");
        setCondition(CommandUtils.IS_ADMIN);

        setDefaultExecutor((commandSender, _) -> {
            commandSender.sendMessage("Invalid syntax");
        });

        ArgumentStringArray message = new ArgumentStringArray("message");
        message.setDefaultValue(new String[]{});
        addSyntax((s, ctx) -> {
            if (!(s instanceof CytosisPlayer player)) return;
            String rawChannel = ctx.getRaw(SnooperCommand.CHANNELS);
            SnooperManager snooperManager = Cytosis.get(SnooperManager.class);
            SnooperChannel realChannel = snooperManager.getChannel(Key.key(rawChannel));
            if (realChannel == null) {
                player.whoops("The channel '" + rawChannel + "' doesn't exist!");
                return;
            }

            String rawMessage = ctx.getRaw(message);
            Component component = Msg.mm("<reset>" + rawMessage).appendNewline()
                .append(Msg.mm("<reset><dark_gray><i>Sent by " + player.getUsername() + " via /snooper test"));
            snooperManager.sendSnoop(realChannel, Msg.snoop(component));
            player.success("Sent snoop!");
        }, SnooperCommand.CHANNELS, message);
    }
}