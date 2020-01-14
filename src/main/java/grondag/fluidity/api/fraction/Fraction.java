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

import it.unimi.dsi.fastutil.HashCommon;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.PacketByteBuf;

/**
 * Immutable, full-resolution fraction implementation.
 */
@API(status = Status.EXPERIMENTAL)
public class Fraction {
	protected long whole;
	protected long numerator;
	protected long divisor;

	public Fraction() {
		this(0, 0, 1);
	}

	public Fraction(long whole, long numerator, long divisor) {
		validate(whole, numerator, divisor);
		this.whole = whole;
		this.numerator = numerator;
		this.divisor = divisor;
		normalize();
	}

	public Fraction(long numerator, long divisor) {
		validate(0, numerator, divisor);
		whole = numerator / divisor;
		this.numerator = numerator - whole * divisor;
		this.divisor = divisor;
		normalize();
	}

	public Fraction(Tag tag) {
		readTag((CompoundTag) tag);
	}

	public Fraction(PacketByteBuf buf) {
		readBuffer(buf);
	}

	public Fraction(long whole) {
		this(whole, 0, 1);
	}

	public Fraction(Fraction template) {
		this(template.whole(), template.numerator(), template.divisor());
	}

	public long whole() {
		return whole;
	}

	public long numerator() {
		return numerator;
	}

	public long divisor() {
		return divisor;
	}

	public void writeBuffer(PacketByteBuf buffer) {
		buffer.writeVarLong(whole);
		buffer.writeVarLong(numerator);
		buffer.writeVarLong(divisor);
	}

	public void writeTag(CompoundTag tag) {
		tag.putLong("whole", whole);
		tag.putLong("numerator", numerator);
		tag.putLong("denominator", divisor);
	}

	public Tag toTag() {
		final CompoundTag result = new CompoundTag();
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
	public boolean equals(Object val) {
		if (val == null || !(val instanceof Fraction)) {
			return false;
		}

		return compareTo((Fraction) val) == 0;
	}

	@Override
	public int hashCode() {
		return (int) (HashCommon.mix(whole) ^ HashCommon.mix(numerator ^ divisor));
	}

	protected void validate(long whole, long numerator, long divisor) {
		if (divisor < 1) {
			throw new IllegalArgumentException("Fraction divisor must be >= 1");
		}
	}

	@Override
	public String toString() {
		return String.format("%d and %d / %d, approx: %f", whole, numerator, divisor, toDouble());
	}

	protected void normalize() {
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
		final long gcd = gcd(Math.abs(numerator), divisor);

		if (gcd != divisor) {
			numerator /= gcd;
			divisor /= gcd;
		}
	}

	protected long gcd(long a, long b) {
		while (b != 0) {
			final long t = b;
			b = a % b;
			a = t;
		}

		return a;
	}

	/**
	 * Intended for user display. Result may be approximate due to floating point error.
	 *
	 * @param units Fraction of one that counts as 1 in the result. Must be >= 1.
	 * @return Current value scaled so that that 1.0 = one of the given units
	 */
	public double toDouble(long units) {
		// start with unit scale
		final double base = (double) numerator() / (double) divisor() + whole();

		// scale to requested unit
		return units == 1 ? base : base / units;
	}

	public double toDouble() {
		return toDouble(1);
	}

	/**
	 * Returns the number of units that is less than or equal to the given unit.
	 * Make be larger than this if value is not evenly divisible .
	 *
	 * @param units Fraction of one bucket that counts as 1 in the result. Must be >= 1.
	 * @return Number of units within current volume.
	 */
	public long toLong(long divisor) {
		if (divisor < 1) {
			throw new IllegalArgumentException("RationalNumber divisor must be >= 1");
		}

		final long base = whole() * divisor;

		if (numerator() == 0) {
			return base;
		} else if (divisor() == divisor) {
			return base + numerator();
		} else {
			return base + numerator() * divisor / divisor();
		}
	}

	public boolean isZero() {
		return whole() == 0 && numerator() == 0;
	}

	public boolean isNegative() {
		return whole() < 0 || (whole() == 0 && numerator() < 0);
	}

	public int compareTo(Fraction o) {
		final int result = Long.compare(whole(), o.whole());
		return result == 0 ? Long.compare(numerator() * o.divisor(), o.numerator() * divisor()) : result;
	}

	public boolean isGreaterThan(Fraction other) {
		return compareTo(other) > 0;
	}

	public boolean isGreaterThankOrEqual(Fraction other) {
		return compareTo(other) >= 0;
	}

	public boolean isLessThan(Fraction other) {
		return compareTo(other) < 0;
	}

	public boolean isLessThankOrEqual(Fraction other) {
		return compareTo(other) <= 0;
	}

	public Fraction toImmutable() {
		return Fraction.of(whole(), numerator(), divisor());
	}

	public long ceil() {
		return numerator() == 0 ? whole() : whole() + 1;
	}

	public Fraction toNegated() {
		return Fraction.of(-whole(), -numerator(), divisor());
	}

	// not great, but like keeping the MutableFraction method names simple...
	public Fraction withSubtraction(Fraction diff) {
		final MutableFraction f = new MutableFraction(this);
		f.subtract(diff);
		return f.toImmutable();
	}

	public Fraction withAddition(Fraction diff) {
		final MutableFraction f = new MutableFraction(this);
		f.add(diff);
		return f.toImmutable();
	}

	public static final Fraction ZERO = Fraction.of(0, 0, 1);
	public static final Fraction ONE = Fraction.of(1, 0, 1);
	public static final Fraction MAX_VALUE = Fraction.of(Long.MAX_VALUE, 0, 1);

	public static Fraction of(long whole, long numerator, long divisor) {
		return new Fraction(whole, numerator, divisor);
	}

	public static Fraction of(long numerator, long divisor) {
		return new Fraction(numerator, divisor);
	}

	public static Fraction of(long whole) {
		return new Fraction(whole);
	}
}
