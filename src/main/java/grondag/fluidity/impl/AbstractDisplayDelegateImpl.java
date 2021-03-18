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

import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.client.resource.language.I18n;

import grondag.fluidity.api.article.Article;

@Internal
abstract class AbstractDisplayDelegateImpl {
	Article article = Article.NOTHING;
	int handle;
	String localizedName = "";
	String lowerCaseLocalizedName = "";

	public AbstractDisplayDelegateImpl(Article article, int handle) {
		setArticleAndHandle(article, handle);
	}

	protected final void setArticleAndHandle(Article article, int handle) {
		if(article == null) {
			article = Article.NOTHING;
		}

		this.handle = handle;

		if(!article.equals(this.article)) {
			this.article = article;
			localizedName = article.isNothing() ? "" : I18n.translate(article.getTranslationKey());
			lowerCaseLocalizedName = localizedName.toLowerCase();
		}
	}

	public final int handle() {
		return handle;
	}

	public final String localizedName() {
		return localizedName;
	}

	public final String lowerCaseLocalizedName() {
		return lowerCaseLocalizedName;
	}

	public final Article article() {
		return article;
	}
}
