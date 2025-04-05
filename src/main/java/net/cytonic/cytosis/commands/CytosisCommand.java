package net.cytonic.cytosis.commands;

import lombok.Getter;
import lombok.Setter;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandExecutor;
import net.minestom.server.command.builder.CommandSyntax;
import net.minestom.server.command.builder.arguments.Argument;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

@Getter
@Setter
public class CytosisCommand extends Command {

    private boolean disabled;

    public CytosisCommand(String command, String... aliases) {
        super(command, aliases);
    }

    private boolean checkDisabled(CommandSender sender) {
        if (!disabled) return false;
        return !CommandUtils.IS_ADMIN.canUse(sender, null);
    }

    @Override
    public @NotNull Collection<CommandSyntax> addSyntax(@NotNull CommandExecutor executor, @NotNull Argument<?>... args) {
        return super.addSyntax(((s, c) -> {
            if (checkDisabled(s)) {
                s.sendMessage(CommandUtils.COMMAND_DISABLED);
                return;
            }
            executor.apply(s, c);
        }), args);
    }

    @Override
    public @NotNull Collection<CommandSyntax> addSyntax(@NotNull CommandExecutor executor, @NotNull String format) {
        return super.addSyntax(((s, c) -> {
            if (checkDisabled(s)) {
                s.sendMessage(CommandUtils.COMMAND_DISABLED);
                return;
            }
            executor.apply(s, c);
        }), format);
    }

    @Override
    public void setDefaultExecutor(@Nullable CommandExecutor executor) {
        super.setDefaultExecutor((s, c) -> {
            if (checkDisabled(s)) {
                s.sendMessage(CommandUtils.COMMAND_DISABLED);
                return;
            }

            if (executor != null) {
                executor.apply(s, c);
            }
        });
    }
}
