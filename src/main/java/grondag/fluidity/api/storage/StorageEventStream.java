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
package grondag.fluidity.api.storage;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import grondag.fluidity.Fluidity;

@API(status = Status.EXPERIMENTAL)
public interface StorageEventStream {
	void startListening(StorageListener listener, boolean sendNotifications);

	void stopListening(StorageListener listener, boolean sendNotifications);

	StorageEventStream UNSUPPORTED = new StorageEventStream() {
		boolean needsStartWarning = true;
		boolean needsStopWarning = true;

		@Override
		public void startListening(StorageListener listener, boolean sendNotifications) {
			if(needsStartWarning) {
				Fluidity.LOG.warn("Encountered startListening call to unsupported event stream. Additional warnings are suppressed.");
				needsStartWarning = false;
			}
		}

		@Override
		public void stopListening(StorageListener listener, boolean sendNotifications) {
			if(needsStopWarning) {
				Fluidity.LOG.warn("Encountered stopListening call to unsupported event stream.");
				needsStopWarning = false;
			}
		}

	};

	StorageEventStream IGNORE = new StorageEventStream() {
		@Override
		public void startListening(StorageListener listener, boolean sendNotifications) {
			// NOOP
		}

		@Override
		public void stopListening(StorageListener listener, boolean sendNotifications) {
			// NOOP
		}
	};
}
