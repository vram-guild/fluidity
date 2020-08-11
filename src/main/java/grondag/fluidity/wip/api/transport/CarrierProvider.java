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
import java.util.function.Function;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.minecraft.util.Identifier;

import grondag.fluidity.Fluidity;
import grondag.fluidity.api.article.ArticleType;
import grondag.fluidity.api.device.DeviceComponentAccess;
import grondag.fluidity.api.device.DeviceComponentRegistry;
import grondag.fluidity.api.device.DeviceComponentType;

@API(status = Status.EXPERIMENTAL)
public interface CarrierProvider {
	@Nullable Carrier getCarrier(CarrierType type);

	CarrierType getBestCarrier(ArticleType<?> type);

	Set<CarrierType> carrierTypes();

	/**
	 *
	 * @param type
	 * @param fromNode
	 * @param broadcastConsumer
	 * @param broadcastSupplier
	 * @return  Will return existing connection if node is already connected.
	 */
	default CarrierSession attachIfPresent(CarrierType type, Function<DeviceComponentType<?>, DeviceComponentAccess<?>> componentFunction) {
		final Carrier carrier = getCarrier(type);
		return carrier == null || carrier == Carrier.EMPTY ? CarrierSession.INVALID : carrier.attach(componentFunction);
	}

	default CarrierSession attachIfPresent(ArticleType<?> type, Function<DeviceComponentType<?>, DeviceComponentAccess<?>> componentFunction) {
		final CarrierType best = getBestCarrier(type);

		if(best == null || best == CarrierType.EMPTY) {
			return CarrierSession.INVALID;
		}

		return attachIfPresent(best, componentFunction);
	}

	CarrierProvider EMPTY = new CarrierProvider() {
		@Override
		public Carrier getCarrier(CarrierType type) {
			return Carrier.EMPTY;
		}

		@Override
		public CarrierType getBestCarrier(ArticleType<?> type) {
			return CarrierType.EMPTY;
		}

		@Override
		public Set<CarrierType> carrierTypes() {
			return Collections.emptySet();
		}
	};

	DeviceComponentType<CarrierProvider> CARRIER_PROVIDER_COMPONENT = DeviceComponentRegistry.INSTANCE.createComponent(new Identifier(Fluidity.MOD_ID, "carrier_provider"), EMPTY);
}
