package com.pixelstorm.better_log4j_config;

import java.io.IOException;
import java.net.URI;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.plugins.util.PluginRegistry;
import org.apache.logging.log4j.core.util.Loader;

import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

public class BetterLog4jConfig implements PreLaunchEntrypoint {
	public static final Logger LOGGER = LogManager.getLogger("Better Log4j Config");

	/**
	 * An arbitrary unique identifier to be passed to Log4j when loading our
	 * {@link LoggerNamePatternSelector} plugin
	 */
	public static final long BUNDLE_ID = 54321;

	public static ClassLoader CLASSLOADER;

	@Override
	public void onPreLaunch() {
		LOGGER.info("Starting Log4j reconfiguration.");

		CLASSLOADER = Loader.getClassLoader();

		// Get log4j to load our plugin, so it doesn't fail to parse the new config file
		loadPlugin();

		// Get the URI to the new config file
		URI newConfigUri = ConfigFileHandler.getOrCreateDefaultConfigFile();

		// Attempt to reconfigure Log4j with the new config
		try {
			Reconfigurator.reconfigureWithUri(newConfigUri);
		} catch (UnsupportedOperationException | IOException e) {
			LOGGER.error("Failed to reconfigure Log4j:", e);
			return;
		}

		LOGGER.info("Finished Log4j reconfiguration.");
	}

	/**
	 * This will prompt Log4j to scan for our {@link LoggerNamePatternSelector}
	 * plugin using the given {@link ClassLoader}. Log4j does this automatically on
	 * initialisation, but that happens very early in the launch sequence - well
	 * before this mod gets loaded by the mod loader. So, now that this mod has been
	 * loaded and we can run code, we must manually prompt Log4j to scan for plugins
	 * again, at which point our plugin will be loaded with our bundle id.
	 *
	 * @param loader The {@link ClassLoader} to load the plugin from
	 *
	 * @see {@link BetterLog4jConfig#BUNDLE_ID}
	 * @see {@link LoggerNamePatternSelector}
	 */
	public static void loadPlugin() {
		PluginRegistry.getInstance().loadFromBundle(BUNDLE_ID, CLASSLOADER);
	}
}
