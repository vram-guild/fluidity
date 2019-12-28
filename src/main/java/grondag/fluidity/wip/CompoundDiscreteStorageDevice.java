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
package grondag.fluidity.wip;

import java.util.function.Consumer;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import grondag.fluidity.api.storage.Storage;
import grondag.fluidity.base.storage.discrete.AggregateDiscreteStorage;

@API(status = Status.EXPERIMENTAL)
public class CompoundDiscreteStorageDevice<T extends CompoundDeviceMember<T, U>, U extends CompoundDiscreteStorageDevice<T, U>> extends AggregateDiscreteStorage implements CompoundDevice<T, U> {

	protected final ObjectOpenHashSet<T> devices = new ObjectOpenHashSet<>();

	@Override
	public void add(T device) {
		devices.add(device);
		final Storage s = device.getLocalStorage();

		if(s != null && s != Storage.EMPTY) {
			addStore(s);
		}
	}

	@Override
	public void remove(T device) {
		onRemove(device);
		devices.remove(device);
	}

	protected void onRemove(T device) {
		final Storage s = device.getLocalStorage();

		if(s != null && s != Storage.EMPTY) {
			removeStore(s);
		}
	}

	@Override
	public int deviceCount() {
		return devices.size();
	}

	@Override
	public Storage getStorage() {
		return this;
	}

	@Override
	public void removalAllAndClose(Consumer<T> closeAction) {
		devices.forEach(d -> {
			onRemove(d);
			closeAction.accept(d);
		});

		devices.clear();
		close();
	}
}
