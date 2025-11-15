package net.cytonic.cytosis.nicknames;

import me.devnatan.AnvilInput;
import me.devnatan.inventoryframework.View;
import me.devnatan.inventoryframework.ViewConfigBuilder;
import me.devnatan.inventoryframework.ViewType;
import me.devnatan.inventoryframework.context.CloseContext;
import me.devnatan.inventoryframework.context.RenderContext;
import me.devnatan.inventoryframework.state.MutableState;
import net.minestom.server.command.CommandManager;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.nicknames.NickSetupCommand;
import net.cytonic.cytosis.utils.Msg;

public class NicknameEntryMenu extends View {

    private final AnvilInput anvilInput = AnvilInput.createAnvilInput();
    private final MutableState<Boolean> intentionallyClosed = mutableState(false);

    @Override
    public void onInit(@NotNull ViewConfigBuilder config) {
        config.title(Msg.black("Enter a nickname"));
        config.type(ViewType.ANVIL);
        config.cancelInteractions();
        config.use(anvilInput);
    }

    private static boolean checkValid(String input) {
        return input.trim().matches("^[a-zA-Z0-9_]{3,16}$");
    }

    @Override
    public void onClose(@NotNull CloseContext context) {
        if (intentionallyClosed.get(context)) return;
        context.getPlayer().sendMessage(Msg.whoops("You closed the menu!"));
        context.setCancelled(true);
    }

    @Override
    public void onFirstRender(@NotNull RenderContext context) {
        context.getPlayer().getInventory().clear();

        context.slot(2).onClick(slotClickContext -> {
            String input = anvilInput.get(slotClickContext);
            if (!checkValid(input)) {
                slotClickContext.getPlayer().sendMessage(Msg.whoops("""
                    Your nickname must be between 3 and 16 characters long, and\s
                    only contain letters, numbers, and underscores."""));
                return;
            }
            intentionallyClosed.set(true, slotClickContext);
            Player player = slotClickContext.getPlayer();
            NickSetupCommand.NICKNAME_DATA.computeIfPresent(player.getUuid(), (uuid, data) -> data.withNickname(input));
            player.sendMessage(Msg.goldSplash("UPDATED!", "Updated your nickname to: <gold>%s<gray>!", input));
            Cytosis.CONTEXT.getComponent(CommandManager.class).execute(player, "nick setup name SKIP");
            player.closeInventory();
        });
    }
}
