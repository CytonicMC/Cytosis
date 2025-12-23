package net.cytonic.cytosis.commands.server.whitelist;

import java.io.IOException;
import java.util.UUID;

import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.utils.mojang.MojangUtils;
import org.jetbrains.annotations.NotNull;

import net.cytonic.cytosis.CytonicNetwork;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CommandUtils;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.config.CytosisSnoops;
import net.cytonic.cytosis.data.RedisDatabase;
import net.cytonic.cytosis.data.enums.PlayerRank;
import net.cytonic.cytosis.environments.Environment;
import net.cytonic.cytosis.environments.EnvironmentManager;
import net.cytonic.cytosis.managers.SnooperManager;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.cytosis.utils.Utils;

public class GrantCommand extends CytosisCommand {

    public GrantCommand() {
        super("grant");
        Environment env = Cytosis.get(EnvironmentManager.class).getEnvironment();
        setCondition(CommandUtils.IS_STAFF);
        setDefaultExecutor((sender, _) -> sender.sendMessage(Msg.whoops("You must specify a player!")));

        ArgumentWord playerArg = ArgumentType.Word("player");
        ArgumentEnum<@NotNull Environment> environmentArg = ArgumentType.Enum("env", Environment.class)
            .setFormat(ArgumentEnum.Format.LOWER_CASED);
        environmentArg.setDefaultValue(env);
        addSyntax((sender, context) -> {
            if (!(sender instanceof CytosisPlayer player)) return;
            String rawPlayer = context.get(playerArg);
            Environment environment = context.get(environmentArg);
            if (environment == Environment.PRODUCTION) {
                player.sendMessage(Msg.whoops("There is no applicable whitelist for the Production Network!"));
                return;
            }
            try {
                UUID target = UUID.fromString(rawPlayer);
                addWhiteListSync(target, rawPlayer, player, environment);
            } catch (IllegalArgumentException ex) {
                Thread.ofVirtual().start(() -> {
                    try {
                        addWhiteList(MojangUtils.getUUID(rawPlayer), rawPlayer, player, environment);
                    } catch (IOException e) {
                        sender.sendMessage(Msg.whoops("Failed to resolve player '%s'.", rawPlayer));
                    }
                });
            }
        }, playerArg, environmentArg);
    }

    private void addWhiteListSync(UUID uuid, String msg, CytosisPlayer player, Environment environment) {
        Thread.ofVirtual().start(() -> addWhiteList(uuid, msg, player, environment));
    }

    private void addWhiteList(UUID uuid, String msg, CytosisPlayer player, Environment environment) {
        PlayerRank rank = Cytosis.get(CytonicNetwork.class).getCachedPlayerRanks().get(uuid);
        if (rank != null && rank.isStaff()) {
            player.sendMessage(Msg.whoops("'%s' already bypasses the whitelist!", msg));
            return;
        }
        RedisDatabase redis = Cytosis.get(RedisDatabase.class);
        if (redis.getSet("player_whitelist", environment).contains(uuid.toString())) {
            player.sendMessage(Msg.whoops("'%s' is already whitelisted on the %s Network!", msg,
                Utils.captializeFirstLetters(environment.name().toLowerCase())));
            return;
        }
        redis.addToSet("player_whitelist", uuid.toString(), environment);
        Cytosis.get(SnooperManager.class).sendSnoop(CytosisSnoops.PLAYER_WHITELIST,
            Msg.snoop(player.trueFormattedName().append(Msg.grey(
                " Added %s to the whitelist! <yellow><b><click:copy_to_clipboard:%s>[UUID]", msg, uuid.toString()))));
        player.sendMessage(Msg.success("Successfully added '%s' to the whitelist!", msg));
    }
}
