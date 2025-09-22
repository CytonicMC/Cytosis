package net.cytonic.cytosis.nicknames;

import eu.koboo.minestom.stomui.api.PlayerView;
import eu.koboo.minestom.stomui.api.ViewBuilder;
import eu.koboo.minestom.stomui.api.ViewType;
import eu.koboo.minestom.stomui.api.component.ViewProvider;
import eu.koboo.minestom.stomui.api.interaction.AnvilInputInteraction;
import eu.koboo.minestom.stomui.api.item.ViewItem;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.nicknames.NickSetupCommand;
import net.cytonic.cytosis.utils.Msg;
import net.minestom.server.command.CommandManager;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.nicknames.NickSetupCommand;
import net.cytonic.cytosis.utils.Msg;

public class NicknameEntryMenu extends ViewProvider implements AnvilInputInteraction {

    private String input = "";
    private boolean intentionallyClosed = false;

    public NicknameEntryMenu() {
        super(Cytosis.VIEW_REGISTRY, ViewBuilder.of(ViewType.ANVIL).disableClickTypes(Click.Double.class)
            .title("<black>Enter a nickname"));
    }

    private static boolean checkValid(String input) {
        return input.trim().matches("^[a-zA-Z0-9_]{3,16}$");
    }

    @Override
    public void onClose(@NotNull PlayerView view, @NotNull Player player) {
        if (intentionallyClosed) return;
        player.sendMessage(Msg.whoops("You closed the menu!"));
        open(player);
    }

    @Override
    public void onOpen(@NotNull PlayerView view, @NotNull Player player) {
        view.getBottomInventory().clear();

        ViewItem.bySlot(view, 0).material(Material.PAPER).name("").cancelClicking();

        ViewItem.bySlot(view, 1).material(Material.BARRIER).name("<red>Cancel").interaction(action -> {
            intentionallyClosed = true;
            player.closeInventory();
            player.sendMessage(Msg.redSplash("CANCELLED!", "You cancelled the nickname setup!"));
            Cytosis.CONTEXT.getComponent(CommandManager.class).execute(player, "nick setup skin SKIP");
        });

        ViewItem.bySlot(view, 2).material(Material.NAME_TAG).name(input).interaction(viewAction -> {
            if (!checkValid(input)) {
                player.sendMessage(Msg.whoops("""
                    Your nickname must be between 3 and 16 characters long, and
                    only contain letters, numbers, and underscores."""));
                return;
            }
            intentionallyClosed = true;
            NickSetupCommand.NICKNAME_DATA.computeIfPresent(player.getUuid(), (uuid, data) -> data.withNickname(input));
            player.sendMessage(Msg.goldSplash("UPDATED!", "Updated your nickname to: <gold>" + input + "<gray>!"));
            Cytosis.CONTEXT.getComponent(CommandManager.class).execute(player, "nick setup name SKIP");
            player.closeInventory();
        });
    }

    @Override
    public void onAnvilInput(@NotNull PlayerView playerView, @NotNull Player player, @NotNull String input) {
        this.input = input;
        ViewItem.bySlot(playerView, 2).name(input);
    }

}
