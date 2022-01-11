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

import grondag.fluidity.wip.api.transport.CarrierType;

public abstract class SubCarrier<T extends CarrierCostFunction> extends BasicCarrier<T> {
	private AggregateCarrier<T> parentCarrier = null;

	public SubCarrier(CarrierType carrierType) {
		super(carrierType);
	}

	public void setParent(AggregateCarrier<T> parent) {
		parentCarrier = parent;
	}

	public AggregateCarrier<T> getParent() {
		return parentCarrier;
	}

	@Override
	public LimitedCarrier<T> effectiveCarrier() {
		return parentCarrier == null ? this : parentCarrier;
	}
}
