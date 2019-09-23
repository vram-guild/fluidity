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
package grondag.fluidity.impl;

import grondag.fluidity.api.fraction.FractionView;
import it.unimi.dsi.fastutil.HashCommon;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.PacketByteBuf;

public abstract class AbstractFraction implements FractionView {
	protected long whole;
	protected long numerator;
	protected long divisor;

	protected AbstractFraction() {
		this(0, 0, 1);
	}

	protected AbstractFraction(long whole, long numerator, long divisor) {
		validate(whole, numerator, divisor);
		this.whole = whole;
		this.numerator = numerator;
		this.divisor = divisor;
		normalize();
	}

	protected AbstractFraction(long numerator, long divisor) {
		validate(0, numerator, divisor);
		this.whole = numerator / divisor;
		this.numerator = numerator - whole * divisor;
		this.divisor = divisor;
		normalize();
	}

	@Override
	public final long whole() {
		return whole;
	}

	@Override
	public final long numerator() {
		return numerator;
	}

	@Override
	public final long divisor() {
		return divisor;
	}

	public final void writeBuffer(PacketByteBuf buffer) {
		buffer.writeVarLong(whole);
		buffer.writeVarLong(numerator);
		buffer.writeVarLong(divisor);
	}

	public final void writeTag(CompoundTag tag) {
		tag.putLong("whole", whole);
		tag.putLong("numerator", numerator);
		tag.putLong("denominator", divisor);
	}

	public final Tag toTag() {
		CompoundTag result = new CompoundTag();
		writeTag(result);
		return result;
	}

	protected void readBuffer(PacketByteBuf buf) {
		whole = buf.readVarLong();
		numerator = buf.readVarLong();
		divisor = buf.readVarLong();
		normalize();
	}

	protected void readTag(CompoundTag tag) {
		whole = tag.getLong("whole");
		numerator = tag.getLong("numerator");
		divisor = tag.getLong("denominator");
		normalize();
	}

	@Override
	public final boolean equals(Object val) {
		if (val == null || !(val instanceof AbstractFraction)) {
			return false;
		}
		AbstractFraction other = (AbstractFraction) val;
		return other.whole() == this.whole && other.numerator() == this.numerator && other.divisor() == this.divisor;
	}

	@Override
	public final int hashCode() {
		return (int) (HashCommon.mix(whole) ^ HashCommon.mix(numerator ^ divisor));
	}

	protected final void validate(long whole, long numerator, long divisor) {
		if (divisor < 1) {
			throw new IllegalArgumentException("Fraction divisor must be >= 1");
		}
	}

	@Override
	public final String toString() {
		return String.format("%d and %d / %d, approx: %f", whole, numerator, divisor, toDouble());
	}

	protected final void normalize() {
		if (Math.abs(numerator) >= divisor) {
			final long w = numerator / divisor;
			whole += w;
			numerator -= w * divisor;
		}

		if (numerator == 0) {
			divisor = 1;
			return;
		}

		// keep signs consistent
		if (whole < 0) {
			if (numerator > 0) {
				whole += 1;
				numerator -= divisor;
			}
		} else if (numerator < 0) {
			if (whole > 0) {
				whole -= 1;
				numerator += divisor;
			}
		}

		// remove powers of two bitwise
		final int twos = Long.numberOfTrailingZeros(numerator | divisor);
		if (twos > 0) {
			numerator >>= twos;
			divisor >>= twos;
		}

		// use conventional gcd for rest
		long gcd = gcd(Math.abs(numerator), divisor);
		if (gcd != divisor) {
			numerator /= gcd;
			divisor /= gcd;
		}
	}

	protected final long gcd(long a, long b) {
		while (b != 0) {
			long t = b;
			b = a % b;
			a = t;
		}
		return a;
	}
}
