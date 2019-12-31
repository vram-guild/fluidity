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

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import grondag.fluidity.api.article.ArticleType;
import grondag.fluidity.wip.api.transport.Carrier;
import grondag.fluidity.wip.api.transport.CarrierProvider;
import grondag.fluidity.wip.api.transport.CarrierType;

public class SingleCarrierProvider implements CarrierProvider {
	protected final Carrier carrier;

	public SingleCarrierProvider(Carrier carrier) {
		this.carrier = carrier;
	}

	@Override
	public Carrier getCarrier(CarrierType type) {
		return type == carrier.carrierType() ? carrier : Carrier.EMPTY;
	}

	@Override
	public CarrierType getBestCarrier(ArticleType<?> type) {
		return carrier.carrierType().canCarry(type) ? carrier.carrierType() : CarrierType.EMPTY;
	}

	@Override
	public Set<CarrierType> carrierTypes() {
		return ImmutableSet.of(carrier.carrierType());
	}

	public static SingleCarrierProvider of(BasicCarrier carrier) {
		return new SingleCarrierProvider(carrier);
	}
}
