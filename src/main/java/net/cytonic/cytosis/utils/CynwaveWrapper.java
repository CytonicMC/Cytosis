package net.cytonic.cytosis.utils;

import net.cytonic.cytosis.config.CytosisSettings;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * A class wrapping the Cynwave API
 */
public class CynwaveWrapper {
    private final OkHttpClient httpClient;


    /**
     * Creates a new CynwaveWrapper, initializating the http client
     */
    public CynwaveWrapper() {
        httpClient = new OkHttpClient();
    }

    /**
     * Sends a friend request to the API for processing
     *
     * @param target the player to send the request to
     * @param sender the player sending the request
     * @return a future containing the response message
     */
    public CompletableFuture<String> sendFriendRequest(UUID target, UUID sender) {
        return CompletableFuture.supplyAsync(() -> {
            Request request = new Request.Builder()
                    .url(STR."\{CytosisSettings.CYNWAVE_URL}/friend-requests/\{target.toString()}")
                    .header("User-Agent", sender.toString())
                    .header("Authorization", CytosisSettings.CYNWAVE_TOKEN)
                    .post(RequestBody.create(new byte[0]))
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                return response.body().string();
            } catch (IOException e) {
                // will be handled in by the caller
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Sends a message to the Cynwave API to accept the friend request
     *
     * @param target the player who is accepting the request
     * @param sender the original sender of the request
     * @return the future containing the response
     */
    public CompletableFuture<String> acceptFriendRequest(UUID target, UUID sender) {
        return CompletableFuture.supplyAsync(() -> {
            Request request = new Request.Builder()
                    .url(STR."\{CytosisSettings.CYNWAVE_URL}/friend-requests/\{target.toString()}")
                    .header("User-Agent", sender.toString())
                    .header("Authorization", CytosisSettings.CYNWAVE_TOKEN)
                    .put(RequestBody.create(new byte[0]))
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                return response.body().string();
            } catch (IOException e) {
                // will be handled in by the caller
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Sends a message to the Cynwave API to decline the friend request
     *
     * @param target The target of the request
     * @param sender the sender of the original request
     * @return the future containing the response
     */
    public CompletableFuture<String> declineFriendRequest(UUID target, UUID sender) {
        return CompletableFuture.supplyAsync(() -> {
            Request request = new Request.Builder()
                    .url(STR."\{CytosisSettings.CYNWAVE_URL}/friend-requests/\{target.toString()}")
                    .header("User-Agent", sender.toString())
                    .header("Authorization", CytosisSettings.CYNWAVE_TOKEN)
                    .delete()
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                return response.body().string();
            } catch (IOException e) {
                // will be handled in by the caller
                throw new RuntimeException(e);
            }
        });
    }
}
