package net.cytonic.cytosis.managers;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.*;
import io.kubernetes.client.util.ClientBuilder;
import net.cytonic.cytosis.logging.Logger;

import java.io.IOException;
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
    }

    public void createCytosisInstance() {
        Map<String, String> labels = new HashMap<>();
        List<V1Container> containers = new ArrayList<>();
        List<V1EnvFromSource> envVars = new ArrayList<>();

        labels.put("app", "cytosis");
        envVars.add(new V1EnvFromSource().configMapRef(new V1ConfigMapEnvSource().name("cytosis-config")));
        envVars.add(new V1EnvFromSource().configMapRef(new V1ConfigMapEnvSource().name("general-config")));

        V1Container container = new V1Container().name("cytosis-container").image("ghcr.io/cytonicmc/cytosis:latest").envFrom(envVars).imagePullPolicy("Always");
        containers.add(container);

        V1Pod lobbyServerPod = new V1Pod().apiVersion("v1").kind("Pod").metadata(new V1ObjectMeta().name("cytosis").labels(labels)).spec(new V1PodSpec().containers(containers));
        api.createNamespacedPod("default", lobbyServerPod);
    }
}
