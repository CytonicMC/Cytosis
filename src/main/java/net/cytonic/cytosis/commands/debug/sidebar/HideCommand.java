package net.cytonic.cytosis.commands.debug.sidebar;

import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.commands.utils.SubCommand;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.sidebar.SidebarComponent;
import net.cytonic.cytosis.utils.Msg;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;

@SubCommand(SidebarTestCommand.class)
public class HideCommand extends CytosisCommand {

    public HideCommand() {
        super("hide");

        this.setDefaultExecutor((sender, ctx) -> sender.sendMessage(Msg.whoops("You must specify a component ID to hide!")));

        var componentIDArgument = ArgumentType.Word("componentID").setSuggestionCallback((sender, ctx, suggestion) -> {
            if(!(sender instanceof CytosisPlayer player)) return;

            if(player.getCurrentSidebar() == null) return;

            for(SidebarComponent<CytosisPlayer> component : player.getCurrentSidebar().getComponents()) {
                if(player.isViewingComponent(component) && component.getId() != null) {
                    suggestion.addEntry(new SuggestionEntry(component.getId()));
                }
            }
        });

        this.addSyntax((sender, ctx) -> {
            CytosisPlayer player = (CytosisPlayer) sender;

            String id = ctx.get(componentIDArgument);
            player.sendMessage("Hiding every component with ID " + id);

            for(SidebarComponent<CytosisPlayer> component : player.getCurrentSidebar().getComponents()) {
                if(id.equals(component.getId())) {
                    player.hideComponent(component);
                }
            }

        }, componentIDArgument);

    }

}
