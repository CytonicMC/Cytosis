package net.cytonic.cytosis.commands.utils;

import java.util.Collection;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandExecutor;
import net.minestom.server.command.builder.CommandSyntax;
import net.minestom.server.command.builder.arguments.Argument;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.cytonic.cytosis.utils.Msg;

@Getter
@Setter
public class CytosisCommand extends Command {

    public static final Component COMMAND_UNAVAILABLE = Msg.whoops("This command is not available here!");
    public static final Component COMMAND_DISABLED = Msg.redSplash("DISABLED!", "This command has been disabled.");

    private boolean disabled = false;
    private boolean unavailable = false;
    private Component unavailableMessage = COMMAND_UNAVAILABLE;
    private Component disabledMessage = COMMAND_DISABLED;

    public CytosisCommand(String command, String... aliases) {
        super(command, aliases);
    }

    @Override
    public @NotNull Collection<CommandSyntax> addSyntax(@NotNull CommandExecutor executor,
        @NotNull Argument<?>... args) {
        return super.addSyntax((s, c) -> {
            if (unavailable) {
                s.sendMessage(unavailableMessage);
                return;
            }
            if (checkDisabled(s)) {
                s.sendMessage(disabledMessage);
                return;
            }
            executor.apply(s, c);
        }, args);
    }

    @Override
    public @NotNull Collection<CommandSyntax> addSyntax(@NotNull CommandExecutor executor, @NotNull String format) {
        return super.addSyntax((s, c) -> {
            if (unavailable) {
                s.sendMessage(unavailableMessage);
                return;
            }
            if (checkDisabled(s)) {
                s.sendMessage(disabledMessage);
                return;
            }
            executor.apply(s, c);
        }, format);
    }

    @Override
    public void setDefaultExecutor(@Nullable CommandExecutor executor) {
        super.setDefaultExecutor((s, c) -> {
            if (unavailable) {
                s.sendMessage(unavailableMessage);
                return;
            }
            if (checkDisabled(s)) {
                s.sendMessage(disabledMessage);
                return;
            }

            if (executor != null) {
                executor.apply(s, c);
            }
        });
    }

    private boolean checkDisabled(CommandSender sender) {
        if (!disabled) {
            return false;
        }
        return !CommandUtils.IS_ADMIN.canUse(sender, null);
    }

    /**
     * Disables this command. Admins can still execute the command, though. See {@link #unavailable(Component)} for an
     * unbypassable alternative.
     *
     * @param comp the Component based message to display
     */
    public void disabled(@NotNull Component comp) {
        this.disabled = true;
        disabledMessage = comp;
    }

    /**
     * Disables this command. Admins can still execute the command, though. See {@link #unavailable(String)} for an
     * unbypassable alternative.
     *
     * @param mini the MiniMessage based message to display
     */
    public void disabled(@NotNull String mini) {
        disabled(Msg.mm(mini));
    }

    /**
     * Disables this command. Admins can still execute the command, though. See {@link #unavailable()} for an
     * unbypassable alternative. The default message is used when this command is executed:
     * {@code This command has been disabled.}
     */
    public void disabed() {
        disabled = true;
    }

    /**
     * Makes this command unavailable. Typically used in an environment where this command has no effect/function. This
     * is unbypassable.
     *
     * @param comp the component based message to display
     */
    public void unavailable(@NotNull Component comp) {
        this.unavailable = true;
        unavailableMessage = comp;
    }

    /**
     * Makes this command unavailable. Typically used in an environment where this command has no effect/function. This
     * is unbypassable.
     *
     * @param mini the MiniMessage based message to display
     */
    public void unavailable(@NotNull String mini) {
        unavailable(Msg.mm(mini));
    }

    /**
     * Makes this command unavailable. Typically used in an environment where this command has no effect/function. This
     * is unbypassable. The default message is used when this command is executed:
     * {@code This command is not available here!}
     */
    public void unavailable() {
        unavailable = true;
    }

}
