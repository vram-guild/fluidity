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
package grondag.fluidity.api.storage.view;

import javax.annotation.Nullable;

import grondag.fluidity.api.fraction.FractionView;
import grondag.fluidity.api.item.base.BulkItem;

/**
 * View of a bulk item in storage.
 */
public interface BulkArticleView extends ArticleView {
	BulkItem bulkItem();

	FractionView volume();

	@Override
	default boolean isBulk() {
		return true;
	}

	@Override
	@Nullable
	default BulkArticleView toBulkView() {
		return  this;
	}
}
