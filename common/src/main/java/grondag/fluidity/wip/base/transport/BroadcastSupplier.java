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

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.article.ArticleType;
import grondag.fluidity.api.fraction.Fraction;
import grondag.fluidity.api.fraction.MutableFraction;
import grondag.fluidity.api.storage.ArticleFunction;
import grondag.fluidity.api.transact.Transaction;
import grondag.fluidity.impl.Fluidity;
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

		if (nodeCount <= 1) {
			return 0;
		}

		try (Transaction tx = Transaction.open()) {
			count = carrier.costFunction().apply(fromNode, item, count, simulate);

			long result = 0;

			for (int i = 0; i < nodeCount; ++i) {
				final CarrierNode n = carrier.nodeByIndex(i);

				if (n != fromNode && n.hasFlag(CarrierNode.FLAG_ACCEPT_SUPPLIER_BROADCASTS)) {
					final ArticleFunction s = n.getComponent(ArticleFunction.SUPPLIER_COMPONENT).get();
					tx.enlist(s); // allow for implementations that do not self-enlist
					result += s.apply(item, count - result, simulate);

					if (result >= count) {
						break;
					}
				}
			}

			return result;
		} catch (final Exception e) {
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

		if (nodeCount <= 1) {
			return Fraction.ZERO;
		}

		try (Transaction tx = Transaction.open()) {
			// note that cost function is self-enlisting
			volume = carrier.costFunction().apply(fromNode, item, volume, simulate);

			result.set(0);
			calc.set(volume);

			for (int i = 0; i < nodeCount; ++i) {
				final CarrierNode n = carrier.nodeByIndex(i);

				if (n != fromNode && n.hasFlag(CarrierNode.FLAG_ACCEPT_SUPPLIER_BROADCASTS)) {
					final ArticleFunction s = n.getComponent(ArticleFunction.SUPPLIER_COMPONENT).get();
					tx.enlist(s); // allow for implementations that do not self-enlist
					final Fraction amt = s.apply(item, calc, simulate);

					if (!amt.isZero()) {
						result.add(amt);
						calc.subtract(amt);

						if (result.isGreaterThanOrEqual(volume)) {
							break;
						}
					}
				}
			}

			tx.commit();
			return result;
		} catch (final Exception e) {
			Fluidity.LOG.warn("Unlable to complete carrier broadcast supply request due to exception.", e);
			return Fraction.ZERO;
		}
	}

	@Override
	public long apply(Article item, long numerator, long divisor, boolean simulate) {
		final LimitedCarrier<T> carrier = fromNode.carrier();

		final int nodeCount = carrier.nodeCount();

		if (nodeCount <= 1) {
			return 0;
		}

		try (Transaction tx = Transaction.open()) {
			// note that cost function is self-enlisting
			numerator = carrier.costFunction().apply(fromNode, item, numerator, divisor, simulate);

			long result = 0;

			for (int i = 0; i < nodeCount; ++i) {
				final CarrierNode n = carrier.nodeByIndex(i);

				if (n != fromNode && n.hasFlag(CarrierNode.FLAG_ACCEPT_SUPPLIER_BROADCASTS)) {
					final ArticleFunction s = n.getComponent(ArticleFunction.SUPPLIER_COMPONENT).get();
					tx.enlist(s); // allow for implementations that do not self-enlist
					result += s.apply(item, numerator - result, divisor, simulate);

					if (result >= numerator) {
						break;
					}
				}
			}

			tx.commit();
			return result;
		} catch (final Exception e) {
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

	@Override
	public Article suggestArticle(ArticleType<?> type) {
		final LimitedCarrier<T> carrier = fromNode.carrier();

		final int nodeCount = carrier.nodeCount();

		if (nodeCount <= 1) {
			return Article.NOTHING;
		}

		for (int i = 0; i < nodeCount; ++i) {
			final CarrierNode n = carrier.nodeByIndex(i);

			if (n != fromNode && n.hasFlag(CarrierNode.FLAG_ACCEPT_SUPPLIER_BROADCASTS)) {
				final ArticleFunction c = n.getComponent(ArticleFunction.SUPPLIER_COMPONENT).get();
				final Article a = c.suggestArticle(type);

				if (!a.isNothing() && (type == null || a.type() == type)) {
					return a;
				}
			}
		}

		return Article.NOTHING;
	}
}
