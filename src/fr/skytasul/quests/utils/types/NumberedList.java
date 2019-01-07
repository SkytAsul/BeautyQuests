package fr.skytasul.quests.utils.types;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import fr.skytasul.quests.utils.Utils;

public class NumberedList<T> implements Iterable<T>, Cloneable{

	private Map<Integer, T> map;
	
	public NumberedList(){
		this.map = new HashMap<>();
	}
	
	private NumberedList(Map<Integer, T> map){
		this.map = map;
	}
	
	public void set(int index, T value){
		map.remove(index);
		map.put(index, value);
	}
	
	public int add(T value){
		int i = 0;
		while(map.get(i) != null){
			i++;
			if (i > Short.MAX_VALUE) throw new InternalError("Maximum number of attempts reached.");
		}
		map.put(i, value);
		return i;
	}
	
	public T remove(int index, boolean resize){
		T obj = null;
		HashMap<Integer, T> tmp = new HashMap<>();
		for (Entry<Integer, T> en : map.entrySet()){
			int idd = en.getKey();
			if (idd < index){
				tmp.put(idd, en.getValue());
			}else if (idd == index){
				obj = en.getValue();
			}else {
				tmp.put(idd - 1, en.getValue());
			}
		}
		map = tmp;
		return obj;
	}
	
	public T remove(T value, boolean resize){
		return remove(Utils.getKeyByValue(map, value), resize);
	}
	
	public void insert(int index, T value){
		if (index > getLast()){
			add(value);
			return;
		}
		HashMap<Integer, T> tmp = new HashMap<>();
		for (Entry<Integer, T> en : map.entrySet()){
			int idd = en.getKey();
			if (idd < index){
				tmp.put(idd, en.getValue());
			}else {
				tmp.put(idd + 1, en.getValue());
			}
		}
		map = tmp;
		map.put(index, value);
	}
	
	public int getLast(){
		int a = 0;
		int n = 0;
		int i = 0;
		while(a < map.size()){
			if (map.get(i) != null){
				a++;
				n = i;
			}
			i++;
		}
		return n;
	}
	
	public int valuesSize(){
		return map.size();
	}
	
	public boolean isEmpty(){
		return map.isEmpty();
	}
	
	public T get(int index){
		return map.get(index);
	}
	
	public int indexOf(T value){
		return Utils.getKeyByValue(map, value);
	}
	
	public boolean contains(int id){
		return map.containsKey(id);
	}
	
	public int clear(){
		int size = map.size();
		map.clear();
		return size;
	}
	
	public NumberedList<T> clone(){
		return new NumberedList<>(new HashMap<>(map));
	}

	
	public Iterator<T> iterator(){
		return map.values().iterator();
	}
	
	public Map<Integer, T> getOriginalMap(){
		return map;
	}
	
}
