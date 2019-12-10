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
