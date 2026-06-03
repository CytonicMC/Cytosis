package net.cytonic.cytosis.metrics;

import java.time.Duration;

import io.opentelemetry.api.logs.LoggerProvider;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogRecordExporter;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;

import net.cytonic.cytosis.Bootstrappable;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.bootstrap.annotations.CytosisComponent;
import net.cytonic.cytosis.config.CytosisConfig;
import net.cytonic.cytosis.config.CytosisConfig.MetricsConfig;
import net.cytonic.cytosis.logging.Logger;

@CytosisComponent
public class CytosisOpenTelemetry implements Bootstrappable {

    private OpenTelemetrySdk sdk;


    @Override
    public void init() {
        MetricsConfig config = Cytosis.getServer().getConfigOrThrow(CytosisConfig.class).metrics();
        if (!config.enabled()) return;

        String url = "http://" + config.host() + ":" + config.port();

        // Configure Span Exporter (for traces)
        OtlpGrpcSpanExporter spanExporter = OtlpGrpcSpanExporter.builder().setEndpoint(url)  // OTLP endpoint
            .build();

        // Configure Tracer Provider (for traces)
        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
            .addSpanProcessor(BatchSpanProcessor.builder(spanExporter)
                .build()).build();

        // Configure Metric Exporter (for metrics)
        OtlpGrpcMetricExporter metricExporter = OtlpGrpcMetricExporter.builder().setEndpoint(url)  // OTLP endpoint
            .build();

        // Configure Meter Provider (for metrics)
        SdkMeterProvider meterProvider = SdkMeterProvider.builder().registerMetricReader(PeriodicMetricReader
            .builder(metricExporter)
            .setInterval(Duration.ofSeconds(5))
            .build()
        ).build();

        // Configure Log Exporter (for logs)
        OtlpGrpcLogRecordExporter logExporter = OtlpGrpcLogRecordExporter.builder().setEndpoint(url)  // OTLP endpoint
            .build();

        // Configure Logger Provider (for logs)
        SdkLoggerProvider loggerProvider = SdkLoggerProvider.builder()
            .addLogRecordProcessor(BatchLogRecordProcessor.builder(logExporter).build()).build();

        // Build and register the OpenTelemetry SDK
        sdk = OpenTelemetrySdk.builder()
            .setTracerProvider(tracerProvider)
            .setMeterProvider(meterProvider)
            .setLoggerProvider(loggerProvider)
            .build();

        Cytosis.CONTEXT.setMetricsEnabled(true);
        Logger.info("Metrics collection has been enabled!");
    }

    public Tracer getTracer(String name) {
        if (sdk == null) throw new IllegalStateException("OTel is not yet set up!");
        return sdk.getTracer(name);
    }

    public Meter getMeter(String name) {
        if (sdk == null) throw new IllegalStateException("OTel is not yet set up!");
        return sdk.getMeter(name);
    }

    public LoggerProvider getLogger(String name) {
        if (sdk == null) throw new IllegalStateException("OTel is not yet set up!");
        return sdk.getLogsBridge();
    }
}