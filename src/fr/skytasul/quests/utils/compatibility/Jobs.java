package fr.skytasul.quests.utils.compatibility;

import org.bukkit.entity.Player;

import com.gamingmesh.jobs.container.Job;
import com.gamingmesh.jobs.container.JobProgression;
import com.gamingmesh.jobs.container.JobsPlayer;

public class Jobs {

	public static int getLevel(Player p, String jobName){
		JobsPlayer player = com.gamingmesh.jobs.Jobs.getPlayerManager().getJobsPlayer(p);
		if (player == null) return -1;
		JobProgression prog = player.getJobProgression((Job) getJob(jobName));
		if (prog == null) return 0;
		return prog.getLevel();
	}
	
	public static Object getJob(String jobName){
		return com.gamingmesh.jobs.Jobs.getJob(jobName);
	}
	
}
