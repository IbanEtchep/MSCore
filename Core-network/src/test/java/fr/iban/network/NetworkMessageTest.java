package fr.iban.network;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NetworkMessageTest {

    /**
     * JSON as written by Gson for MSCore's SLocation, e.g. a guild home stored in the database.
     */
    private static final String SLOCATION_JSON =
            "{\"x\":1.5,\"y\":64.0,\"z\":-3.25,\"pitch\":10.0,\"yaw\":90.0,\"world\":\"world\",\"server\":\"survie1\"}";

    @Test
    void readsSLocationJsonAsNetworkLocation() {
        NetworkMessage message = new NetworkMessage("guilds:home", "survie1", SLOCATION_JSON);

        NetworkLocation location = message.payloadAs(NetworkLocation.class);

        assertEquals(new NetworkLocation("survie1", "world", 1.5, 64.0, -3.25, 10.0f, 90.0f), location);
    }

    @Test
    void writesNetworkLocationWithSLocationFieldNames() {
        NetworkLocation location = new NetworkLocation("survie1", "world", 1.5, 64.0, -3.25, 10.0f, 90.0f);

        NetworkLocation roundTrip = new Gson().fromJson(new Gson().toJson(location), NetworkLocation.class);

        assertEquals(location, roundTrip);
        assertEquals(new Gson().toJsonTree(location).getAsJsonObject().keySet(),
                new Gson().fromJson(SLOCATION_JSON, com.google.gson.JsonObject.class).keySet());
    }
}
