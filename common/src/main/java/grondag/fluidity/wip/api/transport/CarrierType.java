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

package grondag.fluidity.wip.api.transport;

import java.util.Collections;
import java.util.Set;

import org.jetbrains.annotations.ApiStatus.Experimental;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.article.ArticleType;

@FunctionalInterface
@Experimental
public interface CarrierType {
	Set<ArticleType<?>> articleTypes();

	default Set<Article> whiteList() {
		return Collections.emptySet();
	}

	default Set<Article> blackList() {
		return Collections.emptySet();
	}

	default boolean canCarry(ArticleType<?> type) {
		return articleTypes().contains(type);
	}

	default boolean canCarry(Article article) {
		return canCarry(article.type())
				&& (whiteList().isEmpty() || whiteList().contains(article))
				&& (blackList().isEmpty() || !blackList().contains(article));
	}

	CarrierType EMPTY = () -> Collections.emptySet();
}
