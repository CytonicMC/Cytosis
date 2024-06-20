package net.cytonic.cytosis.managers;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.*;
import io.kubernetes.client.util.ClientBuilder;
import net.cytonic.cytosis.config.CytosisSettings;
import net.cytonic.cytosis.logging.Logger;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContainerizedInstanceManager {
    private CoreV1Api api;

    public ContainerizedInstanceManager() {
        ApiClient client;
        try {
            client = ClientBuilder.cluster().build();
        } catch (IOException e) {
            Logger.error("An error occoured whilst initializing the Kubernetes API!", e);
            return;
        }
        Configuration.setDefaultApiClient(client);
        api = new CoreV1Api();
        CytosisSettings.KUBERNETES_SUPPORTED = true;
    }

    public void createCytosisInstance() {
        Map<String, String> labels = new HashMap<>();
        List<V1Container> containers = new ArrayList<>();
        List<V1EnvFromSource> envVars = new ArrayList<>();

        labels.put("app", "cytosis");
        envVars.add(new V1EnvFromSource().configMapRef(new V1ConfigMapEnvSource().name("cytosis-config")));
        envVars.add(new V1EnvFromSource().configMapRef(new V1ConfigMapEnvSource().name("general-config")));

        V1Container container = new V1Container().name("cytosis-container").image("ghcr.io/cytonicmc/cytosis:latest")
                .envFrom(envVars).imagePullPolicy("Always");
        containers.add(container);

        V1Pod lobbyServerPod = new V1Pod().apiVersion("v1").kind("Pod")
                .metadata(new V1ObjectMeta().name(STR."cytosis-\{Math.abs(Instant.now().hashCode())}")
                        .labels(labels)).spec(new V1PodSpec().containers(containers)
                        .addImagePullSecretsItem(new V1LocalObjectReference().name("ghcr-login-secret")));
        try {
            api.createNamespacedPod("default", lobbyServerPod).execute();
        } catch (ApiException e) {
            Logger.error("An error occoured whilst creating the Cytosis instance!", e);
        }
    }

    /**
     * Shuts down all cytosis instances in the cluster
     */
    public void shutdownAllInstances() {
        try {
            V1PodList list = api.listNamespacedPod("default").execute();

            for (V1Pod pod : list.getItems()) {
                if (!pod.getMetadata().getName().contains("cytosis")) continue;
                api.deleteNamespacedPod(pod.getMetadata().getName(), "default").execute();
            }
        } catch (ApiException e) {
            Logger.error("An error occoured whilst shutting down the Cytosis instances!", e);
        }
    }
}
