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
package grondag.fluidity.impl;

import static org.apiguardian.api.API.Status.INTERNAL;

import org.apiguardian.api.API;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.fraction.Fraction;
import grondag.fluidity.api.fraction.FractionView;
import grondag.fluidity.api.storage.FixedArticleSupplier;

@API(status = INTERNAL)
public final class EmptySupplier implements FixedArticleSupplier {
	private EmptySupplier() {}

	public static FixedArticleSupplier INSTANCE = new EmptySupplier();

	@Override
	public long supply(Article item, long count, boolean simulate) {
		return 0;
	}

	@Override
	public FractionView supply(Article item, FractionView volume, boolean simulate) {
		return Fraction.ZERO;
	}

	@Override
	public long supply(Article item, long numerator, long divisor, boolean simulate) {
		return 0;
	}

	@Override
	public TransactionDelegate getTransactionDelegate() {
		return TransactionDelegate.IGNORE;
	}

	@Override
	public long supply(int handle, Article item, long count, boolean simulate) {
		return 0;
	}

	@Override
	public FractionView supply(int handle, Article item, FractionView volume, boolean simulate) {
		return Fraction.ZERO;
	}

	@Override
	public long supply(int handle, Article item, long numerator, long divisor, boolean simulate) {
		return 0;
	}

	@Override
	public boolean canSupply() {
		return false;
	}
}
