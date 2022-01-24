/*
 * This file is part of Fluidity and is licensed to the project under
 * terms that are compatible with the GNU Lesser General Public License.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership and licensing.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
	 * Stop receiving events from the store.
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
			if (needsStartWarning) {
				Fluidity.LOG.warn("Encountered startListening call to unsupported event stream. Additional warnings are suppressed.");
				needsStartWarning = false;
			}
		}

		@Override
		public void stopListening(StorageListener listener, boolean sendNotifications) {
			if (needsStopWarning) {
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
