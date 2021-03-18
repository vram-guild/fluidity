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
package grondag.fluidity.base.storage;

import org.jetbrains.annotations.ApiStatus.Experimental;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.fraction.Fraction;
import grondag.fluidity.api.storage.FixedArticleFunction;

@Experimental
public class ForwardingFixedArticleFunction<T extends FixedArticleFunction> extends ForwardingArticleFunction<T> implements FixedArticleFunction {
	public ForwardingFixedArticleFunction(T wrapped) {
		super(wrapped);
	}

	@Override
	public long apply(int handle, Article item, long count, boolean simulate) {
		return wrapped.apply(handle, item, count, simulate);
	}

	@Override
	public Fraction apply(int handle, Article item, Fraction volume, boolean simulate) {
		return wrapped.apply(handle, item, volume, simulate);
	}

	@Override
	public long apply(int handle, Article item, long numerator, long divisor, boolean simulate) {
		return wrapped.apply(handle, item, numerator, divisor, simulate);
	}
}
