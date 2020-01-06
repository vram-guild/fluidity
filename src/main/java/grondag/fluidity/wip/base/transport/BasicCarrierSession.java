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

import java.util.function.Consumer;
import java.util.function.Function;

import grondag.fluidity.api.device.DeviceComponent;
import grondag.fluidity.api.device.DeviceComponentType;
import grondag.fluidity.api.storage.ArticleFunction;
import grondag.fluidity.api.transact.TransactionContext;
import grondag.fluidity.api.transact.TransactionParticipant.TransactionDelegate;

public class BasicCarrierSession<T extends CarrierCostFunction> implements LimitedCarrierSession<T>, TransactionDelegate {
	protected final long address = AssignedNumbersAuthority.createCarrierAddress();
	protected final Function<DeviceComponentType<?>, DeviceComponent<?>> componentFunction;
	protected final BasicCarrier<T> carrier;
	protected final BroadcastConsumer<T> broadcastConsumer;
	protected final BroadcastSupplier<T> broadcastSupplier;
	protected boolean isOpen = true;

	public BasicCarrierSession(BasicCarrier<T> carrier, Function<DeviceComponentType<?>, DeviceComponent<?>> componentFunction) {
		this.carrier = carrier;
		this.componentFunction = componentFunction;
		broadcastConsumer = createBroadcastConsumer();
		broadcastSupplier = createBroadcastSupplier();
	}

	protected BroadcastConsumer<T> createBroadcastConsumer() {
		return new BroadcastConsumer<>(this);
	}

	protected BroadcastSupplier<T> createBroadcastSupplier() {
		return new BroadcastSupplier<>(this);
	}

	@Override
	public long nodeAddress() {
		return address;
	}

	@Override
	public boolean isValid() {
		return isOpen;
	}

	@Override
	public ArticleFunction broadcastConsumer() {
		return isOpen ? broadcastConsumer : ArticleFunction.FULL;
	}

	@Override
	public ArticleFunction broadcastSupplier() {
		return isOpen ? broadcastSupplier : ArticleFunction.EMPTY;
	}

	@Override
	public void close() {
		if(isOpen) {
			isOpen = false;
			carrier.detach(this);
		}
	}

	@Override
	public Consumer<TransactionContext> prepareRollback(TransactionContext context) {
		if(!isOpen) {
			return c -> {};
		}

		// TODO: Implement proper rollback
		return c -> {};
	}

	@Override
	public LimitedCarrier<T> carrier() {
		return carrier.effectiveCarrier();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V> DeviceComponent<V> getComponent(DeviceComponentType<V> componentType) {
		return (DeviceComponent<V>) componentFunction.apply(componentType);
	}
}
