package net.cytonic.cytosis.commands.staff.snooper;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.command.builder.arguments.ArgumentStringArray;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CommandUtils;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.snooper.SnooperChannel;
import net.cytonic.cytosis.managers.SnooperManager;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;

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
            SnooperManager snooperManager = Cytosis.CONTEXT.getComponent(SnooperManager.class);
            SnooperChannel realChannel = snooperManager.getChannel(Key.key(rawChannel));
            if (realChannel == null) {
                s.sendMessage(Msg.whoops("The channel '" + rawChannel + "' doesn't exist!"));
                return;
            }

            String rawMessage = ctx.getRaw(message);
            Component component = MiniMessage.miniMessage().deserialize("<reset>" + rawMessage).appendNewline()
                .append(Msg.mm("<reset><dark_gray><i>Sent by " + player.getUsername() + " via /snooper test"));
            snooperManager.sendSnoop(realChannel, Msg.snoop(component));
            player.sendMessage(Msg.mm("<green>Sent snoop!"));
        }, SnooperCommand.CHANNELS, message);
    }
}