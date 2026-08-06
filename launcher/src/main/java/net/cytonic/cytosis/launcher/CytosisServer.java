package net.cytonic.cytosis.launcher;

import dev.minestomunited.common.config.ConfigRegistry;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.kyori.adventure.key.Key;

import net.cytonic.cytosis.launcher.utils.BuildInfo;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.server.AbstractCytosisServer;
import net.cytonic.cytosis.server.actionBar.ActionBarService;
import net.cytonic.cytosis.server.chat.ChatService;
import net.cytonic.cytosis.server.chat.DefaultChatServiceImpl;
import net.cytonic.cytosis.server.playerList.PlayerListService;
import net.cytonic.cytosis.server.sideboard.SideboardService;

@Getter
@Accessors(fluent = true)
public class CytosisServer extends AbstractCytosisServer<CytosisPlayer> {

    private final ChatService<CytosisPlayer> chatService;
    private final PlayerListService<CytosisPlayer> playerListService;
    private final SideboardService<CytosisPlayer> sideboardService;
    private final ActionBarService<CytosisPlayer> actionBarService;

    protected CytosisServer(ConfigRegistry registry) {
        super(registry, CytosisPlayer::new);
        chatService = new DefaultChatServiceImpl<>();
        playerListService = new PlayerListService.Noop<>();
        sideboardService = new SideboardService.Noop<>();
        actionBarService = new ActionBarService.Noop<>();
    }

    @Override
    public Key serverType() {
        return Key.key("lobby", "lobby");
    }

    @Override
    public String version() {
        return BuildInfo.BUILD_VERSION;
    }

    @Override
    public boolean shouldKickWithoutPolicyAgreement() {
        return false;
    }

    @Override
    public void onShutdown() {
    }
}
