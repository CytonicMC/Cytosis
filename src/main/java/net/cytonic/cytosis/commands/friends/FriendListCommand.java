package net.cytonic.cytosis.commands.friends;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.managers.FriendManager;
import net.cytonic.cytosis.player.CytosisPlayer;

public class FriendListCommand extends CytosisCommand {

    public FriendListCommand() {
        super("list");
        setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof CytosisPlayer player)) return;
            Cytosis.get(FriendManager.class).listFriends(player);
        });
    }

}
