package net.cytonic.cytosis.metrics;

import java.lang.management.ManagementFactory;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.events.ServerEventListeners;

import static net.cytonic.cytosis.metrics.Metrics.COMMANDS_EXECUTED;
import static net.cytonic.cytosis.metrics.Metrics.CPU_USAGE;
import static net.cytonic.cytosis.metrics.Metrics.MEMORY_PERCENTAGE;
import static net.cytonic.cytosis.metrics.Metrics.MESSAGES_SENT;
import static net.cytonic.cytosis.metrics.Metrics.NETWORK_PACKETS_IN;
import static net.cytonic.cytosis.metrics.Metrics.NETWORK_PACKETS_OUT;
import static net.cytonic.cytosis.metrics.Metrics.ONLINE_PLAYERS;
import static net.cytonic.cytosis.metrics.Metrics.PLAYER_BANS;
import static net.cytonic.cytosis.metrics.Metrics.PLAYER_JOINS;
import static net.cytonic.cytosis.metrics.Metrics.PLAYER_KICKS;
import static net.cytonic.cytosis.metrics.Metrics.PLAYER_MUTES;
import static net.cytonic.cytosis.metrics.Metrics.POLICIES_ACCEPTED;
import static net.cytonic.cytosis.metrics.Metrics.POLICIES_DECLINED;
import static net.cytonic.cytosis.metrics.Metrics.POLICIES_SHOWN;
import static net.cytonic.cytosis.metrics.Metrics.REPORTS_SUBMITTED;
import static net.cytonic.cytosis.metrics.Metrics.SERVER_MSPT;
import static net.cytonic.cytosis.metrics.Metrics.SERVER_TPS;
import static net.cytonic.cytosis.metrics.Metrics.SNOOPS_SENT;
import static net.cytonic.cytosis.metrics.Metrics.UNIQUE_PLAYERS;
import static net.cytonic.cytosis.metrics.Metrics.UNKNOWN_COMMANDS;
import static net.cytonic.cytosis.metrics.Metrics.USED_MEMORY;

public class MetricsHooks {

    public static void init() {
        MetricsManager mm = Cytosis.get(MetricsManager.class);

        //online players
        mm.createLongGauge(ONLINE_PLAYERS, "Currently online players", "Players",
            _ -> (long) Cytosis.getOnlinePlayers()
                .size(), Attributes.empty());
        mm.createLongCounter(UNIQUE_PLAYERS, "The number of unique players that have played", "players");
        mm.createLongCounter(PLAYER_JOINS, "The number of times players have joined the network", "");
        mm.createLongCounter(PLAYER_KICKS, "The number of players kicked", "players");
        mm.createLongCounter(PLAYER_BANS, "The number of bans issued", "bans");
        mm.createLongCounter(PLAYER_MUTES, "The number of mutes issued", "mutes");

        mm.createDoubleGauge(SERVER_MSPT, "The last tick time", "ms", _ -> ServerEventListeners.RAW_MSPT,
            Attributes.empty());

        mm.createDoubleGauge(SERVER_TPS, "Ticks per second", "tps",
            _ -> Math.min(20, 1000 / ServerEventListeners.RAW_MSPT), Attributes.empty());

        Runtime runtime = Runtime.getRuntime();
        mm.createLongGauge(USED_MEMORY, "Currently used memory", "bytes",
            _ -> runtime.totalMemory() - runtime.freeMemory(), Attributes.empty());

        mm.createDoubleGauge(MEMORY_PERCENTAGE, "Percentage of memory usage", "%",
            _ -> {
                long max = runtime.maxMemory();
                long used = runtime.totalMemory() - runtime.freeMemory();
                return ((double) used / max) * 100.0;
            }, Attributes.empty());

        mm.createDoubleGauge(CPU_USAGE, "Percent usage of the server cpu", "%",
            _ -> ManagementFactory.getOperatingSystemMXBean()
                .getSystemLoadAverage(), Attributes.of(AttributeKey.stringKey("service"), "cytosis"));

        mm.createLongCounter(NETWORK_PACKETS_IN, "The number of packets received", "packets");
        mm.createLongCounter(NETWORK_PACKETS_OUT, "The number of packets sent", "packets");

        mm.createLongCounter(UNKNOWN_COMMANDS, "The number of unknown commands run", "commands");
        mm.createLongCounter(COMMANDS_EXECUTED, "The number of commands executed", "commands");
        mm.createLongCounter(MESSAGES_SENT, "The number of messages sent", "messages");

        // reports
        mm.createLongCounter(REPORTS_SUBMITTED, "The number of reports submitted", "reports");

        mm.createLongCounter(SNOOPS_SENT, "The number of messages sent out via snooper", "snoops");

        mm.createLongCounter(POLICIES_SHOWN, "The number of players who were shown the policy warning screen",
            "players");
        mm.createLongCounter(POLICIES_ACCEPTED, "The number of players who accepted the policies", "players");
        mm.createLongCounter(POLICIES_DECLINED, "The number of players who declined the policies", "players");

    }
}