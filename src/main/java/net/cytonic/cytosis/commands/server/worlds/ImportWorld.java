package net.cytonic.cytosis.commands.server.worlds;

import net.cytonic.cytosis.commands.utils.CommandUtils;
import net.cytonic.cytosis.commands.utils.CytosisCommand;

public class ImportWorld extends CytosisCommand {

    public ImportWorld() {
        super("importworld");
        setCondition(CommandUtils.IS_ADMIN);
        addSubcommand(new ImportAnvilWorldCommand());
        addSubcommand(new ImportPolarWorldCommand());
    }
}
