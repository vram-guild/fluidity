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

import org.jetbrains.annotations.ApiStatus.Experimental;

import grondag.fluidity.impl.Fluidity;

/**
 * Exposes an observable stream of storage events that can be used by a listener to replicate a view
 * of store contents or respond to actions involving the store.
 *
 * @see <a href="https://github.com/grondag/fluidity#store-and-its-variants">https://github.com/grondag/fluidity#store-and-its-variants</a>
 */
@Experimental
public interface StorageEventStream {
	/**
	 * Begin receiving events from the store.
	 *
	 * @param listener observer that will receive events
	 * @param sendNotifications if {@code true}, store will immediately send "accept" notifications for all store content visible to the listener
	 */
	void startListening(StorageListener listener, boolean sendNotifications);

	/**
	 * Stop receiving events from the store
	 *
	 * @param listener observer that will no longer receive events
	 * @param sendNotifications if {@code true}, store will immediately send "supply" notifications for all store content visible to the listener
	 */
	void stopListening(StorageListener listener, boolean sendNotifications);

	/**
	 * Specialized event stream that marks the presence of a stream that isn't functional.
	 * Logs a one-time warning if called but does not send events and done not throw an exception.
	 */
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
				Fluidity.LOG.warn("Encountered stopListening call to unsupported event stream. Additional warnings are suppressed.");
				needsStopWarning = false;
			}
		}

	};

	/**
	 * Specialized event stream that is "supported" and functions correctly, but does nothing.
	 * Use for creative stores, trash cans and similar implementations that have no internal
	 * state and thus no meaningful events that change it.
	 */
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
