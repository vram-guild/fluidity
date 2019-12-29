/*******************************************************************************
 * Copyright 2019 grondag
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

import java.util.function.Consumer;

import org.apiguardian.api.API;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.fraction.FractionView;
import grondag.fluidity.api.storage.FixedArticleSupplier;
import grondag.fluidity.api.transact.TransactionContext;

@API(status = INTERNAL)
public final class CreativeSupplier implements FixedArticleSupplier {
	private CreativeSupplier() {}

	public static FixedArticleSupplier INSTANCE = new CreativeSupplier();

	@Override
	public long supply(Article item, long count, boolean simulate) {
		return count;
	}

	@Override
	public FractionView supply(Article item, FractionView volume, boolean simulate) {
		return volume.toImmutable();
	}

	@Override
	public long supply(Article item, long numerator, long divisor, boolean simulate) {
		return numerator;
	}

	@Override
	public Consumer<TransactionContext> prepareRollback(TransactionContext context) {
		return c -> {};
	}

	@Override
	public long supply(int handle, Article item, long count, boolean simulate) {
		return count;
	}

	@Override
	public FractionView supply(int handle, Article item, FractionView volume, boolean simulate) {
		return volume.toImmutable();
	}

	@Override
	public long supply(int handle, Article item, long numerator, long divisor, boolean simulate) {
		return numerator;
	}

	@Override
	public boolean canSupply() {
		return true;
	}
}
