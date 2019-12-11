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
package grondag.fluidity.api.storage.base;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

@API(status = Status.EXPERIMENTAL)
public class SimpleTank {

	//	@Override
	//	public FractionView accept(BulkItem item, FractionView volume, boolean simulate) {
	//		Preconditions.checkArgument(!volume.isNegative(), "Request to accept negative volume. (%s)", volume);
	//
	//		view.prepare(stack, 0);
	//
	//		if (!view.isBulk() || view.isEmpty()) {
	//			return Fraction.ZERO;
	//		}
	//
	//
	//		return null;
	//	}
	//
	//	@Override
	//	public FractionView supply(BulkItem item, FractionView volume, boolean simulate) {
	//		Preconditions.checkArgument(!volume.isNegative(), "Request to supply negative volume. (%s)", volume);
	//		return null;
	//	}
	//
	//	@Override
	//	public long accept(BulkItem item, long numerator, long divisor) {
	//		Preconditions.checkArgument(numerator >= 0, "Request to accept negative volume. (%s)", numerator);
	//		return 0;
	//	}
	//
	//	@Override
	//	public long supply(BulkItem item, long numerator, long divisor) {
	//		Preconditions.checkArgument(numerator >= 0, "Request to supply negative volume. (%s)", numerator);
	//
	//		return 0;
	//	}
}
