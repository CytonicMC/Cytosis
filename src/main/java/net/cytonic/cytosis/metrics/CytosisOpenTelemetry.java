package net.cytonic.cytosis.metrics;

import io.opentelemetry.api.GlobalOpenTelemetry;
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
import net.cytonic.cytosis.Cytosis;


public class CytosisOpenTelemetry {

    public static void setup() {

        String url;
        try {
            url = getUrl();
        } catch (Exception e) {
            Cytosis.setMetricsEnabled(false);
            return;
        }


        // Configure Span Exporter (for traces)
        OtlpGrpcSpanExporter spanExporter = OtlpGrpcSpanExporter.builder()
                .setEndpoint(url)  // OTLP endpoint
                .build();

        // Configure Tracer Provider (for traces)
        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(BatchSpanProcessor.builder(spanExporter).build())
                .build();

        // Configure Metric Exporter (for metrics)
        OtlpGrpcMetricExporter metricExporter = OtlpGrpcMetricExporter.builder()
                .setEndpoint(url)  // OTLP endpoint
                .build();

        // Configure Meter Provider (for metrics)
        SdkMeterProvider meterProvider = SdkMeterProvider.builder()
                .registerMetricReader(PeriodicMetricReader.builder(metricExporter).build())
                .build();

        // Configure Log Exporter (for logs)
        OtlpGrpcLogRecordExporter logExporter = OtlpGrpcLogRecordExporter.builder()
                .setEndpoint(url)  // OTLP endpoint
                .build();

        // Configure Logger Provider (for logs)
        SdkLoggerProvider loggerProvider = SdkLoggerProvider.builder()
                .addLogRecordProcessor(BatchLogRecordProcessor.builder(logExporter).build())
                .build();

        GlobalOpenTelemetry.resetForTest();
        // Build and register the OpenTelemetry SDK
        OpenTelemetrySdk openTelemetry = OpenTelemetrySdk.builder()
                .setTracerProvider(tracerProvider)
                .setMeterProvider(meterProvider)
                .setLoggerProvider(loggerProvider)
                .buildAndRegisterGlobal();

        Cytosis.setMetricsEnabled(true);
    }

    private static String getUrl() {
        // these get read directly from env as the file manager isn't started yet.
        String OPEN_TELEMETRY_HOST = null;
        int OPEN_TELEMETRY_PORT = -1;


        if (System.getenv("OTEL_PORT") != null) OPEN_TELEMETRY_PORT = Integer.parseInt(System.getenv("OTEL_PORT"));
        if (System.getenv("OTEL_HOSTNAME") != null) OPEN_TELEMETRY_HOST = System.getenv("OTEL_HOSTNAME");

        if (System.getProperty("OTEL_PORT") != null)
            OPEN_TELEMETRY_PORT = Integer.parseInt(System.getProperty("OTEL_PORT"));
        if (System.getProperty("OTEL_HOSTNAME") != null) OPEN_TELEMETRY_HOST = System.getProperty("OTEL_HOSTNAME");

        // disable otel support
        if (OPEN_TELEMETRY_PORT == -1 || OPEN_TELEMETRY_HOST == null || OPEN_TELEMETRY_HOST.isEmpty())
            throw new IllegalArgumentException("OPEN_TELEMETRY_HOST is null");

        return "http://" + OPEN_TELEMETRY_HOST + ":" + OPEN_TELEMETRY_PORT;
    }

    public static Tracer getTracer(String name) {
        return GlobalOpenTelemetry.getTracer(name);
    }

    public static Meter getMeter(String name) {
        return GlobalOpenTelemetry.getMeter(name);
    }

    public static LoggerProvider getLogger(String name) {
        return GlobalOpenTelemetry.get().getLogsBridge();
    }
}
