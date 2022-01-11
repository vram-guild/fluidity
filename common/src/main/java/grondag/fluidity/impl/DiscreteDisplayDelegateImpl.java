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

import grondag.fluidity.api.article.Article;
import grondag.fluidity.base.synch.DiscreteDisplayDelegate;

@Internal
public class DiscreteDisplayDelegateImpl extends AbstractDisplayDelegateImpl implements DiscreteDisplayDelegate {
	long count;

	public DiscreteDisplayDelegateImpl(Article article, long count, int handle) {
		super(article, handle);
		this.count = count;
	}

	@Override
	public DiscreteDisplayDelegateImpl set (Article article, long count, int handle) {
		setArticleAndHandle(article, handle);
		this.count = count;
		return this;
	}

	@Override
	public DiscreteDisplayDelegateImpl clone() {
		return new DiscreteDisplayDelegateImpl(article, count, handle);
	}

	@Override
	public long getCount() {
		return count;
	}

	@Override
	public void setCount(long count) {
		this.count = count;
	}

	@Override
	public boolean isEmpty() {
		return count == 0;
	}
}
