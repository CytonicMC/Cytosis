package net.cytonic.cytosis.replay;

import java.time.Instant;
import java.util.UUID;

import net.kyori.adventure.key.Key;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import org.jetbrains.annotations.Nullable;

import net.cytonic.cytosis.report.ReportType;

public record ReplayMeta(Key map, String serverId, @Nullable ReportData reportData, Instant createdAt, Instant start) {

    public static NetworkBuffer.Type<ReplayMeta> NETWORK_TYPE = NetworkBufferTemplate.template(
        NetworkBuffer.KEY, ReplayMeta::map,
        NetworkBuffer.STRING, ReplayMeta::serverId,
        ReportData.NETWORK_TYPE.optional(), ReplayMeta::reportData,
        NetworkBuffer.INSTANT_MS, ReplayMeta::createdAt,
        NetworkBuffer.INSTANT_MS, ReplayMeta::start,
        ReplayMeta::new
    );

    public record ReportData(UUID sender, UUID target, ReportType<?> reportType) {

        public static NetworkBuffer.Type<ReportData> NETWORK_TYPE = NetworkBufferTemplate.template(
            NetworkBuffer.UUID, ReportData::sender,
            NetworkBuffer.UUID, ReportData::target,
            NetworkBuffer.KEY.transform(ReportType::getByKey, ReportType::getKey), ReportData::reportType,
            ReportData::new
        );
    }
}
