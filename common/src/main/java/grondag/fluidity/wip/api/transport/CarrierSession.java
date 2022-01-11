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
