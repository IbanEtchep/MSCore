package fr.iban.common.data.redis;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public record RedisCredentials(String ip, String password, int port, String clientName) {

	public String toRedisURL() {
		return "redis://:" + URLEncoder.encode(password, StandardCharsets.UTF_8) + "@" + ip + ":" + port;
	}
}
