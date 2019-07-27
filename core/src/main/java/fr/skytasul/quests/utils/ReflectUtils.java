package fr.skytasul.quests.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectUtils {

	private String pack;
	
	private ReflectUtils(String pack){
		this.pack = pack;
	}
	
	public Class<?> fromName(String className){
		try {
			return Class.forName(pack + className);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public Class<?> fromNameDotName(String before, String after) throws ClassNotFoundException{
		return getClassDotClass(fromName(before), after);
	}
	
	public static ReflectUtils fromPackage(String pack){
		return new ReflectUtils(pack + ".");
	}
	
	
	public static Class<?> getClassDotClass(Class<?> clazz, String after) throws ClassNotFoundException{
		for (Class<?> c : clazz.getClasses()){
			if (c.getSimpleName().equals(after)) return c;
		}
		throw new ClassNotFoundException(clazz.getName() + "." + after);
	}
	
	public Object fromEnum(String name, int ordinal){
		try {
			return fromEnum(Class.forName(pack + name), ordinal);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/*public static Constructor<?> getConstructor(Class<?> clazz, Class<?>... parameters){
		List<Class<?>> ls = new ArrayList<>(Arrays.asList(parameters));
		for (Constructor<?> c : clazz.getDeclaredConstructors()){
			if (c.getParameterCount() == 0 && parameters.length == 0) continue;
			boolean finded = true;
			//System.out.println("parameters " + c.getParameterCount());
			for (Class<?> cl : c.getParameterTypes()){
				//System.out.println(cl.getName());
				if (!ls.contains(cl)) finded = false;
			}
			if (finded) return c;
		}
		return null;
	}*/
	
	public static Object fromEnum(Class<?> clazz, int ordinal){
		return clazz.getEnumConstants()[ordinal];
	}
	
	public static Object getFieldValue(Field field, Object instance) throws IllegalArgumentException, IllegalAccessException{
		field.setAccessible(true);
		return field.get(instance);
	}
	
	public static void setFieldValue(Field field, Object instance, Object object) throws IllegalArgumentException, IllegalAccessException{
		field.setAccessible(true);
		field.set(instance, object);
	}
	
	public static String listConstructors(Class<?> clazz){
		String s = "s" + clazz.getDeclaredConstructors().length + " ";
		for (Constructor<?> c : clazz.getDeclaredConstructors()){
			s = s + c.getParameterTypes() + "; ";
		}
		return s;
	}
	
	public static void invoke(Object obj, Method method, Object... params) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		method.setAccessible(true);
		method.invoke(null, params);
	}
	
	public static <T> T newInstance(Constructor<T> constructor, Object... params) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		constructor.setAccessible(true);
		/*if (constructor.getParameters().length != 0){
			//for (Parameter param : constructor.getParameters()) Bukkit.broadcastMessage(param.getType().getName());
		}*/
		return constructor.newInstance(params);
	}
	
}
