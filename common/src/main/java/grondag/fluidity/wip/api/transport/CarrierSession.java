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

import org.jetbrains.annotations.ApiStatus.Experimental;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.device.DeviceComponentAccess;
import grondag.fluidity.api.device.DeviceComponentType;
import grondag.fluidity.api.storage.ArticleFunction;

/**
 * Visible to the node that obtained the carrier.
 *
 */
@Experimental
public interface CarrierSession extends CarrierNode {
	ArticleFunction broadcastConsumer();

	ArticleFunction broadcastSupplier();

	default CarrierNode randomPeer() {
		return carrier().randomPeerOf(nodeAddress());
	}

	default CarrierNode supplierOf(Article article) {
		return carrier().supplierOf(article, nodeAddress());
	}

	default CarrierNode consumerOf(Article article) {
		return carrier().consumerOf(article, nodeAddress());
	}

	void close();

	CarrierSession INVALID = new CarrierSession() {
		@Override
		public Carrier carrier() {
			return Carrier.EMPTY;
		}

		@Override
		public long nodeAddress() {
			return -1;
		}

		@Override
		public boolean isValid() {
			return false;
		}

		@Override
		public ArticleFunction broadcastConsumer() {
			return ArticleFunction.ALWAYS_RETURN_ZERO;
		}

		@Override
		public ArticleFunction broadcastSupplier() {
			return ArticleFunction.ALWAYS_RETURN_ZERO;
		}

		@Override
		public void close() {
			// NOOP
		}

		@Override
		public <T> DeviceComponentAccess<T> getComponent(DeviceComponentType<T> componentType) {
			return componentType.getAbsentAccess();
		}

		@Override
		public CarrierNode randomPeer() {
			return CarrierNode.INVALID;
		}
	};
}
