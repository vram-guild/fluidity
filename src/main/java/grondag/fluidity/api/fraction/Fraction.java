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
import net.minecraft.network.PacketByteBuf;

/**
 * Immutable, full-resolution rational number representation.
 */
@API(status = Status.EXPERIMENTAL)
public class Fraction implements Comparable<Fraction> {
	protected long whole;
	protected long numerator;
	protected long divisor;

	/**
	 * Constructs a new fraction with value of zero.
	 * Generally better to use {@link #ZERO} instead.
	 */
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

	/**
	 * Deserializes an new instance from an NBT tag previously returned by {@link #toTag()}.
	 *
	 * @param tag NBT tag previously returned by {@link #toTag()
	 */
	public Fraction(Tag tag) {
		readTagInner((CompoundTag) tag);
	}

	/**
	 * Deserializes a new instance from previously encoded to a packet buffer by {@link #writeBuffer(PacketByteBuf)}
	 *
	 * @param buf packet buffer containing data encoded by {@link #writeBuffer(PacketByteBuf)}
	 */
	public Fraction(PacketByteBuf buf) {
		readBufferInner(buf);
	}

	public Fraction(long whole) {
		this(whole, 0, 1);
	}

	public Fraction(Fraction template) {
		this(template.whole(), template.numerator(), template.divisor());
	}

	/**
	 * The whole-number portion of this fraction.
	 *
	 * If this fraction is negative, both {@link #whole()} and {@link #numerator()}
	 * will be zero or negative.
	 *
	 * @return The whole-number portion of this fraction
	 */
	public final long whole() {
		return whole;
	}

	/**
	 * The fractional portion of this fraction, or zero if the fraction
	 * represents a whole number.<p>
	 *
	 * If this fraction is negative, both {@link #whole()} and {@link #numerator()}
	 * will be zero or negative.<p>
	 *
	 * The absolute values of {@link #numerator()} will always be zero
	 * or less than the value of {@link #divisor()}. Whole numbers are
	 * always fully represented in {@link #whole()}.
	 *
	 * @return The whole-number portion of this fraction
	 */
	public final long numerator() {
		return numerator;
	}

	/**
	 * The denominator for the fractional portion of this fraction.
	 * Will always be >= 1.
	 *
	 * @return the denominator for the fractional portion of this fraction
	 */
	public final long divisor() {
		return divisor;
	}

	/**
	 * Serializes this instance to the given packet buffer.
	 *
	 * @param buffer packet buffer to receive serialized data
	 */
	public final void writeBuffer(PacketByteBuf buffer) {
		buffer.writeVarLong(whole);
		buffer.writeVarLong(numerator);
		buffer.writeVarLong(divisor);
	}

	/**
	 * Serializes this instance in an NBT compound tag without
	 * creating a new tag instance. This is meant for use cases
	 * (mostly internal to Fluidity) where key collision is not a risk.
	 *
	 * @param tag NBT tag to contain serialized data
	 */
	public final void writeTag(CompoundTag tag) {
		tag.putLong("whole", whole);
		tag.putLong("numerator", numerator);
		tag.putLong("denominator", divisor);
	}

	/**
	 * Serializes this instance to a new NBT tag.
	 *
	 * @return new tag instance containing serialized data
	 */
	public final Tag toTag() {
		final CompoundTag result = new CompoundTag();
		writeTag(result);
		return result;
	}

	protected final void readBufferInner(PacketByteBuf buf) {
		whole = buf.readVarLong();
		numerator = buf.readVarLong();
		divisor = buf.readVarLong();
		normalize();
	}

	protected final void readTagInner(CompoundTag tag) {
		whole = tag.getLong("whole");
		numerator = tag.getLong("numerator");
		divisor = Math.max(1, tag.getLong("denominator"));
		normalize();
	}

	@Override
	public final boolean equals(Object val) {
		if (val == null || !(val instanceof Fraction)) {
			return false;
		}

		return compareTo((Fraction) val) == 0;
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
		final long gcd = gcd(Math.abs(numerator), divisor);

		if (gcd != divisor) {
			numerator /= gcd;
			divisor /= gcd;
		}
	}

	protected final long gcd(long a, long b) {
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
	public final double toDouble(long units) {
		// start with unit scale
		final double base = (double) numerator() / (double) divisor() + whole();

		// scale to requested unit
		return units == 1 ? base : base / units;
	}

	/**
	 * Intended for user display. Result may be approximate due to floating point error.
	 *
	 * @return This fraction as a {@code double} primitive
	 */
	public final double toDouble() {
		return toDouble(1);
	}

	/**
	 * Returns the number of units that is less than or equal to the given unit.
	 * Make be larger than this if value is not evenly divisible .
	 *
	 * @param units Fraction of one bucket that counts as 1 in the result. Must be >= 1.
	 * @return Number of units within current volume.
	 */
	public final long toLong(long divisor) {
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

	/**
	 * Test if this fraction is exactly zero.
	 *
	 * @return {@code true} if this fraction is exactly zero
	 */
	public final boolean isZero() {
		return whole() == 0 && numerator() == 0;
	}

	/**
	 * Test if this fraction is a negative number.
	 *
	 * @return {@code true} if this fraction is a negative number
	 */
	public final boolean isNegative() {
		return whole() < 0 || (whole() == 0 && numerator() < 0);
	}

	@Override
	public final int compareTo(Fraction o) {
		final int result = Long.compare(whole(), o.whole());
		return result == 0 ? Long.compare(numerator() * o.divisor(), o.numerator() * divisor()) : result;
	}

	public final boolean isGreaterThan(Fraction other) {
		return compareTo(other) > 0;
	}

	public final boolean isGreaterThankOrEqual(Fraction other) {
		return compareTo(other) >= 0;
	}

	public final boolean isLessThan(Fraction other) {
		return compareTo(other) < 0;
	}

	public final boolean isLessThankOrEqual(Fraction other) {
		return compareTo(other) <= 0;
	}

	/**
	 * Ensures this instance is safe to retain. Should always be
	 * called for any {@code Fraction} instance that will be retained
	 * unless the instance is already known to be immutable.
	 *
	 * @return a new immutable {@code Fraction} instance if this is a {@code MutableFraction}, or this instance otherwise.
	 */
	public Fraction toImmutable() {
		return this;
	}

	/**
	 * The smallest whole number that is greater than or equal to the
	 * rational number represented by this fraction.
	 *
	 * @return the smallest whole number greater than or equal to this fraction
	 */
	public final long ceil() {
		return numerator() == 0 || whole() < 0 ? whole() : whole() + 1;
	}

	/**
	 * Returns a new value equal to this fraction multiplied by -1.
	 *
	 * @return A new fraction equal to this fraction multiplied by -1
	 */
	public final Fraction toNegated() {
		return Fraction.of(-whole(), -numerator(), divisor());
	}

	/**
	 * Returns a new value equal to this value less the given parameter.
	 *
	 * This method is allocating and for frequent and repetitive operations
	 * it will be preferable to use a mutable fraction instance.
	 *
	 * @param diff value to be subtracted from this value
	 * @return a new value equal to this value less the given parameter
	 */
	public final Fraction withSubtraction(Fraction diff) {
		final MutableFraction f = new MutableFraction(this);
		f.subtract(diff);
		return f.toImmutable();
	}

	/**
	 * Returns a new value equal to this value plus the given parameter.
	 *
	 * This method is allocating and for frequent and repetitive operations
	 * it will be preferable to use a mutable fraction instance.
	 *
	 * @param diff value to be added to this value
	 * @return a new value equal to this value plus the given parameter
	 */
	public final Fraction withAddition(Fraction diff) {
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
