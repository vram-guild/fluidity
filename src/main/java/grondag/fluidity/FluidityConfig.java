/*******************************************************************************
 * Copyright 2019, 2020 grondag
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package grondag.fluidity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.fabricmc.loader.api.FabricLoader;

@Internal
public class FluidityConfig  {
	private FluidityConfig() {}

	public static final boolean TRACE_DEVICE_CONNECTIONS;
	// TODO: use it or remove
	public static final int PER_TICK_BUDGET_MILLISECONDS;

	static void init() {
		// NOOP - loads
	}

	static {
		final File configDir = FabricLoader.getInstance().getConfigDirectory();
		if (!configDir.exists()) {
			Fluidity.LOG.warn("[Fluidity] Could not access configuration directory: " + configDir.getAbsolutePath());
		}

		final File configFile = new File(configDir, "fluidity.properties");
		final Properties properties = new Properties();

		if (configFile.exists()) {
			try (FileInputStream stream = new FileInputStream(configFile)) {
				properties.load(stream);
			} catch (final IOException e) {
				Fluidity.LOG.warn("[Fluidity] Could not read property file '" + configFile.getAbsolutePath() + "'", e);
			}
		}

		TRACE_DEVICE_CONNECTIONS = properties.computeIfAbsent("trace_device_connections", (a) -> "false").equals("true");
		PER_TICK_BUDGET_MILLISECONDS = readInteger(properties, "per_tick_budget_milliseconds", 10);

		try (FileOutputStream stream = new FileOutputStream(configFile)) {
			properties.store(stream, "Fluidity properties file");
		} catch (final IOException e) {
			Fluidity.LOG.warn("[Fluidity] Could not store property file '" + configFile.getAbsolutePath() + "'", e);
		}
	}

	static int readInteger(Properties properties, String name, int defaultValue) {
		try {
			return Integer.parseInt(properties.computeIfAbsent(name, (a) -> Integer.toString(defaultValue)).toString());
		} catch (final Exception e) {
			Fluidity.LOG.warn("[Fluidity] Invalid configuration value for '" + name + "'. Using default value.");
			return defaultValue;
		}
	}
}
