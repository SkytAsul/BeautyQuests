package fr.skytasul.quests.editors.checkers;

import java.math.BigDecimal;

import org.bukkit.entity.Player;

import fr.skytasul.quests.utils.Lang;

public class NumberParser implements AbstractParser {
	
	private Class<? extends Number> numberType;
	private boolean positive;
	private boolean noZero;
	
	public NumberParser(Class<? extends Number> numberType, boolean positive){
		this(numberType, positive, false);
	}
	
	public NumberParser(Class<? extends Number> numberType, boolean positive, boolean noZero){
		this.numberType = numberType;
		this.positive = positive;
		this.noZero = noZero;
	}
	
	
	public Object parse(Player p, String msg) throws Throwable{
		try{
			String tname = numberType != Integer.class ? numberType.getSimpleName() : "Int";
			Number number = (Number) numberType.getDeclaredMethod("parse" + tname, String.class).invoke(null, msg);
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
			return number;
		}catch (Throwable ex){}
		Lang.NUMBER_INVALID.send(p, msg);
		return null;
	}

}
