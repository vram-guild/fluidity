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
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import grondag.fluidity.Fluidity;
import grondag.fluidity.api.device.DeviceComponentAccess;
import grondag.fluidity.api.device.DeviceComponentType;
import grondag.fluidity.base.storage.helper.ListenerSet;
import grondag.fluidity.wip.api.transport.Carrier;
import grondag.fluidity.wip.api.transport.CarrierConnector;
import grondag.fluidity.wip.api.transport.CarrierListener;
import grondag.fluidity.wip.api.transport.CarrierNode;
import grondag.fluidity.wip.api.transport.CarrierSession;
import grondag.fluidity.wip.api.transport.CarrierType;

public abstract class AggregateCarrier<T extends CarrierCostFunction> implements LimitedCarrier<T>, CarrierListener {
	protected final ListenerSet<CarrierListener> listeners = new ListenerSet<>(this::sendFirstListenerUpdate, this::sendLastListenerUpdate, this::onListenersEmpty);
	protected final Long2ObjectOpenHashMap<CarrierSession> nodeMap = new Long2ObjectOpenHashMap<>();
	protected final ObjectArrayList<CarrierSession> nodeList = new ObjectArrayList<>();
	protected final ObjectOpenHashSet<SubCarrier<T>> carriers = new ObjectOpenHashSet<>();

	protected final CarrierType carrierType;

	public AggregateCarrier(CarrierType carrierType) {
		this.carrierType = carrierType;
	}

	public void addCarrier(SubCarrier<T> carrier) {
		if(carriers.add(carrier)) {
			carrier.setParent(this);
			carrier.startListening(this, true);
		}
	}
	public void removeCarrier(SubCarrier<T> carrier) {
		if(carriers.contains(carrier)) {
			carriers.remove(carrier);
			carrier.stopListening(this, true);
			carrier.setParent(null);
		}
	}

	@Override
	public CarrierType carrierType() {
		return carrierType;
	}

	@Override
	public void disconnect(Carrier carrier, boolean didNotify, boolean isValid) {
		//TODO: implement and remove warning
		Fluidity.LOG.warn("Unhandled disconnect in aggregate carrier.");
	}

	@Override
	public void onAttach(Carrier carrier, CarrierSession node) {
		if(nodeMap.put(node.nodeAddress(), node) == null) {
			nodeList.add(node);
			listeners.forEach(l -> l.onAttach(this, node));
		}
	}

	@Override
	public void onDetach(Carrier carrier, CarrierSession node) {
		if(nodeMap.remove(node.nodeAddress()) != null) {
			nodeList.remove(node);
			listeners.forEach(l -> l.onDetach(this, node));
		}
	}

	@Override
	public CarrierSession attach(CarrierConnector fromDNode, Function<DeviceComponentType<?>, DeviceComponentAccess<?>> componentFunction) {
		throw new UnsupportedOperationException("Aggregate carriers do not support direct attachment");
	}

	@Override
	public void detach(CarrierSession node) {
		throw new UnsupportedOperationException("Aggregate carriers do not support direct attachment");
	}

	protected void sendFirstListenerUpdate(CarrierListener listener) {
		final int limit = nodeList.size();

		for (int i = 0; i < limit; ++i) {
			final CarrierSession a = nodeList.get(i);
			listener.onAttach(a.carrier(), a);
		}
	}

	protected void sendLastListenerUpdate(CarrierListener listener) {
		final int limit = nodeList.size();

		for (int i = 0; i < limit; ++i) {
			final CarrierSession a = nodeList.get(i);
			listener.onDetach(a.carrier(), a);
		}
	}

	protected void onListenersEmpty() {
		// NOOP
	}

	@Override
	public void startListening(CarrierListener listener, boolean sendNotifications) {
		listeners.startListening(listener, sendNotifications);

	}
	@Override
	public void stopListening(CarrierListener listener, boolean sendNotifications) {
		listeners.stopListening(listener, sendNotifications);
	}

	@Override
	public int nodeCount() {
		return nodeList.size();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V extends CarrierNode> V nodeByIndex(int index) {
		return (V) nodeList.get(index);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V extends CarrierNode> V nodeByAddress(long address) {
		return (V) nodeMap.getOrDefault(address, CarrierSession.INVALID);
	}
}
