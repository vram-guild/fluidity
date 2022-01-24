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

	public static SingleCarrierProvider of(BasicCarrier<?> carrier) {
		return new SingleCarrierProvider(carrier);
	}
}
