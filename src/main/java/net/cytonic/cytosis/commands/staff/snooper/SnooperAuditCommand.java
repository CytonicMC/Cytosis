package net.cytonic.cytosis.commands.staff.snooper;

import com.google.common.collect.ImmutableMap;
import me.devnatan.inventoryframework.ViewFrame;
import net.minestom.server.command.builder.arguments.ArgumentStringArray;
import net.minestom.server.command.builder.arguments.ArgumentType;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CommandUtils;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.managers.SnooperManager;
import net.cytonic.cytosis.menus.snooper.DateRange;
import net.cytonic.cytosis.menus.snooper.SnooperMenu;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;

public class SnooperAuditCommand extends CytosisCommand {
    public SnooperAuditCommand() {
        super("audit");

        ArgumentStringArray search = ArgumentType.StringArray("search");
        search.setDefaultValue(new String[]{});

        setCondition(CommandUtils.IS_STAFF);

        setDefaultExecutor((s, c) -> {
            if (!(s instanceof CytosisPlayer player)) return;
            player.sendMessage(Msg.whoops("Invalid Syntax: /snooper audit <channel>"));
        });

        addSyntax((s, c) -> {
            if (!(s instanceof CytosisPlayer player)) return;
            String rawChannel = c.get(SnooperCommand.CHANNELS);
            if (!Cytosis.CONTEXT.getComponent(SnooperManager.class).getAllChannels(player).contains(rawChannel)) {
                player.sendMessage(Msg.whoops("The channel '" + rawChannel + "' either doesn't exist, or you don't have access to it."));
                return;
            }

            String searchFor = String.join(" ", c.get(search));
            if (searchFor.contains("--force")) {
                searchFor = searchFor.replace("--force", "");
                player.sendMessage(Msg.yellowSplash("CLEARED!", "Invalidated the cached snoops!"));
            }
            boolean ascending = true;
            if (searchFor.contains("--descending")) {
                searchFor = searchFor.replace("--descending", "");
                ascending = false;
            }

            Cytosis.CONTEXT
                    .getComponent(ViewFrame.class)
                    .open(
                            SnooperMenu.class,
                            player,
                            ImmutableMap.of(
                                    "id", rawChannel,
                                    "search", searchFor,
                                    "ascending", ascending,
                                    "date", DateRange.SEVEN_DAYS));
        }, SnooperCommand.CHANNELS, search);
    }
}