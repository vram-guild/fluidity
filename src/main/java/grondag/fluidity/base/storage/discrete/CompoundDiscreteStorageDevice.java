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
package grondag.fluidity.base.storage.discrete;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.minecraft.nbt.CompoundTag;

import grondag.fluidity.api.article.StoredArticleView;
import grondag.fluidity.api.device.CompoundMemberDevice;
import grondag.fluidity.api.device.StorageProvider;
import grondag.fluidity.api.storage.Storage;
import grondag.fluidity.api.storage.StorageListener;
import grondag.fluidity.base.device.AbstractCompoundDevice;

@API(status = Status.EXPERIMENTAL)
public class CompoundDiscreteStorageDevice<T extends CompoundMemberDevice<T, U>, U extends CompoundDiscreteStorageDevice<T, U>> extends AbstractCompoundDevice<T, U> implements DiscreteStorage {
	protected final AggregateDiscreteStorage storage = new AggregateDiscreteStorage();

	@Override
	protected void onRemove(T device) {
		final Storage s = device.getStorageProvider().getLocalStorage();

		if(s != null && s != Storage.EMPTY) {
			storage.removeStore(s);
		}
	}

	@Override
	protected void onAdd(T device) {
		final Storage s = device.getStorageProvider().getLocalStorage();

		if(s != null && s != Storage.EMPTY) {
			storage.addStore(s);
		}
	}

	private final StorageProvider provider = (s, d) -> storage;

	@Override
	public StorageProvider getStorageProvider() {
		return provider;
	}

	@Override
	public int handleCount() {
		return storage.handleCount();
	}

	@Override
	public StoredArticleView view(int handle) {
		return storage.view(handle);
	}

	@Override
	public boolean isFull() {
		return storage.isFull();
	}

	@Override
	public boolean isEmpty() {
		return storage.isEmpty();
	}

	@Override
	public long count() {
		return storage.count();
	}

	@Override
	public long capacity() {
		return storage.capacity();
	}

	@Override
	public void clear() {
		storage.clear();
	}

	@Override
	public void startListening(StorageListener listener, boolean sendNotifications) {
		storage.startListening(listener, sendNotifications);
	}

	@Override
	public void stopListening(StorageListener listener, boolean sendNotifications) {
		storage.stopListening(listener, sendNotifications);
	}

	@Override
	public CompoundTag writeTag() {
		return storage.writeTag();
	}

	@Override
	public void readTag(CompoundTag tag) {
		storage.readTag(tag);
	}

	@Override
	public TransactionDelegate getTransactionDelegate() {
		return storage.getTransactionDelegate();
	}
}
