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
package grondag.fluidity.base.article;

import org.jetbrains.annotations.ApiStatus.Experimental;

import grondag.fluidity.api.article.Article;

@Experimental
public abstract class AbstractStoredArticle implements StoredArticle {
	protected Article article;
	protected int handle;

	@Override
	public Article article() {
		return article;
	}

	@Override
	public int handle() {
		return handle;
	}

	@Override
	public void setArticle(Article article) {
		this.article = article;
	}

	@Override
	public void setHandle(int handle) {
		this.handle = handle;
	}
}
