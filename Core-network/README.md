# core-network

Cross-server services for MS plugins (messaging, player presence, teleport) behind one interface,
so a plugin runs with or without MSCore.

- `NetworkBridges.create(plugin)` returns the MSCore implementation when the `Core` plugin is enabled,
  a single-server implementation otherwise. Declare `softdepend: [Core]` (or keep `depend`) so Core enables first.
- The MSCore implementation keeps MSCore's message format: a plugin using this library and a plugin still
  calling `MessagingManager` directly exchange messages on the same channels.

## Usage

```kotlin
dependencies {
    implementation("com.github.IbanEtchep.MSCore:core-network:<version>")
}

tasks.shadowJar {
    // Relocate: each plugin carries its own copy, no class is shared between plugins at runtime.
    relocate("fr.iban.network", "fr.iban.myplugin.libs.network")
}
```

```java
NetworkBridge network = NetworkBridges.create(this);

network.messenger().subscribe("myplugin:sync", message -> reload(UUID.fromString(message.payload())));
network.messenger().publish("myplugin:sync", id.toString());

if (network.presence().isOnline(uuid)) {
    network.presence().sendMessage(uuid, Component.text("Hello"));
}

network.teleporter().teleport(player, location, 3);

// onDisable
network.close();
```

Handlers passed to `subscribe` run off the main thread.
