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
package grondag.fluidity.wip.base.transport;

import java.util.function.Function;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import grondag.fluidity.api.device.DeviceComponentAccess;
import grondag.fluidity.api.device.DeviceComponentType;
import grondag.fluidity.base.storage.helper.ListenerSet;
import grondag.fluidity.wip.api.transport.CarrierConnector;
import grondag.fluidity.wip.api.transport.CarrierListener;
import grondag.fluidity.wip.api.transport.CarrierSession;
import grondag.fluidity.wip.api.transport.CarrierType;

public abstract class BasicCarrier<T extends CarrierCostFunction> implements LimitedCarrier<T> {
	protected final CarrierType carrierType;

	public BasicCarrier(CarrierType carrierType) {
		this.carrierType = carrierType;
	}

	protected final ListenerSet<CarrierListener> listeners = new ListenerSet<>(this::sendFirstListenerUpdate, this::sendLastListenerUpdate, this::onListenersEmpty);

	protected final Long2ObjectOpenHashMap<CarrierSession> nodes = new Long2ObjectOpenHashMap<>();

	protected void sendFirstListenerUpdate(CarrierListener listener) {
		nodes.values().forEach(a -> listener.onAttach(BasicCarrier.this, a));
	}

	protected void sendLastListenerUpdate(CarrierListener listener) {
		nodes.values().forEach(a -> listener.onDetach(BasicCarrier.this, a));
	}

	protected void onListenersEmpty() {
		// NOOP
	}

	@Override
	public CarrierType carrierType() {
		return carrierType;
	}

	@Override
	public void startListening(CarrierListener listener, boolean sendNotifications) {
		listeners.startListening(listener, sendNotifications);
	}

	@Override
	public void stopListening(CarrierListener listener, boolean sendNotifications) {
		listeners.stopListening(listener, sendNotifications);
	}

	protected CarrierSession createSession(Function<DeviceComponentType<?>, DeviceComponentAccess<?>> componentFunction) {
		return new BasicCarrierSession<>(this, componentFunction);
	}

	@Override
	public CarrierSession attach(CarrierConnector fromNode, Function<DeviceComponentType<?>, DeviceComponentAccess<?>> componentFunction) {
		final CarrierSession result = createSession(componentFunction);

		if(nodes.put(result.nodeAddress(), result) == null) {
			listeners.forEach(l -> l.onAttach(this, result));
		}

		return result;
	}

	@Override
	public void detach(CarrierSession node) {
		if(nodes.remove(node.nodeAddress()) != null) {
			listeners.forEach(l -> l.onDetach(this, node));
		}
	}

	@Override
	public int nodeCount() {
		return nodes.size();
	}

	@Override
	public Iterable<? extends CarrierSession> nodes() {
		return nodes.values();
	}

	public LimitedCarrier<T> effectiveCarrier() {
		return this;
	}

	@Override
	public abstract T costFunction();
}
