package net.cytonic.cytosis.metrics;

import java.lang.management.ManagementFactory;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.events.ServerEventListeners;

public class MetricsHooks {

    public static void init() {
        MetricsManager mm = Cytosis.get(MetricsManager.class);

        //online players
        mm.createLongGauge("players.online", "Currently online players", "Players",
            unused -> (long) Cytosis.getOnlinePlayers()
                .size(), Attributes.empty());

        mm.createDoubleGauge("server.mspt", "The last tick time", "ms", unused -> ServerEventListeners.RAW_MSPT,
            Attributes.empty());

        mm.createDoubleGauge("server.tps", "Ticks per second", "tps",
            unused -> Math.min(20, 1000 / ServerEventListeners.RAW_MSPT), Attributes.empty());

        Runtime runtime = Runtime.getRuntime();
        mm.createLongGauge("memory.used", "Currently used memory", "bytes",
            unused -> runtime.totalMemory() - runtime.freeMemory(), Attributes.empty());

        mm.createDoubleGauge("memory.percentage", "Percentage of memory usage", "%",
            unused -> ((double) runtime.freeMemory() / runtime.totalMemory()) * 100.0, Attributes.empty());

        mm.createDoubleGauge("cpu.usage", "Percent usage of the server cpu", "%",
            unused -> ManagementFactory.getOperatingSystemMXBean()
                .getSystemLoadAverage(), Attributes.of(AttributeKey.stringKey("service"), "cytosis"));

        mm.createLongCounter("players.unique", "The number of unique players that have played", "players");
    }
}