package fr.skytasul.quests.api.editors.parsers;

import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.utils.messaging.PlaceholderRegistry;

import java.math.BigDecimal;

public class NumberParser<T extends Number> implements AbstractParser<T> {

	public static final NumberParser<Integer> INTEGER_PARSER = new NumberParser<>(Integer.class, false, false);
	public static final NumberParser<Integer> INTEGER_PARSER_POSITIVE = new NumberParser<>(Integer.class, true, false);
	public static final NumberParser<Integer> INTEGER_PARSER_STRICT_POSITIVE = new NumberParser<>(Integer.class, true, true);
	public static final NumberParser<Double> DOUBLE_PARSER_STRICT_POSITIVE = new NumberParser<>(Double.class, true, true);

	private Class<T> numberType;
	private boolean positive;
	private boolean noZero;
	private BigDecimal min;
	private BigDecimal max;

	public NumberParser(Class<T> numberType, boolean positive) {
		this(numberType, positive, false);
	}

	public NumberParser(Class<T> numberType, boolean positive, boolean noZero) {
		this.numberType = numberType;
		this.positive = positive;
		this.noZero = noZero;
	}

	public NumberParser(Class<T> numberType, T min, T max) {
		this.numberType = numberType;
		this.min = new BigDecimal(min.doubleValue());
		this.max = new BigDecimal(max.doubleValue());
	}

	@Override
	public T parse(String msg) throws ParsingError {
		try{
			String tname = numberType != Integer.class ? numberType.getSimpleName() : "Int";
			T number = (T) numberType.getDeclaredMethod("parse" + tname, String.class).invoke(null, msg);
			if (positive || noZero){
				int compare = new BigDecimal(msg).compareTo(new BigDecimal(0));
				if (positive && compare < 0){
					throw new ParsingError(Lang.NUMBER_NEGATIVE.format());
				}else if (noZero && compare == 0) {
					throw new ParsingError(Lang.NUMBER_ZERO.format());
				}
			}
			if (min != null || max != null) {
				BigDecimal bd = new BigDecimal(msg);
				if ((min != null && bd.compareTo(min) < 0) || (max != null && bd.compareTo(max) > 0)) {
					throw new ParsingError(Lang.NUMBER_NOT_IN_BOUNDS.format(PlaceholderRegistry.of("min", min, "max", max)));
				}
			}
			return number;
		} catch (ParsingError ex) {
			throw ex;
		} catch (Exception ex) {
			throw new ParsingError(Lang.NUMBER_INVALID.format(PlaceholderRegistry.of("input", msg)));
		}
	}

}