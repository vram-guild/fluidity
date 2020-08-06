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

import grondag.fluidity.Fluidity;
import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.fraction.Fraction;
import grondag.fluidity.api.fraction.MutableFraction;
import grondag.fluidity.api.storage.ArticleFunction;
import grondag.fluidity.api.storage.Store;
import grondag.fluidity.api.transact.Transaction;
import grondag.fluidity.wip.api.transport.CarrierNode;

public class BroadcastSupplier<T extends CarrierCostFunction> implements ArticleFunction {
	private final LimitedCarrierSession<T> fromNode;

	public BroadcastSupplier(LimitedCarrierSession<T> fromNode) {
		this.fromNode = fromNode;
	}

	@Override
	public long apply(Article item, long count, boolean simulate) {
		final LimitedCarrier<T> carrier = fromNode.carrier();

		final int nodeCount = carrier.nodeCount();

		if(nodeCount <= 1) {
			return 0;
		}

		try (Transaction tx = Transaction.open()) {
			count = carrier.costFunction().apply(fromNode, item, count, simulate);

			long result = 0;

			for (int i = 0; i < nodeCount; ++i) {
				final CarrierNode n = carrier.nodeByIndex(i);

				if(n != fromNode && n.hasFlag(CarrierNode.FLAG_ACCEPT_SUPPLIER_BROADCASTS)) {
					final ArticleFunction s = n.getComponent(Store.STORAGE_COMPONENT).get().getSupplier();
					tx.enlist(s); // allow for implementations that do not self-enlist
					result += s.apply(item, count - result, simulate);

					if(result >= count) {
						break;
					}
				}
			}

			return result;
		} catch(final Exception e) {
			Fluidity.LOG.warn("Unlable to complete carrier broadcast supply request due to exception.", e);
			return 0;
		}
	}

	protected final MutableFraction calc = new MutableFraction();
	protected final MutableFraction result = new MutableFraction();

	@Override
	public Fraction apply(Article item, Fraction volume, boolean simulate) {
		final LimitedCarrier<T> carrier = fromNode.carrier();

		final int nodeCount = carrier.nodeCount();

		if(nodeCount <= 1) {
			return Fraction.ZERO;
		}

		try (Transaction tx = Transaction.open()) {
			// note that cost function is self-enlisting
			volume = carrier.costFunction().apply(fromNode, item, volume, simulate);

			result.set(0);
			calc.set(volume);

			for (int i = 0; i < nodeCount; ++i) {
				final CarrierNode n = carrier.nodeByIndex(i);

				if(n != fromNode && n.hasFlag(CarrierNode.FLAG_ACCEPT_SUPPLIER_BROADCASTS)) {
					final ArticleFunction s = n.getComponent(Store.STORAGE_COMPONENT).get().getSupplier();
					tx.enlist(s); // allow for implementations that do not self-enlist
					final Fraction amt = s.apply(item, calc, simulate);

					if(!amt.isZero()) {
						result.add(amt);
						calc.subtract(amt);

						if(result.isGreaterThanOrEqual(volume)) {
							break;
						}
					}
				}
			}

			tx.commit();
			return result;
		} catch(final Exception e) {
			Fluidity.LOG.warn("Unlable to complete carrier broadcast supply request due to exception.", e);
			return Fraction.ZERO;
		}
	}

	@Override
	public long apply(Article item, long numerator, long divisor, boolean simulate) {
		final LimitedCarrier<T> carrier = fromNode.carrier();

		final int nodeCount = carrier.nodeCount();

		if(nodeCount <= 1) {
			return 0;
		}

		try (Transaction tx = Transaction.open()) {
			// note that cost function is self-enlisting
			numerator = carrier.costFunction().apply(fromNode, item, numerator, divisor, simulate);

			long result = 0;

			for (int i = 0; i < nodeCount; ++i) {
				final CarrierNode n = carrier.nodeByIndex(i);

				if(n != fromNode && n.hasFlag(CarrierNode.FLAG_ACCEPT_SUPPLIER_BROADCASTS)) {
					final ArticleFunction s = n.getComponent(Store.STORAGE_COMPONENT).get().getSupplier();
					tx.enlist(s); // allow for implementations that do not self-enlist
					result += s.apply(item, numerator - result, divisor, simulate);

					if(result >= numerator) {
						break;
					}
				}
			}

			tx.commit();
			return result;
		} catch(final Exception e) {
			Fluidity.LOG.warn("Unlable to complete carrier broadcast supply request due to exception.", e);
			return 0;
		}
	}

	/** All transaction handling is in nodes and cost function.  Should never be used */
	@Override
	public TransactionDelegate getTransactionDelegate() {
		assert false : "getTransactionDelegate called for BroadcastSupplier";
	return TransactionDelegate.IGNORE;
	}

	@Override
	public boolean isSelfEnlisting() {
		return true;
	}
}
