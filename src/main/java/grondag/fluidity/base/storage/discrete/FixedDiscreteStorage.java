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
package grondag.fluidity.base.storage.discrete;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.fraction.Fraction;
import grondag.fluidity.api.fraction.FractionView;
import grondag.fluidity.api.storage.FixedStorage;

@API(status = Status.EXPERIMENTAL)
public interface FixedDiscreteStorage extends DiscreteStorage, FixedStorage {
	@Override
	default FractionView accept(int handle, Article item, FractionView volume, boolean simulate) {
		return Fraction.of(accept(handle, item, volume.whole(), simulate));
	}

	@Override
	default FractionView supply(int handle, Article item, FractionView volume, boolean simulate) {
		return Fraction.of(supply(handle, item, volume.whole(), simulate));
	}

	@Override
	default long accept(int handle, Article item, long numerator, long divisor, boolean simulate) {
		final long whole = numerator / divisor;
		return whole == 0 ? 0 : accept(handle, item, whole, simulate) * divisor;
	}

	@Override
	default long supply(int handle, Article item, long numerator, long divisor, boolean simulate) {
		final long whole = numerator / divisor;
		return whole == 0 ? 0 : supply(handle, item, whole, simulate) * divisor;
	}
}
