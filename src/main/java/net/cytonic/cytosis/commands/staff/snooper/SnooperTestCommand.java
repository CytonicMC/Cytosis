package net.cytonic.cytosis.commands.staff.snooper;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CommandUtils;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.data.containers.snooper.SnooperChannel;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.cytosis.utils.SnoopUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.command.builder.arguments.ArgumentStringArray;

public class SnooperTestCommand extends CytosisCommand {
    public SnooperTestCommand() {
        super("test");
        setCondition(CommandUtils.IS_ADMIN);

        setDefaultExecutor((commandSender, commandContext) -> {
            commandSender.sendMessage("Invalid syntax");
        });

        ArgumentStringArray message = new ArgumentStringArray("message");
        message.setDefaultValue(new String[]{});
        addSyntax((s, ctx) -> {
            if (!(s instanceof CytosisPlayer player)) return;
            String rawChannel = ctx.getRaw(SnooperCommand.CHANNELS);
            SnooperChannel realChannel = Cytosis.getSnooperManager().getChannel(Key.key(rawChannel));
            if (realChannel == null) {
                s.sendMessage(Msg.whoops("The channel '" + rawChannel + "' doesn't exist!"));
                return;
            }

            String rawMessage = ctx.getRaw(message);
            Component component = MiniMessage.miniMessage().deserialize("<reset>" + rawMessage).appendNewline().append(Msg.mm("<reset><dark_gray><i>Sent by " + player.getUsername() + " via /snooper test"));
            Cytosis.getSnooperManager().sendSnoop(realChannel, SnoopUtils.toSnoop(component));
            player.sendMessage(Msg.mm("<green>Sent snoop!"));
        }, SnooperCommand.CHANNELS, message);

    }

}
