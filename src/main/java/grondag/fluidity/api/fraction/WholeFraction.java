package grondag.fluidity.api.fraction;

public class WholeFraction implements FractionView {
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
