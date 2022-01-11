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
package grondag.fluidity.base.storage.discrete;

import org.jetbrains.annotations.ApiStatus.Experimental;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.fraction.Fraction;
import grondag.fluidity.api.storage.FixedArticleFunction;
import grondag.fluidity.api.storage.FixedStore;

@Experimental
public interface FixedDiscreteStore extends DiscreteStore, FixedStore {

	public interface FixedDiscreteArticleFunction extends DiscreteStore.DiscreteArticleFunction, FixedArticleFunction {
		@Override
		default Fraction apply(int handle, Article item, Fraction volume, boolean simulate) {
			return Fraction.of(apply(handle, item, volume.whole(), simulate));
		}

		@Override
		default long apply(int handle, Article item, long numerator, long divisor, boolean simulate) {
			final long whole = numerator / divisor;
			return whole == 0 ? 0 : apply(handle, item, whole, simulate) * divisor;
		}
	}
}
