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

import grondag.fluidity.api.device.DeviceComponent;
import grondag.fluidity.api.device.DeviceComponentType;

/**
 * Public view of a device component attached to a carrier.
 */
public interface CarrierNode {
	Carrier carrier();

	/**
	 * Similar to real-world media access control address: globally unique
	 * but no logical significant for routing purposes.
	 *
	 * Immutable once assigned. Does not persist beyond lifetime
	 * of connection. Addresses are not re-used within a game session.
	 *
	 * Implementations <em>must</em> obtain addresses from the AssignedNumberAuthority.
	 */
	long nodeAddress();

	boolean isValid();

	<T> DeviceComponent<T> getComponent(DeviceComponentType<T> componentType);

	default int flags() {
		return FLAG_ALLOW_STORAGE_ACCEPT_BROADCASTS | FLAG_ALLOW_STORAGE_SUPPLY_BROADCASTS;
	}

	default boolean hasFlag(int flag) {
		return (flags() & flag) == flag;
	}

	CarrierNode INVALID = CarrierSession.INVALID;

	int FLAG_ALLOW_STORAGE_ACCEPT_BROADCASTS = 1;
	int FLAG_ALLOW_STORAGE_SUPPLY_BROADCASTS = 2;
}
