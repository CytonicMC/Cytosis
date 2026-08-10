package net.cytonic.cytosis.commands.friends;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.commands.utils.SubCommand;
import net.cytonic.cytosis.managers.FriendManager;
import net.cytonic.cytosis.player.CytosisPlayer;

@SubCommand
class ListCommand extends CytosisCommand {

    ListCommand() {
        super("list");
        setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof CytosisPlayer player)) return;
            Cytosis.get(FriendManager.class).listFriends(player);
        });
    }
}
