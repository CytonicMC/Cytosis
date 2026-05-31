package net.cytonic.cytosis.server.session;

import java.time.Instant;
import java.util.Collection;
import java.util.UUID;

import dev.minestomunited.entrypoint.session.PlayerSession;
import dev.minestomunited.entrypoint.session.SessionService;
import io.ebean.DB;
import net.minestom.server.entity.PlayerSkin;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.Nullable;

import net.cytonic.cytosis.Cytosis;

public class SessionServiceImpl implements SessionService {

    @Blocking
    @Override
    public PlayerSession createSession(UUID uuid, String username, @Nullable PlayerSkin playerSkin, String ip,
        @Nullable String proxy, String version) {
        SessionEntity sessionEntity = new SessionEntity();
        sessionEntity.uuid(uuid);
        sessionEntity.username(username);
        if (playerSkin != null) {
            sessionEntity.skinSignature(playerSkin.signature());
            sessionEntity.skinTextures(playerSkin.textures());
        }
        sessionEntity.createdAt(Instant.now());
        sessionEntity.clientIp(ip);
        sessionEntity.proxy(proxy);
        sessionEntity.serverId(Cytosis.CONTEXT.SERVER_ID);
        sessionEntity.version(version);
        sessionEntity.save();
        return sessionEntity;
    }

    @Blocking
    @Override
    public void deleteSession(UUID uuid) {
        DB.delete(SessionEntity.class, uuid);
    }

    @Blocking
    @Override
    public Collection<SessionEntity> sync() {
        return DB.find(SessionEntity.class).findList();
    }
}
