package net.cytonic.cytosis.managers;

import io.kubernetes.client.custom.Quantity;
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

/**
 * A manager class to aid in the organisation of containerized instances
 */
public class ContainerizedInstanceManager {
    private CoreV1Api api;

    /**
     * Creates the manager
     */
    public ContainerizedInstanceManager() {
        ApiClient client;
        try {
            client = ClientBuilder.cluster().build();
        } catch (IOException e) {
            Logger.error("An error occurred whilst initializing the Kubernetes API!", e);
            return;
        }
        Configuration.setDefaultApiClient(client);
        api = new CoreV1Api();
        CytosisSettings.KUBERNETES_SUPPORTED = true;
    }

    /**
     * Creates a new cytosis instance on the k8s cluster
     */
    public void createCytosisInstance() {
        Map<String, String> labels = new HashMap<>();
        List<V1Container> containers = new ArrayList<>();
        List<V1EnvFromSource> envVars = new ArrayList<>();

        Map<String, Quantity> requests = new HashMap<>();
        requests.put("cpu", Quantity.fromString("500m")); // 0.5 CPU
        requests.put("memory", Quantity.fromString("512Mi")); // 512 MiB

        Map<String, Quantity> limits = new HashMap<>();
        limits.put("cpu", Quantity.fromString("1")); // 1 CPU
        limits.put("memory", Quantity.fromString("1Gi")); // 1 GiB

        V1ResourceRequirements resources = new V1ResourceRequirements();
        resources.setRequests(requests);
        resources.setLimits(limits);

        labels.put("app", "cytosis");
        envVars.add(new V1EnvFromSource().configMapRef(new V1ConfigMapEnvSource().name("cytosis-config")));
        envVars.add(new V1EnvFromSource().configMapRef(new V1ConfigMapEnvSource().name("general-config")));

        V1Container container = new V1Container().name("cytosis-container").image("ghcr.io/cytonicmc/cytosis:latest")
                .envFrom(envVars).resources(resources).imagePullPolicy("Always");
        containers.add(container);

        V1Pod lobbyServerPod = new V1Pod().apiVersion("v1").kind("Pod")
                .metadata(new V1ObjectMeta().name(STR."cytosis-\{Math.abs(Instant.now().hashCode())}")
                        .labels(labels)).spec(new V1PodSpec().containers(containers)
                        .addImagePullSecretsItem(new V1LocalObjectReference().name("ghcr-login-secret")));
        try {
            api.createNamespacedPod("default", lobbyServerPod).execute();
        } catch (ApiException e) {
            Logger.error("An error occurred whilst creating the Cytosis instance!", e);
        }
    }

    /**
     * Creates a new cytosis instance on the k8s cluster
     */
    public void createProxyInstance() {
        Map<String, String> labels = new HashMap<>();
        List<V1Container> containers = new ArrayList<>();
        List<V1EnvFromSource> envVars = new ArrayList<>();

        Map<String, Quantity> requests = new HashMap<>();
        requests.put("cpu", Quantity.fromString("500m")); // 0.5 CPU
        requests.put("memory", Quantity.fromString("1Gi")); // 1 GiB

        Map<String, Quantity> limits = new HashMap<>();
        limits.put("cpu", Quantity.fromString("1")); // 1 CPU
        limits.put("memory", Quantity.fromString("2Gi")); // 2 GiB

        V1ResourceRequirements resources = new V1ResourceRequirements();
        resources.setRequests(requests);
        resources.setLimits(limits);

        labels.put("app", "cynturion");
        envVars.add(new V1EnvFromSource().configMapRef(new V1ConfigMapEnvSource().name("cynturion-config")));
        envVars.add(new V1EnvFromSource().configMapRef(new V1ConfigMapEnvSource().name("general-config")));

        V1Container container = new V1Container().name("cynturion-container").image("ghcr.io/cytonicmc/cynturion:latest")
                .envFrom(envVars).resources(resources).imagePullPolicy("Always");
        containers.add(container);

        V1Pod proxyPod = new V1Pod().apiVersion("v1").kind("Pod")
                .metadata(new V1ObjectMeta().name(STR."cynturion-\{Math.abs(Instant.now().hashCode())}")
                        .labels(labels)).spec(new V1PodSpec().containers(containers)
                        .addImagePullSecretsItem(new V1LocalObjectReference().name("ghcr-login-secret")));
        try {
            api.createNamespacedPod("default", proxyPod).execute();
        } catch (ApiException e) {
            Logger.error("An error occurred whilst creating the Proxy instance!", e);
        }
    }

    /**
     * Shuts down all cytosis instances in the cluster
     */
    public void shutdownAllCytosisInstances() {
        try {
            V1PodList list = api.listNamespacedPod("default").execute();
            for (V1Pod pod : list.getItems()) {
                if (!pod.getMetadata().getName().contains("cytosis")) continue;
                api.deleteNamespacedPod(pod.getMetadata().getName(), "default").execute();
            }
        } catch (ApiException e) {
            Logger.error("An error occurred whilst shutting down the Cytosis instances!", e);
        }
    }

    /**
     * Shuts down all proxy instances in the cluster
     */
    public void shutdownAllProxyInstances() {
        try {
            V1PodList list = api.listNamespacedPod("default").execute();
            for (V1Pod pod : list.getItems()) {
                if (!pod.getMetadata().getName().contains("cynturion")) continue;
                api.deleteNamespacedPod(pod.getMetadata().getName(), "default").execute();
            }
        } catch (ApiException e) {
            Logger.error("An error occurred whilst shutting down the Proxy instances!", e);
        }
    }
}
