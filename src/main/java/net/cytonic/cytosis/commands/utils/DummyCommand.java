package net.cytonic.cytosis.commands.utils;

import net.cytonic.protocol.utils.ExcludeFromIndex;

/**
 * A command that does nothing. Right now it is used to close Book Menus.
 */
@ExcludeFromIndex
public class DummyCommand extends CytosisCommand {

    public static final DummyCommand INSTANCE = new DummyCommand();

    private DummyCommand() {
        super("dummy");
    }
}
