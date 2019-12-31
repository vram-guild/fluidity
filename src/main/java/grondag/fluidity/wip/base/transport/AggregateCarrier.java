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

import java.util.function.Supplier;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import grondag.fluidity.api.storage.ArticleConsumer;
import grondag.fluidity.api.storage.ArticleSupplier;
import grondag.fluidity.base.storage.component.ListenerSet;
import grondag.fluidity.wip.api.transport.Carrier;
import grondag.fluidity.wip.api.transport.CarrierConnector;
import grondag.fluidity.wip.api.transport.CarrierListener;
import grondag.fluidity.wip.api.transport.CarrierSession;
import grondag.fluidity.wip.api.transport.CarrierType;

public class AggregateCarrier implements Carrier, CarrierListener {
	protected final ListenerSet<CarrierListener> listeners = new ListenerSet<>(this::sendFirstListenerUpdate, this::sendLastListenerUpdate, this::onListenersEmpty);
	protected final Long2ObjectOpenHashMap<CarrierSession> nodes = new Long2ObjectOpenHashMap<>();
	protected final ObjectOpenHashSet<SubCarrier> carriers = new ObjectOpenHashSet<>();

	protected final CarrierType carrierType;

	protected AggregateCarrier(CarrierType carrierType) {
		this.carrierType = carrierType;
	}

	public void addCarrier(SubCarrier carrier) {
		if(carriers.add(carrier)) {
			carrier.parentCarrier = this;
			carrier.startListening(this, true);
		}
	}
	public void removeCarrier(SubCarrier carrier) {
		if(carriers.contains(carrier)) {
			carriers.remove(carrier);
			carrier.stopListening(this, true);
			carrier.parentCarrier = null;
		}
	}

	@Override
	public CarrierType carrierType() {
		return carrierType;
	}

	@Override
	public void disconnect(Carrier carrier, boolean didNotify, boolean isValid) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAttach(Carrier carrier, CarrierSession node) {
		if(nodes.put(node.nodeAddress(), node) == null) {
			listeners.forEach(l -> l.onAttach(this, node));
		}
	}

	@Override
	public void onDetach(Carrier carrier, CarrierSession node) {
		if(nodes.remove(node.nodeAddress()) != null) {
			listeners.forEach(l -> l.onDetach(this, node));
		}
	}

	@Override
	public CarrierSession attach(CarrierConnector fromDNode, Supplier<ArticleConsumer> nodeConsumerFactor, Supplier<ArticleSupplier> nodeSupplierFactory) {
		throw new UnsupportedOperationException("Aggregate carriers do not support direct attachment");
	}

	@Override
	public void detach(CarrierSession node) {
		throw new UnsupportedOperationException("Aggregate carriers do not support direct attachment");
	}

	protected void sendFirstListenerUpdate(CarrierListener listener) {
		nodes.values().forEach(a -> listener.onAttach(a.carrier(), a));
	}

	protected void sendLastListenerUpdate(CarrierListener listener) {
		nodes.values().forEach(a -> listener.onDetach(a.carrier(), a));
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
		return nodes.size();
	}
	@Override
	public Iterable<? extends CarrierSession> nodes() {
		return nodes.values();
	}
}
