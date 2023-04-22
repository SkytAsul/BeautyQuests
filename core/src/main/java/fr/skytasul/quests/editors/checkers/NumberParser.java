package fr.skytasul.quests.editors.checkers;

import java.math.BigDecimal;
import org.bukkit.entity.Player;
import fr.skytasul.quests.utils.Lang;

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
	public T parse(Player p, String msg) {
		try{
			String tname = numberType != Integer.class ? numberType.getSimpleName() : "Int";
			T number = (T) numberType.getDeclaredMethod("parse" + tname, String.class).invoke(null, msg);
			if (positive || noZero){
				int compare = new BigDecimal(msg).compareTo(new BigDecimal(0));
				if (positive && compare < 0){
					Lang.NUMBER_NEGATIVE.send(p);
					return null;
				}else if (noZero && compare == 0) {
					Lang.NUMBER_ZERO.send(p);
					return null;
				}
			}
			if (min != null || max != null) {
				BigDecimal bd = new BigDecimal(msg);
				if ((min != null && bd.compareTo(min) < 0) || (max != null && bd.compareTo(max) > 0)) {
					Lang.NUMBER_NOT_IN_BOUNDS.send(p, min, max);
					return null;
				}
			}
			return number;
		}catch (Exception ex) {}
		Lang.NUMBER_INVALID.send(p, msg);
		return null;
	}

}