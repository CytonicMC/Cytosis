package net.cytonic.cytosis.commands.nicknames;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CommandUtils;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.data.enums.PlayerRank;
import net.cytonic.cytosis.nicknames.NicknameEntryMenu;
import net.cytonic.cytosis.nicknames.NicknameGenerator;
import net.cytonic.cytosis.nicknames.NicknameManager;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;
import net.kyori.adventure.inventory.Book;
import net.minestom.server.command.builder.arguments.ArgumentWord;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class NickSetupCommand extends CytosisCommand {

    public static final ConcurrentHashMap<UUID, NicknameManager.NicknameData> NICKNAME_DATA = new ConcurrentHashMap<>();

    private static final Book WARNING_BOOK = Book.builder()
            .author(Msg.aqua("CytonicMC Team"))
            .title(Msg.gold("Acknowledgements!"))
            .pages(Msg.mm("""
                         <red>⚠ WARNING! ⚠</red>
                    
                    <black>All rules still apply. You can still be reported and all nickname history is stored. Staff can still see your true identity.</black>
                    
                    <click:run_command:'/nick setup agree'><underlined>» I understand. Set up my nickname.</underlined></click>
                    """))
            .build();

    private static final Book RANK_BOOK = Book.builder()
            .author(Msg.aqua("CytonicMC Team"))
            .title(Msg.gold("Nickname Rank"))
            .pages(Msg.mm("""
                    <black>First, you'll need to pick which <b><#F5C526>RANK</#F5C526></b> you would like to have while nicked. <hover:show_text:'<gray><i>You will retain the permissions of your current rank, except when using those features would reveal your nickname status. (ie. /fly)'><gray><i>(note)</hover></black>
                    
                    <click:run_command:'/nick setup rank DEFAULT'>» <gray>DEFAULT</click>
                    <click:run_command:'/nick setup rank NOBLE'>» <dark_purple>[NOBLE]</click>
                    <click:run_command:'/nick setup rank VALIENT'>» <dark_green>[VALIENT]</click>
                    <click:run_command:'/nick setup rank MASTER'>» <dark_red>[MASTER]</click>
                    <click:run_command:'/nick setup rank CELESTIAL'>» <dark_aqua>[CELESTIAL]</click>
                    """))
            .build();

    private static final Book SKIN_BOOK = Book.builder()
            .author(Msg.aqua("CytonicMC Team"))
            .title(Msg.gold("Nickname Skin"))
            .pages(Msg.mm("""
                    <black>Great! Now, which <b><#F5C526>SKIN</#F5C526></b> you would like to use while nicked? <hover:show_text:'<gray><i>Your skin will appear to be your real skin, but the skin you chose will be shown to other players.'><gray><i>(note)</hover></black>
                    
                    <click:run_command:'/nick setup skin REAL'>» My normal skin</click>
                    <click:run_command:'/nick setup skin DEFAULT'>» Steve/Alex skin</click>
                    <click:run_command:'/nick setup skin RANDOM'>» Random Skin</click>
                    """))
            .build();

    private static final Book NAME_BOOK = Book.builder()
            .author(Msg.aqua("CytonicMC Team"))
            .title(Msg.gold("Nickname Skin"))
            .pages(Msg.mm("""
                    <black>Finally, you'll need to pick out the <b><#F5C526>NAME</#F5C526></b> use! </black>
                    
                    <click:run_command:'/nick setup name RANDOM'>» Use a random name</click>
                    <click:run_command:'/nick setup name SET'>» Enter your name</click>
                    
                    <black>You can go back to your usual self using the <b><#F5C526>/nick reset</#F5C526></b> command.</black>
                    """))
            .build();
    private static final Set<String> SKIN_OPTIONS = Set.of("DEFAULT", "REAL", "RANDOM");
    private static final Set<String> NAME_OPTIONS = Set.of("RANDOM", "SET", "SKIP");

    public NickSetupCommand() {
        super("setup");
        setCondition(CommandUtils.IS_STAFF);
        setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof CytosisPlayer player)) return;
            if (player.isNicked()) {
                player.sendMessage(Msg.whoops("You are already nicked! Use /nick reset to go back to your normal self and try again."));
                return;
            }
            player.openBook(WARNING_BOOK);
        });
        ArgumentWord verb = new ArgumentWord("verb").from("agree", "done", "rank", "skin", "name");
        ArgumentWord verbArg = new ArgumentWord("verbArg").from("DEFAULT", "NOBLE", "VALIENT", "MASTER", "CELESTIAL", "REAL", "RANDOM", "SET", "SKIP");
        verbArg.setDefaultValue("not set");
        addSyntax((sender, context) -> {
            if (!(sender instanceof CytosisPlayer player)) return;
            String arg = context.get(verbArg);
            switch (context.get(verb).toLowerCase()) {
                case "agree" -> {
                    NICKNAME_DATA.put(player.getUuid(), NicknameManager.NicknameData.EMPTY);
                    player.openBook(RANK_BOOK);
                }
                case "done" -> {
                    if (!NICKNAME_DATA.containsKey(player.getUuid())) {
                        sender.sendMessage(Msg.whoops("You must agree to the terms before you can set up your nickname! Use `/nick setup` to start the process!"));
                        return;
                    }

                    NicknameManager.NicknameData data = NICKNAME_DATA.get(player.getUuid());
                    if (data.nickname().isBlank()) {
                        sender.sendMessage(Msg.whoops("Invalid nickname!"));
                        return;
                    }
                    Cytosis.getNicknameManager().nicknamePlayer(player.getUuid(), data);
                    player.sendMessage(Msg.goldSplash("DISGUISED!", "Your apparent name, rank, and skin have been changed. To go back to your normal self, use the <#BE9025>/nick reset</#BE9025> command."));

                }
                case "rank" -> {
                    if (!NICKNAME_DATA.containsKey(player.getUuid())) {
                        sender.sendMessage(Msg.whoops("You must agree to the terms before you can set up your nickname! Use `/nick setup` to start the process!"));
                        return;
                    }
                    if (arg.equals("not set")) {
                        sender.sendMessage(Msg.whoops("You must specify a rank!"));
                        return;
                    }
                    PlayerRank rank;
                    try {
                        rank = PlayerRank.valueOf(arg.toUpperCase());
                    } catch (IllegalArgumentException ignored) {
                        sender.sendMessage(Msg.whoops("Invalid rank '%s'!", arg));
                        return;
                    }
                    NICKNAME_DATA.computeIfPresent(player.getUuid(), (k, data) -> data.withRank(rank));
                    player.openBook(SKIN_BOOK);
                }
                case "skin" -> {
                    if (!NICKNAME_DATA.containsKey(player.getUuid())) {
                        sender.sendMessage(Msg.whoops("You must agree to the terms before you can set up your nickname! Use `/nick setup` to start the process!"));
                        return;
                    }
                    if (arg.equals("not set")) {
                        sender.sendMessage(Msg.whoops("You must specify a skin option!"));
                        return;
                    }

                    if (arg.equals("SKIP")) {
                        player.openBook(NAME_BOOK); // we can do this since if it's null, it's just a steve skin
                        return;
                    }

                    if (!SKIN_OPTIONS.contains(arg.toUpperCase())) {
                        sender.sendMessage(Msg.whoops("Invalid skin option '%s'!", arg));
                        return;
                    }
                    switch (arg.toUpperCase()) {
                        case "DEFAULT" ->
                                NICKNAME_DATA.computeIfPresent(player.getUuid(), (k, data) -> data.withSkin(null, null));
                        case "REAL" ->
                                NICKNAME_DATA.computeIfPresent(player.getUuid(), (k, data) -> data.withSkin(player.getSkin().signature(), player.getSkin().textures()));
                        case "RANDOM" ->
                                NICKNAME_DATA.computeIfPresent(player.getUuid(), (k, data) -> data.withSkin(NicknameGenerator.generateSkin()));
                    }
                    player.openBook(NAME_BOOK);
                }
                case "name" -> {
                    if (!NICKNAME_DATA.containsKey(player.getUuid())) {
                        sender.sendMessage(Msg.whoops("You must agree to the terms before you can set up your nickname! Use `/nick setup` to start the process!"));
                        return;
                    }
                    if (arg.equals("not set")) {
                        sender.sendMessage(Msg.whoops("You must specify a name option!"));
                        return;
                    }

                    if (!NAME_OPTIONS.contains(arg.toUpperCase())) {
                        sender.sendMessage(Msg.whoops("Invalid name option '%s'!", arg));
                        return;
                    }
                    switch (arg.toUpperCase()) {
                        case "SET" -> {
                            new NicknameEntryMenu().open(player);
                            return;
                        }
                        case "SKIP" -> {
                            if (NICKNAME_DATA.get(player.getUuid()).nickname() == null || NICKNAME_DATA.get(player.getUuid()).nickname().isBlank()) {
                                sender.sendMessage(Msg.whoops("You must set a nickname!"));
                                return;
                            }
                        }
                        case "RANDOM" ->
                                NICKNAME_DATA.computeIfPresent(player.getUuid(), (k, data) -> data.withNickname(NicknameGenerator.generateUsername()));
                    }
                    player.openBook(getConfirmBook(player, NICKNAME_DATA.get(player.getUuid())));
                }
            }
        }, verb, verbArg);
    }

    private static String translateRank(PlayerRank rank) {
        return switch (rank) {
            case NOBLE -> "<dark_purple>[NOBLE]";
            case VALIENT -> "<dark_green>[VALIENT]";
            case MASTER -> "<dark_red>[MASTER]";
            case CELESTIAL -> "<dark_aqua>[CELESTIAL]";
            default -> "<gray>DEFAULT";
        };
    }

    private static Book getConfirmBook(CytosisPlayer player, NicknameManager.NicknameData data) {
        return Book.builder()
                .author(Msg.aqua("CytonicMC Team"))
                .title(Msg.gold("Confirm Details"))
                .pages(Msg.mm("""
                        <black>Awesome! Your nickname is as follows! </black>
                        
                        <black>Name: '%s'</black>
                        <black>Rank: %s</black>
                        <black>Skin: %s</black>
                        
                        <click:run_command:'/nick setup skin SKIP'>» Change Name</click>
                        <click:run_command:'/nick setup'>» Start Over</click>
                        
                        <click:run_command:'/nick setup done'>» <b><dark_green>CONFIRM</dark_green></b></click>
                        """, data.nickname(), translateRank(data.rank()), NicknameManager.translateSkin(player, data.value())))
                .build();
    }
}
