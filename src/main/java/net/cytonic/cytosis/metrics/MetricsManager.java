package net.cytonic.cytosis.metrics;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleCounter;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.LongHistogram;
import io.opentelemetry.api.metrics.Meter;

import net.cytonic.cytosis.Bootstrappable;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.CytosisContext;
import net.cytonic.cytosis.bootstrap.annotations.CytosisComponent;
import net.cytonic.cytosis.environments.Environment;
import net.cytonic.cytosis.logging.Logger;

/**
 * The base cytosis metrics collecting utility. It supports counters and histograms
 */
@CytosisComponent
@SuppressWarnings("unused")
public class MetricsManager implements Bootstrappable {

    private final Meter meter;
    // counters
    private final Map<String, DoubleCounter> doublesCounters = new ConcurrentHashMap<>();
    private final Map<String, LongCounter> longsCounters = new ConcurrentHashMap<>();
    // histograms
    private final Map<String, DoubleHistogram> doubleHistograms = new ConcurrentHashMap<>();
    private final Map<String, LongHistogram> longHistograms = new ConcurrentHashMap<>();
    private CytosisContext cytosisContext;

    /*
     * Here are the types of things for future reference:
     * Counter: A monotonically increasing value (Can only increase). # of unique players, logins today, etc.
     * Gauge: A value that can increase or decrease over time. memory usage, tick time, current player count
     * Histogram: The distribution of events into "buckets", like ping. There would be a bucket for <100, 100-250,
     * 250-500, >500 upDown counter is like counter, but it can go up or down. It stores whole numbers, not floating
     * point or double like gauge. May or may not be implemented, but it's not super useful
     */

    /**
     * Creates a new Metrics Manager following a "cytosis" meter
     */
    public MetricsManager() {
        meter = CytosisOpenTelemetry.getMeter("cytosis");
    }

    /**
     * Creates a new Metrics manager with the specified meter
     *
     * @param meter the meter to follow
     */
    public MetricsManager(Meter meter) {
        this.meter = meter;
    }

    /**
     * Creates a new metrics manager with the meter identified by the given string
     *
     * @param name The name to identify the meter by
     */
    public MetricsManager(String name) {
        this.meter = CytosisOpenTelemetry.getMeter(name);
    }

    @Override
    public void init() {
        this.cytosisContext = Cytosis.CONTEXT;

        if (!cytosisContext.getFlags().contains("--no-metrics")) {
            CytosisOpenTelemetry.setup();
        } else Logger.info("Skipping metric sending due to the `--no-metrics` flag.");
    }

    /**
     * Creates an **increase** only counter of whole numbers
     *
     * @param counterName the name of the counter
     * @param description the description of it
     * @param unit        the unit counted
     */
    public void createLongCounter(String counterName, String description, String unit) {
        validateState(counterName);
        longsCounters.put(counterName,
            meter.counterBuilder(counterName).setDescription(description).setUnit(unit).build());
    }

    private void validateState(String name) {
        if (!cytosisContext.isMetricsEnabled()) {
            throw new IllegalStateException("Metrics collection has not been enabled!");
        }
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Metric name cannot be null or empty");
        }
        if (!name.matches("[a-z_][a-z0-9_.]*")) {
            throw new IllegalArgumentException("Metric name must match pattern [a-z_][a-z0-9_]*");
        }
    }

    /**
     * Creates an **increase** only counter of doubles
     *
     * @param counterName the name of the counter
     * @param description the description of it
     * @param unit        the unit counted
     */
    public void createDoubleCounter(String counterName, String description, String unit) {
        validateState(counterName);
        doublesCounters.put(counterName,
            meter.counterBuilder(counterName).setDescription(description).setUnit(unit).ofDoubles().build());
    }

    /**
     * Add some value to a whole number counter
     *
     * @param counterName the counter to add to
     * @param value       the ** @param extraAttributes any additional attributes that should be recorded. Use
     *                    {@link Attributes#empty()} if no additional attributes are desired. Do not include the server
     *                    id, as it is already included by default.
     */
    public void addToLongCounter(String counterName, long value, Attributes extraAttributes) {
        validateState(counterName);
        if (value <= 0) {
            return; // no negative values, adding 0 does nothing
        }
        if (!longsCounters.containsKey(counterName)) return;
        longsCounters.get(counterName).add(value,
            Attributes.builder().putAll(extraAttributes)
                .put(AttributeKey.stringKey("server_id"), Cytosis.CONTEXT.SERVER_ID)
                .put(AttributeKey.stringKey("server_type"), cytosisContext.getServerGroup().humanReadable())
                .put(AttributeKey.stringKey("environment"), Cytosis.get(Environment.class).name().toLowerCase())
                .build());
    }

    /**
     * Add some value to a double/floating point number counter
     *
     * @param counterName     the counter to add to
     * @param value           the value to add
     * @param extraAttributes any additional attributes that should be recorded. Use {@link Attributes#empty()} if no
     *                        additional attributes are desired. Do not include the server id, as it is already included
     *                        by default.
     */
    public void addToDoubleCounter(String counterName, double value, Attributes extraAttributes) {
        validateState(counterName);
        if (value <= 0) {
            return; // no negative values
        }
        if (!doublesCounters.containsKey(counterName)) return;
        doublesCounters.get(counterName).add(value,
            Attributes.builder().putAll(extraAttributes)
                .put(AttributeKey.stringKey("server_id"), Cytosis.CONTEXT.SERVER_ID)
                .put(AttributeKey.stringKey("server_type"), cytosisContext.getServerGroup().humanReadable())
                .put(AttributeKey.stringKey("environment"), Cytosis.get(Environment.class).name().toLowerCase())
                .build());
    }

    // guages

    /**
     * Creates a gauge that measures a double value
     *
     * @param gaugeName       The name of the gauge
     * @param description     the description of the gauge
     * @param unit            the unit the guage measures
     * @param function        the function defining the value of the gauge. The function should not be blocking, and
     *                        threadsafe.
     * @param extraAttributes any additional attributes that should be recorded. Use {@link Attributes#empty()} if no
     *                        additional attributes are desired. Do not include the server id, as it is already included
     *                        by default.
     */
    public void createDoubleGauge(String gaugeName, String description, String unit, Function<Void, Double> function,
        Attributes extraAttributes) {
        validateState(gaugeName);
        meter.gaugeBuilder(gaugeName).setDescription(description).setUnit(unit).buildWithCallback(
            observableDoubleMeasurement -> observableDoubleMeasurement.record(function.apply(null),
                Attributes.builder().putAll(extraAttributes)
                    .put(AttributeKey.stringKey("server_id"), Cytosis.CONTEXT.SERVER_ID)
                    .put(AttributeKey.stringKey("server_type"), cytosisContext.getServerGroup().humanReadable())
                    .put(AttributeKey.stringKey("environment"), Cytosis.get(Environment.class).name().toLowerCase())
                    .build()));
    }

    /**
     * Creates a gauge that measures a Long value
     *
     * @param gaugeName       The name of the gauge
     * @param description     the description of the gauge
     * @param unit            the unit the guage measures
     * @param function        the function defining the value of the gauge. The function should not be blocking, and
     *                        threadsafe.
     * @param extraAttributes any additional attributes that should be recorded. Use {@link Attributes#empty()} if no
     *                        additional attributes are desired. Do not include the server id, as it is already included
     *                        by default.
     */
    public void createLongGauge(String gaugeName, String description, String unit, Function<Void, Long> function,
        Attributes extraAttributes) {
        validateState(gaugeName);
        meter.gaugeBuilder(gaugeName).setDescription(description).setUnit(unit).ofLongs().buildWithCallback(
            call -> call.record(function.apply(null),
                Attributes.builder().putAll(extraAttributes)
                    .put(AttributeKey.stringKey("server_id"), Cytosis.CONTEXT.SERVER_ID)
                    .put(AttributeKey.stringKey("server_type"), cytosisContext.getServerGroup().humanReadable())
                    .put(AttributeKey.stringKey("environment"), Cytosis.get(Environment.class).name().toLowerCase())
                    .build()));
    }

    // histograms

    /**
     * Creates a histogram that stores double values
     *
     * @param histogramName the name of the histogram
     * @param description   the description of this histogram
     * @param unit          the unit this histogram collects
     */
    public void createDoubleHistogram(String histogramName, String description, String unit) {
        validateState(histogramName);
        doubleHistograms.put(histogramName,
            meter.histogramBuilder(histogramName).setDescription(description).setUnit(unit).build());
    }

    /**
     * Creates a histogram that stores long values
     *
     * @param histogramName the name of the histogram
     * @param description   the description of this histogram
     * @param unit          the unit this histogram collects
     */
    public void createLongHistogram(String histogramName, String description, String unit) {
        validateState(histogramName);
        longHistograms.put(histogramName,
            meter.histogramBuilder(histogramName).setDescription(description).setUnit(unit).ofLongs().build());
    }

    /**
     * Records a double value in the specified histogram. This method has no effect if the histogram doesn't exist or
     * the value is negative.
     *
     * @param histogram       the histogram to record the value in
     * @param value           the positive value to record
     * @param extraAttributes any additional attributes that should be recorded. Use {@link Attributes#empty()} if no
     *                        additional attributes are desired. Do not include the server id, as it is already included
     *                        by default.
     */
    public void recordDouble(String histogram, double value, Attributes extraAttributes) {
        validateState(histogram);
        if (value < 0) return;
        if (!doubleHistograms.containsKey(histogram)) return;
        doubleHistograms.get(histogram).record(value,
            Attributes.builder().putAll(extraAttributes)
                .put(AttributeKey.stringKey("server_id"), Cytosis.CONTEXT.SERVER_ID)
                .put(AttributeKey.stringKey("server_type"), cytosisContext.getServerGroup().humanReadable())
                .put(AttributeKey.stringKey("environment"), Cytosis.get(Environment.class).name().toLowerCase())
                .build());
    }

    /**
     * Records a long value in the specified histogram. This method has no effect if the histogram doesn't exist or the
     * value is negative.
     *
     * @param histogram       the histogram to record the value in
     * @param value           the positive value to record
     * @param extraAttributes any additional attributes that should be recorded. Use {@link Attributes#empty()} if no
     *                        additional attributes are desired. Do not include the server id, as it is already included
     *                        by default.
     */
    public void recordLong(String histogram, long value, Attributes extraAttributes) {
        validateState(histogram);
        if (value < 0) return;
        if (!longHistograms.containsKey(histogram)) return;
        longHistograms.get(histogram).record(value,
            Attributes.builder().putAll(extraAttributes)
                .put(AttributeKey.stringKey("server_id"), Cytosis.CONTEXT.SERVER_ID)
                .put(AttributeKey.stringKey("server_type"), cytosisContext.getServerGroup().humanReadable())
                .put(AttributeKey.stringKey("environment"), Cytosis.get(Environment.class).name().toLowerCase())
                .build());
    }
}