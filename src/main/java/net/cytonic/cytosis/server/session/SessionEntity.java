package net.cytonic.cytosis.server.session;

import java.time.Instant;
import java.util.UUID;

import dev.minestomunited.entrypoint.session.PlayerSession;
import io.ebean.Model;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Setter;
import net.minestom.server.entity.PlayerSkin;
import org.jetbrains.annotations.Nullable;

@Setter
@Entity
@Table(name = "cytonic_sessions")
public class SessionEntity extends Model implements PlayerSession {

    @Id
    @Column(nullable = false)
    private UUID uuid;
    @Column(nullable = false)
    private String username;
    @Column(nullable = false)
    private String skinSignature;
    @Column(nullable = false)
    private String skinTextures;
    @Column(nullable = false)
    private Instant createdAt;
    @Column(nullable = false)
    private String clientIp;
    @Column
    @Nullable
    private String proxy;
    @Column(nullable = false)
    private String serverId;
    @Column(nullable = false)
    private String version;

    @Override
    public UUID uuid() {
        return uuid;
    }

    @Override
    public String username() {
        return username;
    }

    @Override
    public PlayerSkin playerSkin() {
        return new PlayerSkin(skinTextures, skinSignature);
    }

    @Override
    public Instant createdAt() {
        return createdAt;
    }

    @Override
    public String clientIp() {
        return clientIp;
    }

    @Override
    public @Nullable String proxy() {
        return proxy;
    }

    @Override
    public String serverId() {
        return serverId;
    }

    @Override
    public String version() {
        return version;
    }
}
