package grondag.fluidity.test;

import org.junit.jupiter.api.Test;

import grondag.fluidity.api.fraction.MutableFraction;

class FractionTest {
	@Test
	void test() {
		final MutableFraction f = MutableFraction.of(10);

		assert f.whole() == 10;
		assert f.numerator() == 0;
		assert f.divisor() == 1;

		f.subtract(4, 3);

		System.out.println(f.toString());

		f.subtract(7175, 1000);

		System.out.println(f.toString());

		f.add(4, 3);

		System.out.println(f.toString());

		f.add(7175, 1000);

		System.out.println(f.toString());

		f.subtract(10);

		System.out.println(f.toString());

		assert f.isZero();

		f.set(3, 1, 2);
		f.multiply(2);
		assert f.whole() == 7;
		assert f.numerator() == 0;

		f.set(2503, 3, 4);
		f.multiply(-987, -69, 100);

		assert f.whole() == -2472928;
		assert f.numerator() == -67;
		assert f.divisor() == 80;
	}
}
