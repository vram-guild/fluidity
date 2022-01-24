/*
 * This file is part of Fluidity and is licensed to the project under
 * terms that are compatible with the GNU Lesser General Public License.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership and licensing.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package grondag.fluidity.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import dev.architectury.platform.Platform;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public class FluidityConfig {
	private FluidityConfig() { }

	public static final boolean TRACE_DEVICE_CONNECTIONS;
	// TODO: use it or remove
	public static final int PER_TICK_BUDGET_MILLISECONDS;

	static void init() {
		// NOOP - loads
	}

	static {
		final File configDir = Platform.getConfigFolder().toFile();

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
