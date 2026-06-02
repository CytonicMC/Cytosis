package net.cytonic.cytosis.server.player;

import java.util.UUID;

import dev.minestomunited.entrypoint.player.PlayerData;
import io.ebean.Model;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minestom.server.entity.PlayerSkin;
import org.jetbrains.annotations.Nullable;

@Setter
@Entity
@Getter
@Accessors(fluent = true)
@Table(name = "cytonic_player_data")
public class PlayerDataEntity extends Model implements PlayerData {

    @Id
    @Column(nullable = false)
    private UUID uuid;
    @Column(nullable = false)
    private String username;
    @Column
    @Nullable
    private String skinSignature;
    @Column
    @Nullable
    private String skinTextures;
    @Column(nullable = false)
    private String ip;
    @Column
    @Nullable
    private String proxy;
    @Column(nullable = false)
    private String version;

    @Override
    @Nullable
    public PlayerSkin playerSkin() {
        if (skinTextures == null || skinSignature == null) {
            return null;
        }
        return new PlayerSkin(skinTextures, skinSignature);
    }
}
