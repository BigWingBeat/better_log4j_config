package com.pixelstorm.better_log4j_config;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.plugins.util.PluginRegistry;
import org.apache.logging.log4j.core.impl.Log4jContextFactory;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.entrypoint.PreLaunchEntrypoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BetterLog4jConfig implements PreLaunchEntrypoint {
	public static final Logger LOGGER = LoggerFactory.getLogger("Better Log4j Config");

	/**
	 * The path to the config file that Log4j will be reconfigured to use
	 */
	public static final String CONFIG_RESOURCE_PATH = "data/log4j_new.xml";

	/**
	 * An arbitrary unique identifier to be passed to Log4j when loading our
	 * {@link LoggerNamePatternSelector} plugin
	 */
	public static final long BUNDLE_ID = 54321;

	@Override
	public void onPreLaunch(ModContainer mod) {
		LOGGER.info("Starting Log4j reconfiguration.");

		ClassLoader classLoader = mod.getClassLoader();

		// Get log4j to load our plugin, so it doesn't fail to parse the new config file
		loadPlugin(classLoader);

		// Get the URI to the new config file
		URI newConfigUri = getConfigResourceUri(classLoader);

		// Attempt to reconfigure Log4j with the new config
		try {
			reconfigureWithUri(newConfigUri);
		} catch (UnsupportedOperationException e) {
			LOGGER.error("Failed to reconfigure Log4j:");
			LOGGER.error(getPrintedStackTrace(e));
			return;
		}

		LOGGER.info("Finished Log4j reconfiguration.");
	}

	public static void reconfigureWithUri(URI newConfigUri) throws UnsupportedOperationException {
		var factory = LogManager.getFactory();
		if (factory instanceof Log4jContextFactory) {
			reconfigureLog4jContextFactoryWithUri((Log4jContextFactory) factory, newConfigUri);
		} else {
			throw new UnsupportedOperationException(
					String.format("Expected ContextFactory to be Log4jContextFactory, but it was %s instead!",
							factory.getClass().getSimpleName()));
		}
	}

	public static void reconfigureLog4jContextFactoryWithUri(Log4jContextFactory factory,
			URI newConfigUri) {
		// Get the LoggerContexts to be reconfigured
		var contexts = factory.getSelector().getLoggerContexts();

		LOGGER.debug("Reconfiguring %d LoggerContexts:", contexts.size());

		// Reconfigure each of the LoggerContexts
		for (LoggerContext context : contexts) {
			reconfigureLoggerContextWithUri(context, newConfigUri);
			LOGGER.debug("Reconfigured {} (Name {})", context, context.getName());
		}
	}

	public static void reconfigureLoggerContextWithUri(LoggerContext context, URI newConfigUri) {
		context.setConfigLocation(newConfigUri);
	}

	public static URI getConfigResourceUri(ClassLoader loader) {
		try {
			return loader.getResource(CONFIG_RESOURCE_PATH).toURI();
		} catch (URISyntaxException e) {
			throw new RuntimeException("Class loader returned an invalid URI! This should never happen.", e);
		}
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
	public static void loadPlugin(ClassLoader loader) {
		PluginRegistry.getInstance().loadFromBundle(BUNDLE_ID, loader);
	}

	/**
	 * Collects the output of {@link Throwable#printStackTrace()} to a
	 * {@link String}, because there is no built-in method to do this.
	 *
	 * @param e The throwable to get the stack trace of
	 * @return The printed stack trace of the given throwable
	 */
	public static String getPrintedStackTrace(Throwable e) {
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		try (PrintStream printStream = new PrintStream(byteStream)) {
			e.printStackTrace(printStream);
		}
		return byteStream.toString(StandardCharsets.UTF_8);
	}
}
