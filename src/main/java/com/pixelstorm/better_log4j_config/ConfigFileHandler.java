package com.pixelstorm.better_log4j_config;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.quiltmc.loader.api.QuiltLoader;

public class ConfigFileHandler {
	public static final String CONFIG_FILENAME = "better_log4j_config.xml";

	/**
	 * The path to the fallback config resource that will be used if the default
	 * config file does not exist
	 */
	public static final String FALLBACK_CONFIG_RESOURCE_PATH = "data/fallback_log4j_config.xml";

	public static Path getDefaultConfigPath() {
		return QuiltLoader.getConfigDir().resolve(CONFIG_FILENAME);
	}

	public static URI getOrCreateDefaultConfigFile() {
		Path configPath = getDefaultConfigPath();
		if (!Files.exists(configPath)) {
			BetterLog4jConfig.LOGGER.warn(
					"Expected to find config file in default location '{}', but it does not exist! The fallback config will be written to this location to fix this.",
					configPath);
			try {
				writeFallbackConfig(configPath);
			} catch (IOException e) {
				BetterLog4jConfig.LOGGER.error(
						"Could not write fallback config to the aforementioned location! The fallback config will be used directly for this session instead, but this error may happen again if the issue is not fixed:");
				BetterLog4jConfig.LOGGER.error(BetterLog4jConfig.getPrintedStackTrace(e));
			}
			return getFallbackConfigUri();
		}
		return configPath.toUri();
	}

	public static URI getFallbackConfigUri() {
		try {
			return BetterLog4jConfig.CLASSLOADER.getResource(FALLBACK_CONFIG_RESOURCE_PATH).toURI();
		} catch (URISyntaxException e) {
			BetterLog4jConfig.LOGGER.error("Class loader returned an invalid URI! This should never happen.");
			BetterLog4jConfig.LOGGER.error(BetterLog4jConfig.getPrintedStackTrace(e));
			throw new RuntimeException(e);
		}
	}

	public static InputStream getFallbackConfigBytes() {
		return BetterLog4jConfig.CLASSLOADER.getResourceAsStream(FALLBACK_CONFIG_RESOURCE_PATH);
	}

	public static void writeFallbackConfig(Path configPath) throws IOException {
		// Use CREATE_NEW instead of the default of CREATE to avoid overwriting an
		// existing file
		try (InputStream input = getFallbackConfigBytes();
				OutputStream output = Files.newOutputStream(configPath, StandardOpenOption.CREATE_NEW,
						StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);) {
			input.transferTo(output);
		}
	}
}
