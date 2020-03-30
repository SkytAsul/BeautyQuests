package fr.skytasul.quests.editors.checkers;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

import fr.skytasul.quests.utils.Lang;

public class EnumParser<T extends Enum<T>> implements AbstractParser {

	private Map<String, T> names;
	private String namesString;

	public EnumParser(Class<T> enumClass) {
		try {
			T[] values = (T[]) enumClass.getDeclaredMethod("values").invoke(null);
			names = new HashMap<>(values.length + 1, 1);
			for (T value : values) {
				names.put(proceed(value.name()), value);
			}
			namesString = String.join(", ", names.keySet());
		}catch (ReflectiveOperationException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public T parse(Player p, String msg) throws Throwable {
		T obj = names.get(proceed(msg));
		if (obj == null) Lang.NO_SUCH_ELEMENT.send(p, namesString);
		return obj;
	}

	public String getNames() {
		return namesString;
	}

	private String proceed(String key) {
		return key.toLowerCase().replaceAll(" |_", "");
	}

}
