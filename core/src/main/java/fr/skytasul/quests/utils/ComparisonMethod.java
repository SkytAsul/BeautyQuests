package fr.skytasul.quests.utils;

public enum ComparisonMethod {
	EQUALS, DIFFERENT, LESS, LESS_OR_EQUAL, GREATER, GREATER_OR_EQUAL;

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