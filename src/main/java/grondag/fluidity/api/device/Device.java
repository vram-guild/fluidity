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

package grondag.fluidity.api.device;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import grondag.fluidity.api.storage.Storage;

@API(status = Status.EXPERIMENTAL)
@FunctionalInterface
public interface Device {
	Storage getStorage();

	default Storage getStorage(Object connection) {
		return getStorage();
	}


	/**
	 * Component members may elect to return the compound storage instance from calls to
	 * {@link Device#getStorage()}. This method offers an unambiguous way to
	 * reference the storage of this component device specifically.
	 *
	 * <p>Also used by and necessary for aggregate storage implementations for the same reason.
	 *
	 * @return {@link Storage} of this compound member device.
	 */
	default Storage getLocalStorage() {
		return getStorage();
	}
}
