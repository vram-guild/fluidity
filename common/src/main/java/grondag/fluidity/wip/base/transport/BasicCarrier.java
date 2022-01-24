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

import grondag.fluidity.api.device.DeviceComponentAccess;
import grondag.fluidity.api.device.DeviceComponentType;
import grondag.fluidity.base.storage.helper.ListenerSet;
import grondag.fluidity.wip.api.transport.CarrierListener;
import grondag.fluidity.wip.api.transport.CarrierNode;
import grondag.fluidity.wip.api.transport.CarrierSession;
import grondag.fluidity.wip.api.transport.CarrierType;

public abstract class BasicCarrier<T extends CarrierCostFunction> implements LimitedCarrier<T> {
	protected final CarrierType carrierType;

	public BasicCarrier(CarrierType carrierType) {
		this.carrierType = carrierType;
	}

	protected final ListenerSet<CarrierListener> listeners = new ListenerSet<>(this::sendFirstListenerUpdate, this::sendLastListenerUpdate, this::onListenersEmpty);

	protected final Long2ObjectOpenHashMap<CarrierSession> nodeMap = new Long2ObjectOpenHashMap<>();
	protected final ObjectArrayList<CarrierSession> nodeList = new ObjectArrayList<>();

	protected void sendFirstListenerUpdate(CarrierListener listener) {
		final int limit = nodeList.size();

		for (int i = 0; i < limit; ++i) {
			final CarrierSession a = nodeList.get(i);
			listener.onAttach(this, a);
		}
	}

	protected void sendLastListenerUpdate(CarrierListener listener) {
		final int limit = nodeList.size();

		for (int i = 0; i < limit; ++i) {
			final CarrierSession a = nodeList.get(i);
			listener.onDetach(this, a);
		}
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
	public CarrierSession attach(Function<DeviceComponentType<?>, DeviceComponentAccess<?>> componentFunction) {
		final CarrierSession result = createSession(componentFunction);

		if (nodeMap.put(result.nodeAddress(), result) == null) {
			nodeList.add(result);
			listeners.forEach(l -> l.onAttach(this, result));
		}

		return result;
	}

	@Override
	public void detach(CarrierSession node) {
		if (nodeMap.remove(node.nodeAddress()) != null) {
			nodeList.remove(node);
			listeners.forEach(l -> l.onDetach(this, node));
		}
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

	public LimitedCarrier<T> effectiveCarrier() {
		return this;
	}

	@Override
	public abstract T costFunction();
}
