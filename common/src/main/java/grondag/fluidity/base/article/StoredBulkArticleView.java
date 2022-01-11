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

import grondag.fluidity.api.article.StoredArticleView;

/**
 * A view of an article stored in a container. (Could be an discrete item or bulk item.) <p>
 *
 * Containers (especially virtual ones) could contain both types of article.
 * Most containers will not need this and should instead use the specific view type for their content.
 */
@Experimental
public interface StoredBulkArticleView extends StoredArticleView{
	@Override
	default long count() {
		return amount().whole();
	}
}
