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

import grondag.fluidity.api.item.DiscreteItem;
import grondag.fluidity.base.article.DiscreteArticle;

@API(status = Status.EXPERIMENTAL)
public class DividedItemStorage extends FlexibleItemStorage  {
	protected final int divisionCount;
	protected final long capacityPerDivision;

	public DividedItemStorage(int divisionCount, long capacityPerDivision) {
		super(divisionCount, divisionCount * capacityPerDivision);
		this.divisionCount = divisionCount;
		this.capacityPerDivision = capacityPerDivision;
	}

	@Override
	public long accept(DiscreteItem item, long count, boolean simulate) {
		if(articles.usedHandleCount() >= divisionCount) {
			final DiscreteArticle a = articles.get(item);

			if(a == null) {
				return 0;
			}

			final long cap = capacityPerDivision - a.count;

			if (cap <= 0) {
				return 0;
			} else if(count > cap) {
				count = cap;
			}
		}

		return super.accept(item, count, simulate);
	}
}
