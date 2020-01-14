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
package grondag.fluidity.base.storage.bulk;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.storage.FixedArticleFunction;
import grondag.fluidity.api.storage.FixedStore;

@API(status = Status.EXPERIMENTAL)
public interface FixedBulkStore extends BulkStore, FixedStore {
	public interface FixedBulkArticleSupplier extends BulkArticleFunction, FixedArticleFunction {
		@Override
		default long apply(int handle, Article item, long count, boolean simulate) {
			return apply(handle, item, count, 1, simulate);
		}
	}
}
