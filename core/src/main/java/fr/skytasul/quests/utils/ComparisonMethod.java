package fr.skytasul.quests.utils;

public enum ComparisonMethod {
	EQUALS(Lang.ComparisonEquals), DIFFERENT(Lang.ComparisonDifferent), LESS(Lang.ComparisonLess), LESS_OR_EQUAL(Lang.ComparisonLessOrEquals), GREATER(Lang.ComparisonGreater), GREATER_OR_EQUAL(Lang.ComparisonGreaterOrEquals);

	private Lang title;

	private ComparisonMethod(Lang title) {
		this.title = title;
	}

	public Lang getTitle() {
		return title;
	}

	public boolean isEqualOperation() {
		switch (this) {
		case EQUALS:
		case GREATER_OR_EQUAL:
		case LESS_OR_EQUAL:
			return true;
		default:
			return false;
		}
	}

	public boolean isNumberOperation() {
		switch (this) {
		case GREATER:
		case GREATER_OR_EQUAL:
		case LESS:
		case LESS_OR_EQUAL:
			return true;
		default:
			return false;
		}
	}

	public boolean test(double diff) {
		if (diff == 0) return isEqualOperation();
		if (this == ComparisonMethod.DIFFERENT) return true;
		if (diff > 0) return this == ComparisonMethod.GREATER || this == ComparisonMethod.GREATER_OR_EQUAL;
		if (diff < 0) return this == ComparisonMethod.LESS || this == ComparisonMethod.LESS_OR_EQUAL;
		return false;
	}
}