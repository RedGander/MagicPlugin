package com.elmakers.mine.bukkit.plugins.magic;

import java.util.Random;
import java.util.logging.Logger;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import com.elmakers.mine.bukkit.utilities.NMSUtils;

public class WandChestRunnable extends BukkitRunnable {
	World world;
	int dx = 1;
	int dz = 0;
	int segmentLength = 1;
	int x;
	int z;
	int segmentPassed = 0;
	int maxy;
	WandChestPopulator populator;
	Random random;
	Logger logger;
	
	public WandChestRunnable(Spells spells, World world, int maxy) {
		this.world = world;
		this.maxy = maxy;
		this.random = new Random();
		logger = spells.getPlugin().getLogger();
		populator = spells.getWandChestPopulator();
	}
	
	public void run() {
		Chunk chunk = world.getChunkAt(x, z);
		if (!NMSUtils.isDone(chunk) || !chunk.isLoaded()) {
			if (!NMSUtils.isDone(chunk) || !chunk.load(false)) {
				logger.info("Done populating chests, found ungenerated chunk");
				this.cancel();
			}
		} else {
			logger.info("Looking for chests in chunk at " + x + "," + z);
			populator.populate(world, random, chunk);
			x += dx;
			z += dz;
			segmentPassed++;
			if (segmentPassed == segmentLength) {
				segmentPassed = 0;
				int odx = dx;
				dx = -dz;
				dz = odx;
				if (dz == 0) {
					segmentLength++;
				}
			}
		}
	}
}