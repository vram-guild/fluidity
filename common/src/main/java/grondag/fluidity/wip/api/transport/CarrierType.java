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
