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

package grondag.fluidity.wip.base.transport;

import java.util.function.Function;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import grondag.fluidity.api.device.DeviceComponentAccess;
import grondag.fluidity.api.device.DeviceComponentType;
import grondag.fluidity.base.storage.helper.ListenerSet;
import grondag.fluidity.impl.Fluidity;
import grondag.fluidity.wip.api.transport.Carrier;
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
		if (carriers.add(carrier)) {
			carrier.setParent(this);
			carrier.startListening(this, true);
		}
	}

	public void removeCarrier(SubCarrier<T> carrier) {
		if (carriers.contains(carrier)) {
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
		if (nodeMap.put(node.nodeAddress(), node) == null) {
			nodeList.add(node);
			listeners.forEach(l -> l.onAttach(this, node));
		}
	}

	@Override
	public void onDetach(Carrier carrier, CarrierSession node) {
		if (nodeMap.remove(node.nodeAddress()) != null) {
			nodeList.remove(node);
			listeners.forEach(l -> l.onDetach(this, node));
		}
	}

	@Override
	public CarrierSession attach(Function<DeviceComponentType<?>, DeviceComponentAccess<?>> componentFunction) {
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
