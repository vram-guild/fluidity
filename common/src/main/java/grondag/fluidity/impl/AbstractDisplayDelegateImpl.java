/*
 * This file is part of Fluidity and is licensed to the project under
 * terms that are compatible with the GNU Lesser General Public License.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership and licensing.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package grondag.fluidity.impl;

import java.util.Locale;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.client.resources.language.I18n;

import grondag.fluidity.api.article.Article;

@Internal
abstract class AbstractDisplayDelegateImpl {
	Article article = Article.NOTHING;
	int handle;
	String localizedName = "";
	String lowerCaseLocalizedName = "";

	AbstractDisplayDelegateImpl(Article article, int handle) {
		setArticleAndHandle(article, handle);
	}

	protected final void setArticleAndHandle(Article article, int handle) {
		if (article == null) {
			article = Article.NOTHING;
		}

		this.handle = handle;

		if (!article.equals(this.article)) {
			this.article = article;
			localizedName = article.isNothing() ? "" : I18n.get(article.getTranslationKey());
			lowerCaseLocalizedName = localizedName.toLowerCase(Locale.ROOT);
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
