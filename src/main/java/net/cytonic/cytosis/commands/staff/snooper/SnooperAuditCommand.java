package net.cytonic.cytosis.commands.staff.snooper;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.CommandUtils;
import net.cytonic.cytosis.menus.Menu;
import net.cytonic.cytosis.menus.snooper.SnooperMenu;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentStringArray;
import net.minestom.server.command.builder.arguments.ArgumentType;

public class SnooperAuditCommand extends Command {
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
            if (!Cytosis.getSnooperManager().getAllChannels(player).contains(rawChannel)) {
                player.sendMessage(Msg.whoops("The channel '" + rawChannel + "' either doesn't exist, or you don't have access to it."));
                return;
            }

            String searchFor = String.join(" ", c.get(search));
            if (searchFor.contains("--force")) {
                searchFor = searchFor.replace("--force", "");
                Cytosis.getSnooperManager().getPersistenceManager().invalidateCache();
                player.sendMessage(Msg.mm("<yellow><b>CLEARED!</b></yellow><gray> Invalidated the cached snoops!"));
            }
            boolean ascending = true;
            if (searchFor.contains("--descending")) {
                searchFor = searchFor.replace("--descending", "");
                ascending = false;
            }

            Menu inventory = new SnooperMenu(rawChannel, player, searchFor, ascending);
            player.openInventory(inventory);


        }, SnooperCommand.CHANNELS, search);
    }
}
