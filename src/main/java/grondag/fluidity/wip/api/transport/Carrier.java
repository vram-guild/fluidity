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
import java.util.function.Function;

import grondag.fluidity.api.device.DeviceComponent;
import grondag.fluidity.api.device.DeviceComponentType;

/**
 *
 * Storages don't move articles to or from other storages.  This is done by a Transactor, which
 * must have access to each involved storage and ensures everything is handled properly or not.
 *
 * If and how a Transactor has access to a storage and the constraints for what may be moved,
 * from where to where, when, and how quickly, are potentially interesting gameplay mechanics
 * and thus worth modeling.
 *
 * Movement of articles within a single-block or ItemStack device aren't modeled by this API.
 * It is assumed such Transactor implementations will do whatever is expedient for their purposes because
 * such behavior doesn't generally have to be visible outside the device (only the outcomes.)
 *
 * Similarly, movement of articles within an compound storage device is also not modeled as transport.
 * Conceptually, the resources don't "move" at all and no consideration is given to location or movement,
 * only storage capacity, or other factors according to the policy of the compound storage implementation.
 * This is what a "compound" storage means.
 *
 * The compound device implementation is, by far, the simplest option if there is no desire to
 * model constraints for transport.  However, this breaks down when the system must connect with
 * a separate device or system, perhaps from a different mod.  It also does not work particularly
 * well with the conventional concept of machine buffers - what purpose does an input buffer serve
 * if everything is essentially all glommed together in a big chest and instantly accessible at all times?
 *
 * For these reasons and others, not all aggregate storage implementations will be compound. In particular,
 * storage networks that model the connection and transport of resources over long distances will want to
 * model some behaviors of similar real-world networks.  In such implementations, the storage itself should
 * still only be concerned with storing. Accept and supply requests for the aggregate storage should be
 * front-ended by a Transactor that orchestrates all the necessary transfers and only commits them
 * if the result system state abides by policy and is valid (no duplication or loss).
 *
 * When articles move between two devices, they do so via a Carrier.  Every device connected to the
 * carrier is assigned a Port - which may also model logical or physical constraints of the connection
 * if desired for game play.  Ports may be designed to expose more than one storage and/or more than
 * one type of storage in the same device.
 *
 * Note that a Storage can be attached to the same carrier via more than one port, which also makes
 * ports useful as a way to disambiguate the physical routing of transport, when it matters for the implementation.
 *
 * A Carrier knows which storages are attached to it via which ports, and can provide information about them.
 * Devices that embody a Transactor and which are physically attached should generally be able to request
 * transfers to and from ports on the local carrier. Such transfers require no routing, as carriers act like busses.
 *
 * Often, it will be desirable to move resources across more than one carrier.  This requires two things:
 *
 * 1) A bridge or switch device that connects two carriers. Such a device has a port connected to
 * both carriers and can handle requests to move articles from one to the other and forward them to
 * their next destination port.
 *
 * 2) Some sort of routing logic to determine which path (carriers and ports) should be taken to handle
 * the transfer.  This will be highly specific to the implementation and for "realism" and gameplay purposes
 * will generally require the existence of some intelligent controller, which could be a central devices (the norm)
 * or distributed across multiple smart devices, or happen via mystical power.  Point is - the transactor
 * in such cases must have visibility and access to all of the nework needed to plan and execute the transfer,
 * and it is likely such a system will have some centralization.
 *
 * The simplest Carrier is a direct connection between two adjacent devices.  The carrier in such a
 * case is virtual and not particularly meaningful, but givens transactors a consistent pattern for conducting transfers.
 *
 * Carrier device implementations control how a carrier may be formed, but generally point-to-point
 * and bus topologies will be the easiest to implement.  (Mesh topologies may be physically possible
 * but can behave like a bus for gameplay purposes.) Star topologies, if wanted are probably best modeled
 * as multiple point-to-point carriers connecting to a faster or larger carrier device that acts as a switch.
 *
 */
public interface Carrier {
	CarrierType carrierType();

	default boolean isPointToPoint() {
		return false;
	}

	static Carrier p2p(CarrierConnector a, CarrierConnector b) {
		return null;
	}

	CarrierSession attach(CarrierConnector fromNode, Function<DeviceComponentType<?>, DeviceComponent<?>> componentFunction);

	void startListening(CarrierListener listener, boolean sendNotifications);

	void stopListening(CarrierListener listener, boolean sendNotifications);

	void detach(CarrierSession node);

	int nodeCount();

	Iterable<? extends CarrierNode> nodes();

	Carrier EMPTY = new Carrier() {
		@Override
		public CarrierType carrierType() {
			return CarrierType.EMPTY;
		}

		@Override
		public CarrierSession attach(CarrierConnector fromNode, Function<DeviceComponentType<?>, DeviceComponent<?>> componentFunction) {
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
		public Iterable<? extends CarrierSession> nodes() {
			return Collections.emptyList();
		}
	};
}
