package com.pixelstorm.elytra_tech;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;

import javax.management.RuntimeErrorException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.AbstractConfiguration;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.plugins.processor.PluginCache;
import org.apache.logging.log4j.core.config.plugins.processor.PluginEntry;
import org.apache.logging.log4j.core.config.plugins.util.PluginRegistry;
import org.apache.logging.log4j.core.config.plugins.util.PluginType;
import org.apache.logging.log4j.core.config.xml.XmlConfiguration;
import org.apache.logging.log4j.core.config.xml.XmlConfigurationFactory;
import org.apache.logging.log4j.core.impl.Log4jContextFactory;
import org.apache.logging.log4j.core.util.Loader;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.entrypoint.PreLaunchEntrypoint;
import org.slf4j.LoggerFactory;

public class PreLaunch implements PreLaunchEntrypoint {
	public static final String LOG_CONFIG_RESOURCE_PATH = "data/log4j_new.xml";
	public static final long BUNDLE_ID = 54321;

	@Override
	public void onPreLaunch(ModContainer mod) {
		var classLoader = Loader.getClassLoader();
		PluginRegistry.getInstance().loadFromBundle(BUNDLE_ID, classLoader);

		URI uri;
		try {
			uri = classLoader.getResource(LOG_CONFIG_RESOURCE_PATH).toURI();
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}

		var contextNames = new ArrayList<String>();

		var factory = LogManager.getFactory();
		if (factory instanceof Log4jContextFactory) {
			var selector = ((Log4jContextFactory) factory).getSelector();
			var contexts = selector.getLoggerContexts();
			for (LoggerContext context : contexts) {
				context.setConfigLocation(uri);
				contextNames.add(String.format("%s (Name %s)", context, context.getName()));
			}
		} else {
			throw new RuntimeException(String
					.format("Expected ContextFactory to be Log4jContextFactory, but it was %s instead!", factory));
		}

		var logger = LoggerFactory.getLogger("Elytra Tech (Pre Launch)");
		logger.info("Finished reconfiguration");
		logger.info("Reconfigured {} contexts:", contextNames.size());
		for (String contextName : contextNames) {
			logger.info(contextName);
		}
	}
}
