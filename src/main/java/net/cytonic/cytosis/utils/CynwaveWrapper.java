package net.cytonic.cytosis.utils;

import net.cytonic.cytosis.config.CytosisSettings;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class CynwaveWrapper {
    private final OkHttpClient httpClient;


    public CynwaveWrapper() {
        httpClient = new OkHttpClient();
    }

    public CompletableFuture<String> sendFriendRequest(UUID target, UUID sender) {
        return CompletableFuture.supplyAsync(() -> {
            Request request = new Request.Builder()
                    .url(STR."\{CytosisSettings.CYNWAVE_URL}/friend-requests/\{target.toString()}")
                    .header("User-Agent", sender.toString())
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

    public CompletableFuture<String> acceptFriendRequest(UUID target, UUID sender) {
        return CompletableFuture.supplyAsync(() -> {
            Request request = new Request.Builder()
                    .url(STR."\{CytosisSettings.CYNWAVE_URL}/friend-requests/\{target.toString()}")
                    .header("User-Agent", sender.toString())
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

    public CompletableFuture<String> declineFriendRequest(UUID target, UUID sender) {
        return CompletableFuture.supplyAsync(() -> {
            Request request = new Request.Builder()
                    .url(STR."\{CytosisSettings.CYNWAVE_URL}/friend-requests/\{target.toString()}")
                    .header("User-Agent", sender.toString())
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
