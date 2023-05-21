package com.pixelstorm.better_log4j_config;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.impl.Log4jContextFactory;
import org.apache.logging.log4j.spi.LoggerContextFactory;

public class Reconfigurator {
	/**
	 * Attempts to reconfigure Log4j with the config pointed to by the specified
	 * {@link URI}.
	 *
	 * @param newConfigUri The {@link URI} of the new config
	 * @throws UnsupportedOperationException If Log4j could not be reconfigured
	 * @throws IOException                   If the specified URI completely failed
	 *                                       configuration (But not for partial
	 *                                       failures)
	 */
	public static void reconfigureWithUri(URI newConfigUri) throws UnsupportedOperationException, IOException {
		LoggerContextFactory factory = LogManager.getFactory();
		reconfigureLoggerContextFactoryWithUri(factory, newConfigUri);
	}

	/**
	 * Attempts to reconfigure the specified {@link LoggerContextFactory} with the
	 * config pointed to by the specified {@link URI}.
	 *
	 * @param factory      The {@link LoggerContextFactory} to be reconfigured
	 * @param newConfigUri The {@link URI} of the new config
	 * @throws UnsupportedOperationException If the specified factory could not be
	 *                                       reconfigured
	 * @throws IOException                   If the specified URI completely failed
	 *                                       configuration (But not for partial
	 *                                       failures)
	 */
	public static void reconfigureLoggerContextFactoryWithUri(LoggerContextFactory factory, URI newConfigUri)
			throws UnsupportedOperationException, IOException {
		if (factory instanceof Log4jContextFactory) {
			reconfigureLog4jContextFactoryWithUri((Log4jContextFactory) factory, newConfigUri);
		} else {
			throw new UnsupportedOperationException(
					String.format("Expected LoggerContextFactory to be Log4jContextFactory, but it was %s instead!",
							factory.getClass().getSimpleName()));
		}
	}

	/**
	 * Attempts to reconfigure the specified {@link Log4jContextFactory} with the
	 * config pointed to by the specified {@link URI}.
	 *
	 * @param factory      The {@link Log4jContextFactory} to be reconfigured
	 * @param newConfigUri The {@link URI} of the new config
	 * @throws IOException If the specified URI completely failed configuration (But
	 *                     not for partial failures)
	 */
	public static void reconfigureLog4jContextFactoryWithUri(Log4jContextFactory factory,
			URI newConfigUri) throws IOException {
		// Get the LoggerContexts to be reconfigured
		List<LoggerContext> contexts = factory.getSelector().getLoggerContexts();

		BetterLog4jConfig.LOGGER.debug("Reconfiguring {} LoggerContexts:", contexts.size());

		boolean anySucceeded = false;

		// Reconfigure each of the LoggerContexts
		for (LoggerContext context : contexts) {
			if (reconfigureLoggerContextWithUri(context, newConfigUri)) {
				anySucceeded = true;
				BetterLog4jConfig.LOGGER.debug("Reconfigured LoggerContext[{}] ({})", context.getName(), context);
			} else {
				BetterLog4jConfig.LOGGER.warn(
						"Could not reconfigure LoggerContext[{}] ({})! Some log messages may use the incorrect config.",
						context.getName(), context);
			}
		}

		if (!anySucceeded) {
			throw new IOException(String.format(
					"The config URI %s failed configuration with every available LoggerContext!", newConfigUri));
		}
	}

	/**
	 * Attempts to reconfigure the specified {@link LoggerContext} with the config
	 * pointed to by the specified {@link URI}.
	 *
	 * @param context      The {@link LoggerContext} to be reconfigured
	 * @param newConfigUri The {@link URI} of the new config
	 * @return {@code True} if the specified URI was successfully resolved to a
	 *         {@link Configuration}, or {@code False} if the specified URI could
	 *         not be resolved to a configuration.
	 *
	 *         A return value of {@code False} means configuration for this
	 *         LoggerContext definitely failed, but a return value of {@code True}
	 *         does not necessarily mean that configuration succeeded, as the
	 *         resolved configuration may be broken or malformed.
	 */
	public static boolean reconfigureLoggerContextWithUri(LoggerContext context, URI newConfigUri) {
		Configuration configuration = ConfigurationFactory.getInstance().getConfiguration(context, context.getName(),
				newConfigUri);
		if (configuration != null) {
			context.reconfigure(configuration);
			return true;
		}
		return false;
	}
}
