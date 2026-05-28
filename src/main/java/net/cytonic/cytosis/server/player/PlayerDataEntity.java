package net.cytonic.cytosis.server.player;

import java.util.UUID;

import dev.minestomunited.entrypoint.player.PlayerData;
import io.ebean.Model;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Setter;
import net.minestom.server.entity.PlayerSkin;

@Setter
@Entity
@Table(name = "cytonic_player_data")
public class PlayerDataEntity extends Model implements PlayerData {

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
    private String ip;
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
    public String ip() {
        return ip;
    }

    @Override
    public String version() {
        return version;
    }
}
