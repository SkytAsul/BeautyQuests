package fr.skytasul.quests.api.stages;

import java.util.ArrayList;
import java.util.List;

public class StageType{
	
	public static final List<StageType> types = new ArrayList<>();
	
	public final String id;
	public final Class<? extends AbstractStage> stageClass;
	public final String name;
	public final String dependCode;
	
	public StageType(String id, Class<? extends AbstractStage> clazz, String name){
		this(id, clazz, name, null);
	}
	
	public StageType(String id, Class<? extends AbstractStage> clazz, String name, String depend){
		this.id = id;
		this.stageClass = clazz;
		this.name = name;
		this.dependCode = depend;
	}
	
	public static StageType getStageType(String id){
		if (id == null) return null;
		for (StageType type : types){
			if (type.id.equals(id)) return type;
		}
		return null;
	}
	
}