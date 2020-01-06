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
package grondag.fluidity.wip.api.transport;

import java.util.Collections;
import java.util.Set;

import net.minecraft.util.Identifier;

import grondag.fluidity.Fluidity;
import grondag.fluidity.api.article.ArticleType;
import grondag.fluidity.api.device.DeviceComponentRegistry;
import grondag.fluidity.api.device.DeviceComponentType;

/**
 * A device component that may attach to a carrier.
 */
@FunctionalInterface
public interface CarrierConnector {
	Set<ArticleType<?>> articleTypes();

	default String name() {
		return "Unknown";
	}

	CarrierConnector EMPTY  = Collections::emptySet;

	DeviceComponentType<CarrierConnector> CARRIER_CONNECTOR_COMPONENT = DeviceComponentRegistry.INSTANCE.createComponent(new Identifier(Fluidity.MOD_ID, "carrier_connector"), EMPTY);
}