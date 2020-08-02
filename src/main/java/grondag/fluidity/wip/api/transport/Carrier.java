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

import java.util.function.Function;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import grondag.fluidity.api.device.DeviceComponentAccess;
import grondag.fluidity.api.device.DeviceComponentType;


@API(status = Status.EXPERIMENTAL)
public interface Carrier {
	CarrierType carrierType();

	default boolean isPointToPoint() {
		return false;
	}

	CarrierSession attach(CarrierConnector fromNode, Function<DeviceComponentType<?>, DeviceComponentAccess<?>> componentFunction);

	void startListening(CarrierListener listener, boolean sendNotifications);

	void stopListening(CarrierListener listener, boolean sendNotifications);

	void detach(CarrierSession node);

	int nodeCount();

	/**
	 * Use to iterates nodes. Iterator not exposed to avoid allocation. Not thread-safe.
	 * @param <T>
	 * @param index must be >=0 and < {@link #nodes()}
	 * @return
	 */
	<T extends CarrierNode> T nodeByIndex(int index);

	Carrier EMPTY = new Carrier() {
		@Override
		public CarrierType carrierType() {
			return CarrierType.EMPTY;
		}

		@Override
		public CarrierSession attach(CarrierConnector fromNode, Function<DeviceComponentType<?>, DeviceComponentAccess<?>> componentFunction) {
			return CarrierSession.INVALID;
		}

		@Override
		public void startListening(CarrierListener listener, boolean sendNotifications) {
			// NOOP
		}

		@Override
		public void stopListening(CarrierListener listener, boolean sendNotifications) {
			// NOOP
		}

		@Override
		public void detach(CarrierSession node) {
			// NOOP
		}

		@Override
		public int nodeCount() {
			return 0;
		}

		@Override
		public <T extends CarrierNode> T nodeByIndex(int index) {
			throw new ArrayIndexOutOfBoundsException();
		}
	};
}
