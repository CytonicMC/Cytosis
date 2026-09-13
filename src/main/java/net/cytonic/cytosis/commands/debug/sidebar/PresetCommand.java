package net.cytonic.cytosis.commands.debug.sidebar;

import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.commands.utils.SubCommand;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.sidebar.Sidebar;
import net.cytonic.cytosis.sidebar.SidebarComponent;
import net.cytonic.cytosis.utils.Msg;
import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;

@SubCommand(SidebarTestCommand.class)
public class PresetCommand extends CytosisCommand {

    public PresetCommand() {
        super("preset", "create");

        ArgumentEnum<Preset> selectedPreset = ArgumentType.Enum("presetID", Preset.class).setFormat(ArgumentEnum.Format.LOWER_CASED);

        this.setDefaultExecutor((sender, ctx) -> {
            sender.sendMessage(Msg.whoops("You need to specify a preset to use this!"));
        });

        this.addSyntax((sender, ctx) -> {
            CytosisPlayer player = (CytosisPlayer) sender;

            Preset preset = ctx.get(selectedPreset);

            IPreset set = switch(preset) {
                case HYPIXEL_LOBBY -> HypixelLobbyPreset.PRESET;
            };

            player.sendMessage("Applying preset " + set.getClass().getName());

            long curr = System.currentTimeMillis();
            player.setCurrentSidebar(set.getSidebar());
            long took = System.currentTimeMillis() - curr;

            player.sendMessage("Applying took " + took + "ms");
        }, selectedPreset);

    }

    enum Preset {
        HYPIXEL_LOBBY
    }

    public interface IPreset {
        public Sidebar<CytosisPlayer> getSidebar();
    }

}
