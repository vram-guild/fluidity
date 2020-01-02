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
import grondag.fluidity.api.storage.ArticleConsumer;
import grondag.fluidity.api.storage.ArticleSupplier;
import grondag.fluidity.api.transact.TransactionContext;
import grondag.fluidity.api.transact.TransactionParticipant.TransactionDelegate;
import grondag.fluidity.wip.api.transport.Carrier;
import grondag.fluidity.wip.api.transport.CarrierSession;
import grondag.fluidity.wip.api.transport.StorageConnection;

class CarrierSessionImpl implements CarrierSession, TransactionDelegate {
	final long address = AssignedNumbersAuthority.createCarrierAddress();
	final Function<DeviceComponentType<?>, DeviceComponent<?>> componentFunction;
	final BasicCarrier carrier;
	protected boolean isOpen = true;

	CarrierSessionImpl(BasicCarrier carrier, Function<DeviceComponentType<?>, DeviceComponent<?>> componentFunction) {
		this.carrier = carrier;
		this.componentFunction = componentFunction;
	}

	@Override
	public long nodeAddress() {
		return address;
	}

	@Override
	public boolean isValid() {
		return isOpen;
	}

	protected final ArticleConsumer broadcastConsumer = new BroadcastConsumer(this);

	@Override
	public ArticleConsumer broadcastConsumer() {
		return isOpen ? broadcastConsumer : ArticleConsumer.FULL;
	}

	protected final ArticleSupplier broadcastSupploer = new BroadcastSupplier(this);

	@Override
	public ArticleSupplier broadcastSupplier() {
		return isOpen ? broadcastSupploer : ArticleSupplier.EMPTY;
	}

	@Override
	public StorageConnection connect(long remoteAddress) {
		return null;
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

		// TODO Auto-generated method stub
		return c -> {};
	}

	@Override
	public Carrier carrier() {
		return carrier.effectiveCarrier();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> DeviceComponent<T> getComponent(DeviceComponentType<T> componentType) {
		return (DeviceComponent<T>) componentFunction.apply(componentType);
	}
}
