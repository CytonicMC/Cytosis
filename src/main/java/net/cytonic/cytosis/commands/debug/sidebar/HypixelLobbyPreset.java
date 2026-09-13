package net.cytonic.cytosis.commands.debug.sidebar;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.sidebar.Sidebar;
import net.cytonic.cytosis.sidebar.SidebarComponent;
import net.kyori.adventure.text.Component;

public class HypixelLobbyPreset implements PresetCommand.IPreset {

    public static Sidebar<CytosisPlayer> SIDEBAR;
    public static HypixelLobbyPreset PRESET = new HypixelLobbyPreset();

    @Override
    public Sidebar<CytosisPlayer> getSidebar() {
        return SIDEBAR;
    }

    static {
        SidebarComponent<CytosisPlayer> footer = new SidebarComponent.Builder<CytosisPlayer>("FOOTER")
            .line(Component.text("§ehypixel.net"))
            .line(Component.empty())
            .build();


        SidebarComponent<CytosisPlayer> bottomInfo = new SidebarComponent.DynamicBuilder<CytosisPlayer>("BOTTOM_INFO", (_) -> true)
            .line(Component.text("Server ID: §7" + Cytosis.getServer().serverType()))
            .line((player) -> Component.text("Friends: §a" + player.getFriends().size()))
            .line(Component.empty())
            .build();

        SidebarComponent<CytosisPlayer> middleInfo = new SidebarComponent.DynamicBuilder<CytosisPlayer>("MIDDLE_INFO", (_) -> true)
            .line(Component.text("Players Online: §a" + Cytosis.getOnlinePlayers().size()))
            .line(Component.text("Server Version: §a" + Cytosis.getServer().version()))
            .line(Component.empty())
            .build();

        SidebarComponent<CytosisPlayer> topInfo = new SidebarComponent.DynamicBuilder<CytosisPlayer>("TOP_INFO", (_) -> true)
            .line((player) -> Component.text("Level: §3" + player.getLevel()))
            .line((player) -> Component.text("Ping: §e" + player.getLatency()))
            .line((player) -> Component.text("Rank: " + player.getRank().name()))
            .build();

        SIDEBAR = new Sidebar.Builder<CytosisPlayer>((_) -> Component.text("§e§lHYPIXEL"))
            .component(footer)
            .component(bottomInfo)
            .component(middleInfo)
            .component(topInfo)
            .build();
    }

}
