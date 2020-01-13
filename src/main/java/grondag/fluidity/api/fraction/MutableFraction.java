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

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.PacketByteBuf;

import grondag.fluidity.impl.AbstractFraction;

@API(status = Status.EXPERIMENTAL)
public final class MutableFraction extends AbstractFraction {
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

	public MutableFraction(FractionView template) {
		super(template.whole(), template.numerator(), template.divisor());
	}

	public MutableFraction(Tag tag) {
		readTag((CompoundTag) tag);
	}

	public MutableFraction(PacketByteBuf buf) {
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

	public MutableFraction set(FractionView template) {
		whole = template.whole();
		numerator = template.numerator();
		divisor = template.divisor();
		return this;
	}

	public MutableFraction add(FractionView val) {
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

	public MutableFraction subtract(FractionView val) {
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

	public MutableFraction multiply(long whole) {
		numerator *= whole;
		this.whole *= whole;
		normalize();
		return this;
	}

	/**
	 * Rounds down to multiple of divisor if not already divisible by it.
	 * @param divisor Desired multiple
	 */
	public void floor(long divisor) {
		if(this.divisor != divisor) {
			this.set(whole, numerator * divisor / this.divisor, divisor);
		}
	}

	@Override
	public void readBuffer(PacketByteBuf buffer) {
		super.readBuffer(buffer);
	}

	@Override
	public void readTag(CompoundTag tag) {
		super.readTag(tag);
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
}
