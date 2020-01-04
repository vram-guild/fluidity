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

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import grondag.fluidity.api.multiblock.MultiBlockMember;
import grondag.fluidity.base.multiblock.AbstractMultiBlock;
import grondag.fluidity.wip.api.transport.CarrierType;

@API(status = Status.EXPERIMENTAL)
public abstract class AbstractCarrierMultiBlock<T extends MultiBlockMember<T, U, SubCarrier>, U extends AbstractCarrierMultiBlock<T, U>> extends AbstractMultiBlock<T, U, SubCarrier> {
	protected final AggregateCarrier carrier;

	public AbstractCarrierMultiBlock(CarrierType carrierType) {
		carrier = createCarrier(carrierType);
	}

	protected AggregateCarrier createCarrier(CarrierType carrierType) {
		return new AggregateCarrier(carrierType);
	}

	@Override
	protected void beforeMemberRemoval(T member) {
		final SubCarrier c = member.getMemberComponent();

		if(c != null) {
			carrier.removeCarrier(c);
		}
	}

	@Override
	protected void afterMemberAddition(T member) {
		final SubCarrier c = member.getMemberComponent();

		if(c != null) {
			carrier.addCarrier(c);
		}
	}
}
