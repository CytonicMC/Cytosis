package net.cytonic.cytosis.server.session;

import java.time.Instant;
import java.util.Collection;
import java.util.UUID;

import dev.minestomunited.entrypoint.session.PlayerSession;
import dev.minestomunited.entrypoint.session.SessionService;
import io.ebean.DB;
import net.minestom.server.entity.PlayerSkin;
import org.jetbrains.annotations.Nullable;

public class SessionServiceImpl implements SessionService {

    @Override
    public PlayerSession createSession(UUID uuid, String username, @Nullable PlayerSkin playerSkin, String ip,
        @Nullable String proxy, String version) {
        SessionEntity sessionEntity = new SessionEntity();
        sessionEntity.setUuid(uuid);
        sessionEntity.setUsername(username);
        if (playerSkin != null) {
            sessionEntity.setSkinSignature(playerSkin.signature());
            sessionEntity.setSkinTextures(playerSkin.textures());
        }
        sessionEntity.setCreatedAt(Instant.now());
        sessionEntity.setClientIp(ip);
        sessionEntity.setProxy(proxy);
        sessionEntity.setServerId("unknown");
        sessionEntity.setVersion(version);
        sessionEntity.save();
        return sessionEntity;
    }

    @Override
    public void deleteSession(UUID uuid) {
        DB.delete(SessionEntity.class, uuid);
    }

    @Override
    public Collection<SessionEntity> sync() {
        return DB.find(SessionEntity.class).findList();
    }
}
