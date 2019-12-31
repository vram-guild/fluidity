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
package grondag.fluidity.wip.api.transport;

public interface CarrierListener {
	/**
	 * Called when carrier being listened to becomes unavailable.
	 * Carriers that are disconnecting due to error or unexpected conditions
	 * should, at a minimum, call with {@code didNotify = false} and
	 * {@code isValid = false} so aggregate listeners know to reconstruct their views.
	 *
	 * @param storage Carrier that was being monitored.
	 * @param didNotify True if carrier called {@code onAttach} before disconnecting. (Preferred)
	 * @param isValid True if carrier state is currently valid and could be used to update listener.
	 */
	void disconnect(Carrier carrier, boolean didNotify, boolean isValid);

	void onAttach(Carrier carrier, CarrierSession node);

	void onDetach(Carrier carrier, CarrierSession node);
}
