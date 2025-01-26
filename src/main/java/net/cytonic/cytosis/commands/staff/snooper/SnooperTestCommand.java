package net.cytonic.cytosis.commands.staff.snooper;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.CommandUtils;
import net.cytonic.cytosis.data.containers.snooper.SnooperChannel;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.SnoopUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentStringArray;
import net.minestom.server.utils.NamespaceID;

import static net.cytonic.cytosis.utils.MiniMessageTemplate.MM;

public class SnooperTestCommand extends Command {
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
            SnooperChannel realChannel = Cytosis.getSnooperManager().getChannel(NamespaceID.from(rawChannel));
            if (realChannel == null) {
                s.sendMessage(MM."<red><b>WHOOPS!</b></red><gray> The channel '\{rawChannel}' doesn't exist!");
                return;
            }

            String rawMessage = ctx.getRaw(message);
            Component component = MiniMessage.miniMessage().deserialize("<reset>" + rawMessage).appendNewline().append(MM."<reset><dark_gray><i>Sent by \{player.getUsername()} via /snooper test");
            Cytosis.getSnooperManager().sendSnoop(realChannel, SnoopUtils.toSnoop(component));
            player.sendMessage(MM."<green>Sent snoop!");
        }, SnooperCommand.CHANNELS, message);

    }

}
