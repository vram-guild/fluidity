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

package grondag.fluidity.wip.api.transport;

import java.util.Collections;
import java.util.Set;
import java.util.function.Function;

import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.Nullable;

import net.minecraft.resources.ResourceLocation;

import grondag.fluidity.api.article.ArticleType;
import grondag.fluidity.api.device.DeviceComponentAccess;
import grondag.fluidity.api.device.DeviceComponentRegistry;
import grondag.fluidity.api.device.DeviceComponentType;
import grondag.fluidity.impl.Fluidity;

@Experimental
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

		if (best == null || best == CarrierType.EMPTY) {
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

	DeviceComponentType<CarrierProvider> CARRIER_PROVIDER_COMPONENT = DeviceComponentRegistry.INSTANCE.createComponent(new ResourceLocation(Fluidity.MOD_ID, "carrier_provider"), EMPTY);
}
