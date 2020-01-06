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
package grondag.fluidity.impl.storage;

import static org.apiguardian.api.API.Status.INTERNAL;

import org.apiguardian.api.API;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.fraction.Fraction;
import grondag.fluidity.api.fraction.FractionView;
import grondag.fluidity.api.storage.FixedArticleFunction;

@API(status = INTERNAL)
public final class FullConsumer implements FixedArticleFunction {
	private FullConsumer() {}

	public static FixedArticleFunction INSTANCE = new FullConsumer();

	@Override
	public long apply(Article item, long count, boolean simulate) {
		return 0;
	}

	@Override
	public FractionView apply(Article item, FractionView volume, boolean simulate) {
		return Fraction.ZERO;
	}

	@Override
	public long apply(Article item, long numerator, long divisor, boolean simulate) {
		return 0;
	}

	@Override
	public TransactionDelegate getTransactionDelegate() {
		return TransactionDelegate.IGNORE;
	}

	@Override
	public long apply(int handle, Article item, long count, boolean simulate) {
		return 0;
	}

	@Override
	public FractionView apply(int handle, Article item, FractionView volume, boolean simulate) {
		return Fraction.ZERO;
	}

	@Override
	public long apply(int handle, Article item, long numerator, long divisor, boolean simulate) {
		return 0;
	}

	@Override
	public boolean canApply() {
		return false;
	}
}