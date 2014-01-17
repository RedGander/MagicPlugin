package com.elmakers.mine.bukkit.blocks;

import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.plugins.magic.Mage;
import com.elmakers.mine.bukkit.plugins.magic.MaterialBrush;
import com.elmakers.mine.bukkit.plugins.magic.Spell;

public class ConstructBatch extends VolumeBatch {
	
	private int             timeToLive              = 0;
	private final Set<Material>	indestructible;
	private final BlockList constructedBlocks = new BlockList();
	private final Location 		center;
	private final int radius;
	private final MaterialBrush brush;
	private boolean checkDestructible = true;
	private final ConstructionType type;
	private final boolean fill;
	private final Mage playerSpells;
	private final Spell spell;
	private final String playerName;
	private final boolean spawnFallingBlocks;
	private Vector fallingBlockVelocity = null;
	
	private int x = 0;
	private int y = 0;
	private int z = 0;
	
	public ConstructBatch(Spell spell, Location center, ConstructionType type, int radius, boolean fill, MaterialBrush brush, Set<Material> indestructible, boolean spawnFallingBlocks) {
		super(spell.getMage().getController(), center.getWorld().getName());
		this.indestructible = indestructible;
		this.center = center;
		this.radius = radius;
		this.brush = brush;
		this.type = type;
		this.fill = fill;
		this.spawnFallingBlocks = spawnFallingBlocks;
		this.playerSpells = spell.getMage();
		this.spell = spell;
		this.playerName = this.playerSpells.getPlayer().getName();
	}
	
	public void setFallingBlockVelocity(Vector velocity) {
		fallingBlockVelocity = velocity;
	}
	
	public int process(int maxBlocks) {
		int processedBlocks = 0;
		
		while (processedBlocks <= maxBlocks && x <= radius) {
			if (!fillBlock(x, y, z)) {
				return processedBlocks;
			}
			
			y++;
			if (y > radius) {
				y = 0;
				z++;
				if (z > radius) {
					z = 0;
					x++;
				}
			}
			processedBlocks++;
		}
		
		if (x > radius) 
		{
			finish();
		}
		
		return processedBlocks;
	}
	
	@Override
	protected void finish() {
		super.finish();
		
		if (timeToLive == 0)
		{
			spells.addToUndoQueue(playerName, constructedBlocks);
		}
		else
		{
			constructedBlocks.setTimeToLive(timeToLive);
			spells.scheduleCleanup(playerName, constructedBlocks);
		}
		spell.castMessage("Constructed " + constructedBlocks.size() + " blocks");
	}

	public boolean fillBlock(int x, int y, int z)
	{
		boolean fillBlock = false;
		switch(type) {
			case SPHERE:
				int maxDistanceSquared = radius * radius;
				float mx = (float)x - 0.5f;
				float my = (float)y - 0.5f;
				float mz = (float)z - 0.5f;
				
				int distanceSquared = (int)((mx * mx) + (my * my) + (mz * mz));
				if (fill)
				{
					fillBlock = distanceSquared <= maxDistanceSquared;
				} 
				else 
				{
					mx++;
					my++;
					mz++;
					int outerDistanceSquared = (int)((mx * mx) + (my * my) + (mz * mz));
					fillBlock = maxDistanceSquared >= distanceSquared && maxDistanceSquared <= outerDistanceSquared;
				}
				//spells.getLog().info("(" + x + "," + y + "," + z + ") : " + fillBlock + " = " + distanceSquared + " : " + maxDistanceSquared);
				break;
			case PYRAMID:
				int elevation = radius - y;
				if (fill) {
					fillBlock = (x <= elevation) && (z <= elevation);
				} else {
					fillBlock = (x == elevation && z <= elevation) || (z == elevation && x <= elevation);
				}
				break;
			default: 
				fillBlock = fill ? true : (x == radius || y == radius || z == radius);
				break;
		}
		boolean success = true;
		if (fillBlock)
		{
			success = success && constructBlock(x, y, z);
			success = success && constructBlock(-x, y, z);
			success = success && constructBlock(x, -y, z);
			success = success && constructBlock(x, y, -z);
			success = success && constructBlock(-x, -y, z);
			success = success && constructBlock(x, -y, -z);
			success = success && constructBlock(-x, y, -z);
			success = success && constructBlock(-x, -y, -z);
		}
		return success;
	}

	public int getDistanceSquared(int x, int y, int z)
	{
		return x * x + y * y + z * z;
	}

	@SuppressWarnings("deprecation")
	public boolean constructBlock(int dx, int dy, int dz)
	{
		int x = center.getBlockX() + dx;
		int y = center.getBlockY() + dy;
		int z = center.getBlockZ() + dz;
		if (y < 0 || y > 255) return true;
		
		Block block = center.getWorld().getBlockAt(x, y, z);
		brush.update(block.getLocation());
		if (!block.getChunk().isLoaded()) {
			block.getChunk().load();
			return false;
		}
		if (!brush.isReady()) {
			brush.prepare();
			return false;
		}
		if (playerSpells.isIndestructible(block)) 
		{
			return true;
		}
		if (checkDestructible && !isDestructible(block))
		{
			return true;
		}
		if (!playerSpells.hasBuildPermission(block)) 
		{
			return true;
		}
		
		Material previousMaterial = block.getType();
		byte previousData = block.getData();
		
		if (previousMaterial != brush.getMaterial() || previousData != brush.getData()) {			
			updateBlock(center.getWorld().getName(), x, y, z);
			constructedBlocks.add(block);
			block.setType(brush.getMaterial());
			block.setData(brush.getData());
			if (spawnFallingBlocks) {
				FallingBlock falling = block.getWorld().spawnFallingBlock(block.getLocation(), previousMaterial, previousData);
				falling.setDropItem(false);
				if (fallingBlockVelocity != null) {
					falling.setVelocity(fallingBlockVelocity);
				}
			}
		}
		return true;
	}

	protected boolean isDestructible(Block block)
	{
		if (indestructible.isEmpty()) {
			return playerSpells.getController().getDestructibleMaterials().contains(block.getType());
		}
		return !indestructible.contains(block.getType());
	}
	
	public void setTimeToLive(int timeToLive) {
		this.timeToLive = timeToLive;
	}
	
	public void setCheckDestructible(boolean check) {
		this.checkDestructible = check;
	}
}