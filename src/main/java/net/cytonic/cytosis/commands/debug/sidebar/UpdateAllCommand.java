package net.cytonic.cytosis.commands.debug.sidebar;

import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.commands.utils.SubCommand;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.sidebar.SidebarComponent;

@SubCommand(SidebarTestCommand.class)
public class UpdateAllCommand extends CytosisCommand {

    public UpdateAllCommand() {
        super("updateall");

        this.setDefaultExecutor((sender, ctx) -> {
            if (!(sender instanceof CytosisPlayer player)) return;

            if (player.getCurrentSidebar() == null) return;

            player.sendMessage("Updating every updatable component...");

            for (SidebarComponent<CytosisPlayer> component : player.getCurrentSidebar().getComponents()) {
                if (!component.isStatic()) {
                    player.sendMessage("Updating component " + component.getId());

                    player.updateComponent(component);
                }
            }
        });
    }

}
