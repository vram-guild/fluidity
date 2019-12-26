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
package grondag.fluidity.base.article;

import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.article.StoredArticleView;
import grondag.fluidity.api.storage.Storage;

@API(status = Status.EXPERIMENTAL)
public abstract class AbstractStoredArticle implements StoredArticleView {
	// TODO: encapsulate
	public Article article;
	public int handle;
	public final ObjectArraySet<Storage> stores = new ObjectArraySet<>();

	@Override
	public Article item() {
		return article;
	}

	@Override
	public int handle() {
		return handle;
	}

	public abstract void addStore(Storage store);

	public abstract void zero();

}
