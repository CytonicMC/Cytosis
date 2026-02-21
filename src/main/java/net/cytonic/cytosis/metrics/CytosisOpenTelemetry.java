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
import net.cytonic.cytosis.logging.Logger;

@CytosisComponent
public class CytosisOpenTelemetry implements Bootstrappable {

    private OpenTelemetrySdk sdk;

    private static String getUrl() {
        // these get read directly from env as the file manager isn't started yet.
        String host = null;
        int port = -1;

        if (System.getenv("OTEL_PORT") != null) {
            port = Integer.parseInt(System.getenv("OTEL_PORT"));
        }
        if (System.getenv("OTEL_HOSTNAME") != null) {
            host = System.getenv("OTEL_HOSTNAME");
        }

        if (System.getProperty("OTEL_PORT") != null) {
            port = Integer.parseInt(System.getProperty("OTEL_PORT"));
        }
        if (System.getProperty("OTEL_HOSTNAME") != null) {
            host = System.getProperty("OTEL_HOSTNAME");
        }

        // disable otel support
        if (port == -1 || host == null || host.isEmpty()) {
            throw new IllegalArgumentException(
                String.format("Invalid OTEL connection parameters: [PORT:%d, HOST: '%s']", port, host));
        }

        return "http://" + host + ":" + port;
    }

    @Override
    public void init() {

        String url;
        try {
            url = getUrl();
        } catch (Exception e) {
            Logger.warn("Disabling metrics as the URL was invalid. (%s)", e.getMessage());
            Cytosis.CONTEXT.setMetricsEnabled(false);
            return;
        }

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