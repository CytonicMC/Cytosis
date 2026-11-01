package net.cytonic.cytosis.replay;

import java.time.Instant;
import java.util.UUID;

import net.kyori.adventure.key.Key;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import org.jetbrains.annotations.Nullable;

import net.cytonic.cytosis.report.ReportType;

public record ReplayMeta(Key map, String serverId, @Nullable ReportData reportData, Instant createdAt, Instant start) {

    public static final int DATA_VERSION = 1;

    public static final NetworkBuffer.Type<ReplayMeta> NETWORK_TYPE = NetworkBuffer.VAR_INT.unionType(i -> switch (i) {
        case 1 -> NetworkBufferTemplate.template(
                NetworkBuffer.KEY, ReplayMeta::map,
                NetworkBuffer.STRING, ReplayMeta::serverId,
                ReportData.NETWORK_TYPE.optional(), ReplayMeta::reportData,
                NetworkBuffer.INSTANT_MS, ReplayMeta::createdAt,
                NetworkBuffer.INSTANT_MS, ReplayMeta::start,
                ReplayMeta::new
        );
        default -> throw new UnsupportedFormatException("Unknown meta version: " + i);
    }, _ -> DATA_VERSION);


    public record ReportData(UUID sender, UUID target, ReportType<?> reportType) {
        public static final int DATA_VERSION = 1;

        public static final NetworkBuffer.Type<ReportData> NETWORK_TYPE = NetworkBuffer.VAR_INT.unionType(i -> switch (i) {
            case 1 -> NetworkBufferTemplate.template(
                    NetworkBuffer.UUID, ReportData::sender,
                    NetworkBuffer.UUID, ReportData::target,
                    NetworkBuffer.KEY.transform(ReportType::getByKey, ReportType::getKey), ReportData::reportType,
                    ReportData::new
            );
            default -> throw new UnsupportedFormatException("Unknown report data version: " + i);
        }, _ -> DATA_VERSION);

    }
}
