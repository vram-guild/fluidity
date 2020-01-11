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

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.fraction.FractionView;
import grondag.fluidity.api.fraction.MutableFraction;
import grondag.fluidity.base.synch.BulkDisplayDelegate;

@API(status = Status.INTERNAL)
public class BulkDisplayDelegateImpl extends AbstractDisplayDelegateImpl implements BulkDisplayDelegate {
	Article article = Article.NOTHING;
	final MutableFraction amount = new MutableFraction();
	int handle;
	String localizedName = "";
	String lowerCaseLocalizedName = "";

	public BulkDisplayDelegateImpl(Article article, FractionView amount, int handle) {
		super(article, handle);
		this.amount.set(amount);
	}

	@Override
	public BulkDisplayDelegateImpl set (Article article, FractionView amount, int handle) {
		setArticleAndHandle(article, handle);
		this.amount.set(amount);
		return this;
	}

	@Override
	public BulkDisplayDelegateImpl clone() {
		return new BulkDisplayDelegateImpl(article, amount, handle);
	}

	@Override
	public FractionView getAmount() {
		return amount;
	}

	@Override
	public void setAmount(FractionView amount) {
		this.amount.set(amount);
	}

	@Override
	public boolean isEmpty() {
		return amount.isZero();
	}
}
