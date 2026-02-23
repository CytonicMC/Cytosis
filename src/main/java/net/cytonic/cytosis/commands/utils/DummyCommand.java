package net.cytonic.cytosis.commands.utils;

/**
 * A command that does nothing. Right now it is used to close Book Menus.
 */
public class DummyCommand extends CytosisCommand {

    public DummyCommand() {
        super("dummy");
        setDefaultExecutor((_, _) -> {
        });
    }
}
