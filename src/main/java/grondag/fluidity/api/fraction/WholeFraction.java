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
package grondag.fluidity.api.fraction;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * Immutable fraction implementation that only deals in whole numbers.
 */
@API(status = Status.EXPERIMENTAL)
public final class WholeFraction implements FractionView {
	private final long whole;

	private WholeFraction(long whole) {
		this.whole = whole;
	}

	@Override
	public long whole() {
		return whole;
	}

	@Override
	public long numerator() {
		return 0;
	}

	@Override
	public long divisor() {
		return 1;
	}

	public static WholeFraction of(long whole) {
		return new WholeFraction(whole);
	}
}
