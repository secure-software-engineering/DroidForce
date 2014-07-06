package de.tum.in.i22.uc.cm.distribution;

import java.util.Objects;

public abstract class AbstractDistributionStrategy {

	private final EDistributionStrategy _eStrategy;

	public AbstractDistributionStrategy(EDistributionStrategy eStrategy) {
		_eStrategy = eStrategy;
	}

	public final EDistributionStrategy getStrategy() {
		return _eStrategy;
	}


	@Override
	public final boolean equals(Object obj) {
		if (obj != null && obj.getClass().equals(this.getClass())) {
			return Objects.equals(_eStrategy, ((AbstractDistributionStrategy) obj)._eStrategy);
		}
		return false;
	}
}
