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

import java.util.function.Consumer;
import java.util.function.Function;

import grondag.fluidity.api.device.DeviceComponentAccess;
import grondag.fluidity.api.device.DeviceComponentType;
import grondag.fluidity.api.storage.ArticleFunction;
import grondag.fluidity.api.transact.TransactionContext;
import grondag.fluidity.api.transact.TransactionParticipant.TransactionDelegate;

public class BasicCarrierSession<T extends CarrierCostFunction> implements LimitedCarrierSession<T>, TransactionDelegate {
	protected final long address = AssignedNumbersAuthority.createCarrierAddress();
	protected final Function<DeviceComponentType<?>, DeviceComponentAccess<?>> componentFunction;
	protected final BasicCarrier<T> carrier;
	protected final BroadcastConsumer<T> broadcastConsumer;
	protected final BroadcastSupplier<T> broadcastSupplier;
	protected boolean isOpen = true;

	public BasicCarrierSession(BasicCarrier<T> carrier, Function<DeviceComponentType<?>, DeviceComponentAccess<?>> componentFunction) {
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
		return isOpen ? broadcastConsumer : ArticleFunction.ALWAYS_RETURN_ZERO;
	}

	@Override
	public ArticleFunction broadcastSupplier() {
		return isOpen ? broadcastSupplier : ArticleFunction.ALWAYS_RETURN_ZERO;
	}

	@Override
	public void close() {
		if (isOpen) {
			isOpen = false;
			carrier.detach(this);
		}
	}

	@Override
	public Consumer<TransactionContext> prepareRollback(TransactionContext context) {
		if (!isOpen) {
			return c -> { };
		}

		// TODO: Implement proper rollback
		return c -> { };
	}

	@Override
	public LimitedCarrier<T> carrier() {
		return carrier.effectiveCarrier();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V> DeviceComponentAccess<V> getComponent(DeviceComponentType<V> componentType) {
		return (DeviceComponentAccess<V>) componentFunction.apply(componentType);
	}
}
