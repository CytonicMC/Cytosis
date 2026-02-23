# Cytosis

Cytosis is a high-performance Minecraft server framework built on top of Minestom, designed for scalability,
extensibility, and modern development practices. It provides a robust set of core systems including a component-based
dependency injection framework, cross-server messaging via NATS, and integrated data persistence using Ebean.

## Table of Contents

- [Core Framework](#core-framework)
- [Data & Persistence](#data-persistence)
- [Networking & Messaging](#networking--messaging)
- [Player Systems](#player-systems)
- [UI & Display Systems](#ui--display-systems)
- [Game World & Entities](#game-world--entities)
- [Commands & Events](#commands--events)
- [Moderation & Security](#moderation--security)
- [Observability](#observability)

---

## Core Framework

Cytosis uses a custom component-based architecture for managing system lifecycles and dependencies, ensuring that
services are initialized in the correct order.

### Component System

Systems are defined as components using the `@CytosisComponent` annotation. This allows for automatic discovery and
dependency management during the bootstrap process.

```java
@CytosisComponent(dependsOn = {DatabaseManager.class, RedisManager.class})
public class MyService implements Bootstrappable {
    @Override
    public void init() {
        // Initialization logic called after dependencies are ready
    }
}
```

Components can be retrieved from the central context:

```java
MyService service = Cytosis.get(MyService.class);
```

### Plugin System

Cytosis includes a built-in plugin loader that supports Java plugins. Plugins can define their own components, entities,
and commands, integrating seamlessly with the core framework.

---

## Data & Persistence

Cytosis integrates Ebean ORM for robust data persistence, supporting both global and environment-specific databases.

### Database Management

The `EbeanManager` handles database connections and migrations. It automatically scans for `@Entity` and `@Embeddable`
classes within the project and loaded plugins, managing schema updates through Flyway-style migrations.

### Databases

- **Global Database**: Stores network-wide data such as player ranks, friends, and global settings.
- **Environment Database**: Stores server-specific data such as local instance state or temporary metadata.

---

## Networking & Messaging

Cytosis provides a high-level protocol layer on top of NATS that gives you type-safe request/response APIs and
fire-and-forget notifications without dealing with raw subjects or byte arrays. Redis is used alongside this for
low-latency caching and coordination.

### Protocol System

The protocol system lives in the `protocol` module and is auto-wired at runtime:

- `ProtocolObject<T,R>`: Defines a subject and (de)serialization for a request `T` and a response `R`.
- `NoResponse<T>` / `NotifyPacket<T>`: Publish-only notifications (no replies) for event fan-out.
- `Endpoint<T,R>`: Server-side handler for a subject that can return a response asynchronously.
- `Message<T,R>`: A convenience interface implemented by your request/packet records to `publish()` or `request()`
  themselves.
- Auto-registration: `ProtocolHelper.init()` scans the `net.cytonic` package for protocol objects, endpoints, and notify
  listeners/handlers and subscribes them via `NatsAPI` (backed by Cytosis' `NatsManager`).

#### Request/Response (ProtocolObject)

Define a typed request/response pair and use it without manual serialization.

```java
// Subject: "servers.list"
public final class FetchServersProtocolObject extends ProtocolObject<Packet, Response> {
    @Override public String getSubject() { return "servers.list"; }
    public record Packet() implements Message<Packet, Response> {}
    public record Response(List<ServerStatusNotifyPacket.Packet> servers) {}
}

// Request active servers from Cydian
new FetchServersProtocolObject.Packet().request((response, throwable) -> {
    if (throwable != null) {
        Logger.error("failed to fetch active servers", throwable);
        return;
    }
    for (ServerStatusNotifyPacket.Packet server : response.servers()) {
        // handle server entries
    }
});
```

#### Fire-and-forget notifications (NotifyPacket/NoResponse)

Publish events network-wide without expecting a reply.

```java
// Subject: "chat.message"
var packet = new ChatMessageNotifyPacket.Packet(
        recipients,             // Set<UUID> or null to broadcast
        "GLOBAL",               // or an enum; an overloaded ctor accepts Enum<?>
        new JsonComponent(Component.text("Hello!")),
        senderUuid              // UUID or null
    );
packet.

publish(); // Uses the subject defined by ChatMessageNotifyPacket
```

#### Handling requests (Endpoint)

Implement an endpoint to handle a subject and produce an optional response.

```java
public final class HealthCheckEndpoint implements
    Endpoint<HealthCheckProtocolObject.Packet, HealthCheckProtocolObject.Response> {

    @Override
    public CompletableFuture<HealthCheckProtocolObject.Response> onMessage(
        HealthCheckProtocolObject.Packet message, NotifyData meta) {
        return CompletableFuture.completedFuture(new HealthCheckProtocolObject.Response());
    }

    @Override
    public String getSubject() {
        return "health.check." + Cytosis.CONTEXT.SERVER_ID;
    }
}
```

#### Listening to notifications

You can consume notifications either by implementing `NotifyListener<T>` or by annotating a handler method with
`@NotifyHandler`.

```java
// Type-safe listener bound to the protocol object's subject
public final class ChatListener implements NotifyListener<ChatMessageNotifyPacket.Packet> {
    @Override public String getSubject() { return "chat.message"; }
    @Override public void onMessage(ChatMessageNotifyPacket.Packet msg, NotifyData ctx) {
        // handle chat message
    }
}

// Or via annotation (method subject override is supported when the first field is a String)
public final class AnnotatedHandlers {
    @NotifyHandler(subject = "chat.message")
    public void onChat(ChatMessageNotifyPacket.Packet msg, NotifyData ctx) {
        // handle chat message
    }
}
```

### Redis

Redis is primarily used for caching and managing network-wide cooldowns, ensuring consistency across multiple server
instances.

---

## Player Systems

The `CytosisPlayer` class extends the base Minestom `Player` to provide integrated support for all framework features.

### Preference System

Persistent player preferences are managed through a typed registry, allowing for easy addition of new settings.

```java
// Updating a preference
player.updatePreference(Preferences.CHAT_VISIBILITY, ChatVisibility.ALL);

// Retrieving a preference
ChatVisibility visibility = player.getPreference(Preferences.CHAT_VISIBILITY);
```

### Friend & Party Systems

Integrated support for managing friends and parties across the network, including request handling and synchronized
state via NATS.

---

## UI & Display Systems

Cytosis provides several systems for managing the player's visual experience.

### Sideboard (Scoreboard)

The `SideboardManager` facilitates the creation of dynamic, per-player scoreboards that update automatically.

### PlayerList (Tablist)

The `PlayerListManager` allows for custom tablist layouts, supporting multiple columns, headers, footers, and
player-specific entries.

### Actionbar Manager

Easily send and manage action bar messages with support for automated rotations and priority-based display.

---

## Game World & Entities

### Instance Management

The `InstanceManager` facilitates world loading and management. It supports high-performance world formats like Polar
and provides utilities for block placement and world manipulation.

### NPC & Hologram Systems

The `NpcManager` and hologram systems allow for easy creation and management of non-player entities and floating text
displays, including support for per-player visibility and interaction.

---

## Commands & Events

### Command Handling

Commands are registered through the `CommandHandler`. Cytosis uses Minestom's modern command API, enhanced with custom
permission checking and player-specific argument providers.

### Event System

The `EventHandler` manages server and custom events, providing a unified way to respond to player actions and system
state changes. It has the `@Listener` annotation to mark a method as an event handler. Classes that implement `Event`
are automatically registered, so the listener API supports custom event classes.

```java
@Listener
public void myListener(MyEvent event) {
    // do something with the event
}
```

---

## Moderation & Security

### Report System

A built-in reporting system allows players to report others for various offenses. Reports are persisted in the database
and broadcasted to staff members across the network.

### Snooper

The `SnooperManager` allows staff to monitor various server activities, such as chat, commands, and reports, in
real-time, even across different server instances.

---

## Observability

### Metrics & OpenTelemetry

Cytosis includes built-in support for OpenTelemetry to monitor server performance. It tracks execution times, gathers
JVM metrics, and provides insights into the health of various systems.

---

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
