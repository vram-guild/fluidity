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
package grondag.fluidity.base.multiblock;

import org.jetbrains.annotations.ApiStatus.Experimental;

import grondag.fluidity.api.multiblock.MultiBlockMember;
import grondag.fluidity.api.storage.Store;
import grondag.fluidity.base.storage.AbstractAggregateStore;

@Experimental
public abstract class AbstractStorageMultiBlock<T extends MultiBlockMember<T, U, Store>, U extends AbstractStorageMultiBlock<T, U>> extends AbstractMultiBlock<T, U, Store> {
	@SuppressWarnings("rawtypes")
	protected final AbstractAggregateStore storage;

	@SuppressWarnings("rawtypes")
	public AbstractStorageMultiBlock(AbstractAggregateStore storage) {
		this.storage = storage;
	}

	@Override
	protected void beforeMemberRemoval(T member) {
		final Store s = member.getMemberComponent();

		if(s != null && s != Store.EMPTY) {
			storage.removeStore(s);
		}
	}

	@Override
	protected void afterMemberAddition(T member) {
		final Store s = member.getMemberComponent();

		if(s != null && s != Store.EMPTY) {
			storage.addStore(s);
		}
	}
}
