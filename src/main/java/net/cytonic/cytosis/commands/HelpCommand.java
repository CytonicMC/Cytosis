package net.cytonic.cytosis.commands;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;

import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.utils.Msg;

/**
 * The class representing the help command
 */
public class HelpCommand extends CytosisCommand {

    private static final Component COMPONENT = Msg.mm("""
        <dark_green><st>                            </st> <gold>[<aqua>!</aqua>] <aqua><b>CytonicMC</b></aqua> [<aqua>!
        </aqua>]</gold> <st>                           </st>
        
        <white><b>◆</b><gold><hover:show_text:'<gold>Click to join our discord!'>
        <click:open_url:'https://discord.cytonic.net'>Discord</click>
        <white><b>◆</b><gold><hover:show_text:'<gold>Click to open our forums!'>
        <click:open_url:'https://cytonic.net/forums'>Forums</click>
        <white><b>◆</b><gold><hover:show_text:'<gold>Click to go to appeals!'>
        <click:open_url:'https://cytonic.net/forums/topics/PUNISHMENT_APPEALS'>Appeals</click>
        <white><b>◆</b><gold><hover:show_text:'<gold>Click to go to bug reports!'>
        <click:open_url:'https://cytonic.net/forums/topics/BUG_REPORTS'>Bug Reports</click>
        
        <aqua>Don't be afraid to ask a staff member for help either in-game, on our forums, or on discord.</aqua>
        <dark_green><st>                                                                                </st>
        """);

    /**
     * Creates a new command and sets up the consumers and execution logic
     */
    public HelpCommand() {
        super("help", "?");
        setDefaultExecutor((sender, ignored) -> {
            if (!(sender instanceof final Player player)) return;
            player.sendMessage(COMPONENT);
        });
    }
}
