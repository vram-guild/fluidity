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

package grondag.fluidity.api.fraction;

import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;

@Experimental
public final class MutableFraction extends Fraction {
	public MutableFraction() {
		super();
	}

	public MutableFraction(long whole) {
		super(whole, 0, 1);
	}

	public MutableFraction(long numerator, long divisor) {
		super(numerator, divisor);
	}

	public MutableFraction(long whole, long numerator, long divisor) {
		super(whole, numerator, divisor);
	}

	public MutableFraction(Fraction template) {
		super(template.whole(), template.numerator(), template.divisor());
	}

	/**
	 * Constructs a new instance initialized with the value
	 * previously encoded in the given tag via {@link #writeTag(CompoundTag)}.
	 *
	 * @param tag NBT tag with encoded value
	 */
	public MutableFraction(Tag tag) {
		readTag((CompoundTag) tag);
	}

	/**
	 * Constructs a new instance initialized with the value
	 * previously encoded in the given packet buffer via {@link #writeBuffer(FriendlyByteBuf)}.
	 *
	 * @param buf packet buffer with encoded value
	 */
	public MutableFraction(FriendlyByteBuf buf) {
		readBuffer(buf);
	}

	public MutableFraction set(long whole) {
		return this.set(whole, 0, 1);
	}

	public MutableFraction set(long numerator, long divisor) {
		validate(0, numerator, divisor);
		whole = numerator / divisor;
		this.numerator = numerator - whole * divisor;
		this.divisor = divisor;
		return this;
	}

	public MutableFraction set(long whole, long numerator, long divisor) {
		validate(whole, numerator, divisor);
		this.whole = whole;
		this.numerator = numerator;
		this.divisor = divisor;
		return this;
	}

	public MutableFraction set(Fraction template) {
		whole = template.whole();
		numerator = template.numerator();
		divisor = template.divisor();
		return this;
	}

	public MutableFraction add(Fraction val) {
		return add(val.whole(), val.numerator(), val.divisor());
	}

	public MutableFraction add(long whole) {
		return add(whole, 0, 1);
	}

	public MutableFraction add(long numerator, long divisor) {
		return add(0, numerator, divisor);
	}

	public MutableFraction add(long whole, long numerator, long divisor) {
		validate(whole, numerator, divisor);
		this.whole += whole;

		if (Math.abs(numerator) >= divisor) {
			final long w = numerator / divisor;
			this.whole += w;
			numerator -= w * divisor;
		}

		final long n = this.numerator * divisor + numerator * this.divisor;

		if (n == 0) {
			this.numerator = 0;
			this.divisor = 1;
		} else {
			this.numerator = n;
			this.divisor = divisor * this.divisor;
			normalize();
		}

		return this;
	}

	public MutableFraction multiply(Fraction val) {
		return multiply(val.whole(), val.numerator(), val.divisor());
	}

	public MutableFraction multiply(long whole) {
		numerator *= whole;
		this.whole *= whole;
		normalize();
		return this;
	}

	public MutableFraction multiply(long numerator, long divisor) {
		return multiply(0, numerator, divisor);
	}

	public MutableFraction multiply(long whole, long numerator, long divisor) {
		if (numerator == 0) {
			return multiply(whole);
		}

		validate(whole, numerator, divisor);

		// normalize fractional part
		if (Math.abs(numerator) >= divisor) {
			final long w = numerator / divisor;
			whole += w;
			numerator -= w * divisor;
		}

		// avoids a division later to factor out common divisor from the two steps that follow this
		final long numeratorProduct = numerator * this.numerator;
		this.numerator *= divisor;
		numerator *= this.divisor;

		this.divisor *= divisor;
		this.numerator = this.numerator * whole + numerator * this.whole + numeratorProduct;
		this.whole *= whole;
		normalize();

		return this;
	}

	public MutableFraction subtract(Fraction val) {
		return add(-val.whole(), -val.numerator(), val.divisor());
	}

	public MutableFraction subtract(long whole) {
		return add(-whole, 0, 1);
	}

	public MutableFraction subtract(long numerator, long divisor) {
		return add(0, -numerator, divisor);
	}

	public MutableFraction negate() {
		numerator = -numerator;
		whole = -whole;
		return this;
	}

	/**
	 * Rounds down to multiple of divisor if not already divisible by it.
	 *
	 * @param divisor Desired multiple
	 */
	public void roundDown(long divisor) {
		if (this.divisor != divisor) {
			this.set(whole, numerator * divisor / this.divisor, divisor);
		}
	}

	public void readBuffer(FriendlyByteBuf buffer) {
		super.readBufferInner(buffer);
	}

	public void readTag(CompoundTag tag) {
		super.readTagInner(tag);
	}

	public static MutableFraction of(long whole, long numerator, long divisor) {
		return new MutableFraction(whole, numerator, divisor);
	}

	public static MutableFraction of(long numerator, long divisor) {
		return new MutableFraction(numerator, divisor);
	}

	public static MutableFraction of(long whole) {
		return new MutableFraction(whole);
	}

	@Override
	public Fraction toImmutable() {
		return Fraction.of(whole(), numerator(), divisor());
	}
}
