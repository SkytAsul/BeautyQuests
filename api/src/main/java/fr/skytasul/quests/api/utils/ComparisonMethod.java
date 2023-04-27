package fr.skytasul.quests.api.utils;

import fr.skytasul.quests.api.editors.checkers.EnumParser;
import fr.skytasul.quests.api.localization.Lang;

public enum ComparisonMethod {
	EQUALS('=', Lang.ComparisonEquals),
	DIFFERENT('≠', Lang.ComparisonDifferent),
	LESS('<', Lang.ComparisonLess),
	LESS_OR_EQUAL('≤', Lang.ComparisonLessOrEquals),
	GREATER('>', Lang.ComparisonGreater),
	GREATER_OR_EQUAL('≥', Lang.ComparisonGreaterOrEquals);

	private char symbol;
	private Lang title;
	
	private static final EnumParser<ComparisonMethod> COMPARISON_PARSER = new EnumParser<>(ComparisonMethod.class);

	private ComparisonMethod(char symbol, Lang title) {
		this.symbol = symbol;
		this.title = title;
	}
	
	public char getSymbol() {
		return symbol;
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
	
	public static EnumParser<ComparisonMethod> getComparisonParser() {
		return COMPARISON_PARSER;
	}
	
}