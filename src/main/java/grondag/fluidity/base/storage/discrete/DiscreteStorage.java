package grondag.fluidity.base.storage.discrete;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.fraction.Fraction;
import grondag.fluidity.api.fraction.FractionView;
import grondag.fluidity.api.storage.Storage;

public interface DiscreteStorage extends Storage {
	@Override
	default FractionView accept(Article item, FractionView volume, boolean simulate) {
		return volume.whole() == 0 ? Fraction.ZERO : Fraction.of(accept(item, volume.whole(), simulate));
	}

	@Override
	default FractionView supply(Article item, FractionView volume, boolean simulate) {
		return volume.whole() == 0 ? Fraction.ZERO : Fraction.of(supply(item, volume.whole(), simulate));
	}

	@Override
	default long accept(Article item, long numerator, long divisor, boolean simulate) {
		final long whole = numerator / divisor;
		return whole == 0 ? 0 : accept(item, whole, simulate);
	}

	@Override
	default long supply(Article item, long numerator, long divisor, boolean simulate) {
		final long whole = numerator / divisor;
		return whole == 0 ? 0 : supply(item, whole, simulate);
	}

	@Override
	default FractionView amount() {
		return Fraction.of(count());
	}

	@Override
	default FractionView volume() {
		return Fraction.of(capacity());
	}
}
