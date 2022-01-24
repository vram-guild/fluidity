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

package grondag.fluidity.wip.api.transport;

import java.util.Random;
import java.util.function.Function;

import io.netty.util.internal.ThreadLocalRandom;
import org.jetbrains.annotations.ApiStatus.Experimental;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.device.DeviceComponentAccess;
import grondag.fluidity.api.device.DeviceComponentType;
import grondag.fluidity.api.storage.ArticleFunction;

@Experimental
public interface Carrier {
	CarrierType carrierType();

	default boolean isPointToPoint() {
		return false;
	}

	CarrierSession attach(Function<DeviceComponentType<?>, DeviceComponentAccess<?>> componentFunction);

	void startListening(CarrierListener listener, boolean sendNotifications);

	void stopListening(CarrierListener listener, boolean sendNotifications);

	void detach(CarrierSession node);

	int nodeCount();

	/**
	 * Use to iterates nodes. Iterator not exposed to avoid allocation. Not thread-safe.
	 * @param <T>
	 * @param index must be >=0 and < {@link #nodes()}
	 * @return
	 */
	<T extends CarrierNode> T nodeByIndex(int index);

	<V extends CarrierNode> V nodeByAddress(long address);

	default CarrierNode randomPeerOf(long requestorAddress) {
		final int nodeCount = nodeCount();
		CarrierNode result = CarrierNode.INVALID;
		int attempts = 0;

		if (nodeCount == 2) {
			result = nodeByIndex(0);

			if (result.nodeAddress() == requestorAddress) {
				result = nodeByIndex(1);
			}
		} else if (nodeCount > 2) {
			final Random r = ThreadLocalRandom.current();

			do {
				final CarrierNode node = nodeByIndex(r.nextInt(nodeCount));

				if (node.isValid() && node.nodeAddress() != requestorAddress) {
					result = node;
					break;
				}
			} while (++attempts < 4);
		}

		return result;
	}

	default CarrierNode supplierOf(Article article, long requestorAddress) {
		final int nodeCount = nodeCount();

		for (int i = 0; i < nodeCount; ++i) {
			final CarrierNode node = nodeByIndex(i);

			if (node.nodeAddress() != requestorAddress && node.getComponent(ArticleFunction.SUPPLIER_COMPONENT).get().canApply(article)) {
				return node;
			}
		}

		return CarrierNode.INVALID;
	}

	default CarrierNode consumerOf(Article article, long requestorAddress) {
		final int nodeCount = nodeCount();

		for (int i = 0; i < nodeCount; ++i) {
			final CarrierNode node = nodeByIndex(i);

			if (node.nodeAddress() != requestorAddress && node.getComponent(ArticleFunction.CONSUMER_COMPONENT).get().canApply(article)) {
				return node;
			}
		}

		return CarrierNode.INVALID;
	}

	Carrier EMPTY = new Carrier() {
		@Override
		public CarrierType carrierType() {
			return CarrierType.EMPTY;
		}

		@Override
		public CarrierSession attach(Function<DeviceComponentType<?>, DeviceComponentAccess<?>> componentFunction) {
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
		public <T extends CarrierNode> T nodeByIndex(int index) {
			throw new ArrayIndexOutOfBoundsException();
		}

		@SuppressWarnings("unchecked")
		@Override
		public <V extends CarrierNode> V nodeByAddress(long address) {
			return (V) CarrierSession.INVALID;
		}

		@Override
		public CarrierNode randomPeerOf(long nodeAddress) {
			return CarrierNode.INVALID;
		}
	};
}
