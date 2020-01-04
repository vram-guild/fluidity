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

import java.util.Iterator;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.fraction.Fraction;
import grondag.fluidity.api.fraction.FractionView;
import grondag.fluidity.api.fraction.MutableFraction;
import grondag.fluidity.api.storage.ArticleFunction;
import grondag.fluidity.api.storage.Storage;
import grondag.fluidity.wip.api.transport.Carrier;
import grondag.fluidity.wip.api.transport.CarrierNode;
import grondag.fluidity.wip.api.transport.CarrierSession;

public class BroadcastSupplier implements ArticleFunction {
	private final CarrierSession fromNode;

	public BroadcastSupplier(CarrierSession fromNode) {
		this.fromNode = fromNode;
	}

	@Override
	public long apply(Article item, long count, boolean simulate) {
		final Carrier carrier = fromNode.carrier();

		if(carrier.nodeCount() <= 1) {
			return 0;
		}

		long result = 0;

		final Iterator<? extends CarrierNode> it = carrier.nodes().iterator();

		while(it.hasNext()) {
			final CarrierNode n = it.next();

			if(n != fromNode && n.hasFlag(CarrierNode.FLAG_ALLOW_STORAGE_SUPPLY_BROADCASTS)) {
				final ArticleFunction s = n.getComponent(Storage.STORAGE_COMPONENT).get().getSupplier();
				result += s.apply(item, count - result, simulate);

				if(result >= count) {
					break;
				}
			}
		}

		return result;
	}

	protected final MutableFraction calc = new MutableFraction();
	protected final MutableFraction result = new MutableFraction();

	@Override
	public FractionView apply(Article item, FractionView volume, boolean simulate) {
		final Carrier carrier = fromNode.carrier();

		if(carrier.nodeCount() <= 1) {
			return Fraction.ZERO;
		}

		result.set(0);
		calc.set(volume);

		final Iterator<? extends CarrierNode> it = carrier.nodes().iterator();

		while(it.hasNext()) {
			final CarrierNode n = it.next();

			if(n != fromNode && n.hasFlag(CarrierNode.FLAG_ALLOW_STORAGE_SUPPLY_BROADCASTS)) {
				final ArticleFunction s = n.getComponent(Storage.STORAGE_COMPONENT).get().getSupplier();
				final FractionView amt = s.apply(item, calc, simulate);

				if(!amt.isZero()) {
					result.add(amt);
					calc.subtract(amt);

					if(result.isGreaterThankOrEqual(volume)) {
						break;
					}
				}
			}
		}

		return result;
	}

	@Override
	public long apply(Article item, long numerator, long divisor, boolean simulate) {
		final Carrier carrier = fromNode.carrier();

		if(carrier.nodeCount() <= 1) {
			return 0;
		}


		long result = 0;

		final Iterator<? extends CarrierNode> it = carrier.nodes().iterator();

		while(it.hasNext()) {
			final CarrierNode n = it.next();

			if(n != fromNode && n.hasFlag(CarrierNode.FLAG_ALLOW_STORAGE_SUPPLY_BROADCASTS)) {
				final ArticleFunction s = n.getComponent(Storage.STORAGE_COMPONENT).get().getSupplier();
				result += s.apply(item, numerator - result, divisor, simulate);

				if(result >= numerator) {
					break;
				}
			}
		}

		return result;
	}

	@Override
	public TransactionDelegate getTransactionDelegate() {
		//TODO: implement
		return ctx -> null;
	}
}
