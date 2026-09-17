package net.cytonic.cytosis.commands.debug.sidebar;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.sidebar.Sidebar;
import net.cytonic.cytosis.sidebar.SidebarComponent;
import net.cytonic.cytosis.utils.Msg;
import net.kyori.adventure.text.Component;

public class HypixelLobbyPreset implements PresetCommand.IPreset {

    public static final Sidebar<CytosisPlayer> SIDEBAR;
    public static final HypixelLobbyPreset PRESET = new HypixelLobbyPreset();

    @Override
    public Sidebar<CytosisPlayer> getSidebar() {
        return SIDEBAR;
    }

    static {
        SidebarComponent<CytosisPlayer> footer = new SidebarComponent.Builder<CytosisPlayer>("FOOTER")
            .line(Msg.yellow("hypixel.net"))
            .line(Component.empty())
            .build();

        SidebarComponent<CytosisPlayer> bottomInfo = new SidebarComponent.DynamicBuilder<CytosisPlayer>("BOTTOM_INFO", (_) -> true)
            .line(Msg.mm("Server ID: <gray>" + Cytosis.getServer().serverType()))
            .line((player) -> Msg.mm("Friends: <green>" + player.getFriends().size()))
            .line(Component.empty())
            .build();

        SidebarComponent<CytosisPlayer> middleInfo = new SidebarComponent.DynamicBuilder<CytosisPlayer>("MIDDLE_INFO", (_) -> true)
            .line(Msg.mm("Players Online: <green>" + Cytosis.getOnlinePlayers().size()))
            .line(Msg.mm("Server Version: <green>" + Cytosis.getServer().version()))
            .line(Component.empty())
            .build();

        SidebarComponent<CytosisPlayer> topInfo = new SidebarComponent.DynamicBuilder<CytosisPlayer>("TOP_INFO", (_) -> true)
            .line((player) -> Msg.mm("Level: <aqua>" + player.getLevel()))
            .line((player) -> Msg.mm("Ping: <yellow>" + player.getLatency()))
            .line((player) -> Component.text("Rank: " + player.getRank().name()))
            .build();

        SIDEBAR = new Sidebar.Builder<CytosisPlayer>((_) -> Msg.mm("<yellow><bold>HYPIXEL.NET"))
            .component(footer)
            .component(bottomInfo)
            .component(middleInfo)
            .component(topInfo)
            .build();
    }

}
