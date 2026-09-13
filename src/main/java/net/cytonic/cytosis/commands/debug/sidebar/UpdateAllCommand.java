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
            CytosisPlayer player = (CytosisPlayer) sender;

            if(player.getCurrentSidebar() == null) return;

            player.sendMessage("Updating every updatable component...");

            for(SidebarComponent<CytosisPlayer> component : player.getCurrentSidebar().getComponents()) {
                if(!component.isFullyStatic()) {
                    player.sendMessage("Updating component " + component.getId());

                    player.updateComponent(component);
                }
            }
        });
    }

}
