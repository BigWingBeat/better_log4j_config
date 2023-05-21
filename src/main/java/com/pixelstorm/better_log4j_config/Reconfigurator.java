package com.pixelstorm.better_log4j_config;

import java.net.URI;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.impl.Log4jContextFactory;
import org.apache.logging.log4j.spi.LoggerContextFactory;

public class Reconfigurator {
	public static void reconfigureWithUri(URI newConfigUri) throws UnsupportedOperationException {
		LoggerContextFactory factory = LogManager.getFactory();
		reconfigureLoggerContextFactoryWithUri(factory, newConfigUri);
	}

	public static void reconfigureLoggerContextFactoryWithUri(LoggerContextFactory factory, URI newConfigUri)
			throws UnsupportedOperationException {
		if (factory instanceof Log4jContextFactory) {
			reconfigureLog4jContextFactoryWithUri((Log4jContextFactory) factory, newConfigUri);
		} else {
			throw new UnsupportedOperationException(
					String.format("Expected LoggerContextFactory to be Log4jContextFactory, but it was %s instead!",
							factory.getClass().getSimpleName()));
		}
	}

	public static void reconfigureLog4jContextFactoryWithUri(Log4jContextFactory factory,
			URI newConfigUri) {
		// Get the LoggerContexts to be reconfigured
		List<LoggerContext> contexts = factory.getSelector().getLoggerContexts();

		BetterLog4jConfig.LOGGER.debug("Reconfiguring {} LoggerContexts:", contexts.size());

		// Reconfigure each of the LoggerContexts
		for (LoggerContext context : contexts) {
			reconfigureLoggerContextWithUri(context, newConfigUri);
			BetterLog4jConfig.LOGGER.debug("Reconfigured LoggerContext[{}] ({})", context.getName(), context);
		}
	}

	public static void reconfigureLoggerContextWithUri(LoggerContext context, URI newConfigUri) {
		Configuration configuration = ConfigurationFactory.getInstance().getConfiguration(context, context.getName(),
				newConfigUri);
		if (configuration == null) {
			BetterLog4jConfig.LOGGER.warn(
					"Could not reconfigure LoggerContext[{}] ({})! Some log messages may use the incorrect config.",
					context.getName(), context);
		} else {
			context.reconfigure(configuration);
		}
		// context.setConfigLocation(newConfigUri);
	}
}
