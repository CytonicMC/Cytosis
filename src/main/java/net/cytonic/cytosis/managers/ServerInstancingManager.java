package net.cytonic.cytosis.managers;

import net.cytonic.cytosis.Bootstrappable;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.bootstrap.annotations.CytosisComponent;
import net.cytonic.cytosis.data.packet.packets.servers.CreateInstancePacket;
import net.cytonic.cytosis.data.packet.packets.servers.DeleteAllInstancesPacket;
import net.cytonic.cytosis.data.packet.packets.servers.DeleteInstancePacket;
import net.cytonic.cytosis.data.packet.packets.servers.UpdateInstancesPacket;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.messaging.NatsManager;
import net.cytonic.cytosis.messaging.Subjects;

@CytosisComponent(dependsOn = {NatsManager.class})
public class ServerInstancingManager implements Bootstrappable {

    public static final String CYTOSIS = "cytosis";
    public static final String CYNDER = "cynder";
    public static final String GILDED_GORGE_HUB = "gg_hub";
    public static final String GILDED_GORGE_INSTANCING = "gg_instancing";
    public static final String CYTONIC_LOBBY = "cytonic_lobby";
    public static final String BEDWARS_SOLOS = "bw_solos";
    public static final String BEDWARS_LOBBY = "bw_lobby";
    public static final String BEDWARS_DUOS = "bw_duos";
    public static final String BEDWARS_TRIOS = "bw_trios";
    public static final String BEDWARS_QUADROS = "bw_quadros";

    public static final String[] TYPES = {CYTOSIS, CYNDER, GILDED_GORGE_HUB, GILDED_GORGE_INSTANCING, CYTONIC_LOBBY,
        BEDWARS_SOLOS, BEDWARS_LOBBY, BEDWARS_DUOS, BEDWARS_TRIOS, BEDWARS_QUADROS};
    private NatsManager nats;

    public static boolean isServerType(String type) {
        for (String t : TYPES) {
            if (t.equals(type)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void init() {
        this.nats = Cytosis.get(NatsManager.class);
    }

    public void createServerInstances(String type, int amount) {
        if (!isServerType(type)) {
            return;
        }
        new CreateInstancePacket(type, amount).request(Subjects.CREATE_SERVER,
            (response, throwable) -> {
                if (throwable != null) {
                    Logger.error("Failed to create server instance: " + type, throwable);
                    return;
                }
                if (!response.isSuccess()) {
                    Logger.error("Failed to create server instance of type %s. (%s)", type, response.getMessage());
                }
            });
    }

    public void deleteAllServerInstances(String type) {
        if (!isServerType(type)) {
            return;
        }
        new DeleteAllInstancesPacket(type).request(Subjects.DELETE_ALL_SERVERS, (response, throwable) -> {
            if (throwable != null) {
                Logger.error("Failed to delete all server instances: " + type, throwable);
                return;
            }
            if (!response.isSuccess()) {
                Logger.error("Failed to delete all server instances of type %s. (%s)", type, response.getMessage());
            }
        });
    }

    public void deleteThisServerInstance() {
        if (!Cytosis.IS_NOMAD) {
            return; // this isn't a nomad server to delete
        }
        deleteServerInstance(System.getenv("NOMAD_JOB_NAME"), System.getenv("NOMAD_ALLOC_ID"));
    }

    public void deleteServerInstance(String type, String allocId) {
        if (!isServerType(type)) {
            return;
        }
        new DeleteInstancePacket(type, allocId).request(Subjects.DELETE_SERVER, (response, throwable) -> {
            if (throwable != null) {
                Logger.error("Failed to delete server instance: " + allocId, throwable);
                return;
            }
            if (!response.isSuccess()) {
                Logger.error("Failed to delete server instance %s of type %s. (%s)", allocId, type,
                    response.getMessage());
            }
        });
    }

    public void updateServers(String type) {
        if (!isServerType(type)) {
            return;
        }
        new UpdateInstancesPacket(type).request(Subjects.UPDATE_SERVER, (response, throwable) -> {
            if (throwable != null) {
                Logger.error("Failed to update server instance type: " + type, throwable);
                return;
            }
            if (!response.isSuccess()) {
                Logger.error("Failed to update server instances of type %s. (%s)", type, response.getMessage());
            }
        });
    }
}