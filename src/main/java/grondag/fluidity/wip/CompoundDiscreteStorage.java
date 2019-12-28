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
public class CompoundDiscreteStorage<T extends CompoundDeviceMember<T, U>, U extends CompoundDiscreteStorage<T, U>> extends AggregateDiscreteStorage implements CompoundDevice<T, U> {

	protected final ObjectOpenHashSet<T> devices = new ObjectOpenHashSet<>();

	@Override
	public void add(T device) {
		devices.add(device);
		addStore(device.getStorage());
	}

	@Override
	public void remove(T device) {
		removeStore(device.getStorage());
		devices.remove(device);
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
		devices.forEach(closeAction);
		devices.clear();
		close();
	}
}
