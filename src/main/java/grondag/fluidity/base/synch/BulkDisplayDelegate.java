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
package grondag.fluidity.base.synch;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.fraction.Fraction;
import grondag.fluidity.impl.BulkDisplayDelegateImpl;

/**
 * Client-side representation of server inventory that supports
 * very large quantities and slotless/virtual containers via handles.
 */
@API(status = Status.EXPERIMENTAL)
public interface BulkDisplayDelegate extends DisplayDelegate  {
	Fraction getAmount();

	void setAmount(Fraction amount);

	BulkDisplayDelegate set(Article article, Fraction amount, int handle);

	default BulkDisplayDelegate set(BulkDisplayDelegate from) {
		return set(from.article(), from.getAmount(), from.handle());
	}

	BulkDisplayDelegate EMPTY = new BulkDisplayDelegateImpl(Article.NOTHING, Fraction.ZERO, -1);

	static BulkDisplayDelegate create(Article article, Fraction amount, int handle) {
		return new BulkDisplayDelegateImpl(article, amount, handle);
	}
}
