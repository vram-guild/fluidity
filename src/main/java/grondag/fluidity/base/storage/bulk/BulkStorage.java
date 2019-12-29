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
package grondag.fluidity.base.storage.bulk;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.storage.ArticleConsumer;
import grondag.fluidity.api.storage.ArticleSupplier;
import grondag.fluidity.api.storage.Storage;

@API(status = Status.EXPERIMENTAL)
public interface BulkStorage extends Storage {
	@Override
	default long count() {
		return amount().whole();
	}

	@Override
	default long capacity() {
		return volume().whole();
	}

	public interface BulkArticleSupplier extends ArticleSupplier {
		@Override
		default long supply(Article item, long count, boolean simulate) {
			return supply(item, count, 1, simulate);
		}
	}

	public interface BulkArticleConsumer extends ArticleConsumer {
		@Override
		default long accept(Article item, long count, boolean simulate) {
			return accept(item, count, 1, simulate);
		}
	}
}
