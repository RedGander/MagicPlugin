package com.elmakers.mine.bukkit.spell.builtin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.TargetingSpell;

public class ThrustSpell extends TargetingSpell
{
	public static int tickSpan = 2; // How often velocity is applied, in scheduler ticks

	protected int targetHeight = 0;
	protected int checkCounter = 0;
	protected int groundHeight = 0;
	protected float hoverHeight = 0;
	protected long lastTick = 0;

	protected int checkFrequency = 10; // We'll check the ground every X steps 
	protected int maxTerrainChangeHeight = 4;

	protected int defaultHoverHeight = 5;
	protected int elevateRate = 32; // In blocks /s / s, at max speed 
	protected int maxSpeedAtElevation = 32;
	protected int maxSpeed = 20; // In blocks /s / s, at max speed and elevation
	protected int minSpeed = 6; // In blocks /s / s, at max speed and elevation

	protected float gravity = 0.98f; // anti-gravity force, in velocity/s (?)

	public class LevitateAction implements Runnable
	{
		protected HashMap<String, ThrustSpell> players = new HashMap<String, ThrustSpell>();
		protected BukkitScheduler scheduler;
		protected Plugin plugin;
		protected Server server;

		public LevitateAction(Plugin plugin)
		{
			this.plugin = plugin;
			this.server = plugin.getServer();
			this.scheduler = server.getScheduler();
		}

		public boolean isActive(Player player)
		{
			return players.containsKey(player.getName());
		}

		protected void scheduleForce()
		{
			scheduler.scheduleSyncDelayedTask(plugin, this, tickSpan);
		}

		public void run()
		{
			List<String> levitatingPlayers = new ArrayList<String>();
			levitatingPlayers.addAll(players.keySet());
			for (String playerName : levitatingPlayers)
			{
				ThrustSpell spell = players.get(playerName);
				if (spell.isActive())
				{
					spell.applyForce();
				}
				else
				{
					players.remove(playerName);
				}
			}

			if (players.size() > 0)
			{
				scheduleForce();
			}
		}
	}

	protected static LevitateAction action = null;

	protected void checkForGround()
	{
		checkCounter = 0;

		Location playerLocation = getLocation();
		World world = playerLocation.getWorld();
		Block targetBlock = world.getBlockAt(playerLocation);
		targetBlock = targetBlock.getRelative(BlockFace.DOWN);

		int newGroundHeight = targetBlock.getY();

		while (targetBlock.getType() == Material.AIR && newGroundHeight > 1)
		{
			newGroundHeight--;
			targetBlock = targetBlock.getRelative(BlockFace.DOWN);
		}

		// if the terrain has changed more than the auto-hover tolerance, re-adjust hover height and keep level.
		if (groundHeight == 0 || targetHeight == 0)
		{
			hoverHeight = mage.getEntity().getLocation().getBlockY() - newGroundHeight;
			if (hoverHeight < defaultHoverHeight)
			{
				hoverHeight = defaultHoverHeight;
			}
		}
		else if (Math.abs(newGroundHeight - groundHeight) > maxTerrainChangeHeight)
		{
			hoverHeight = targetHeight - newGroundHeight;
		}

		groundHeight = newGroundHeight;

		updateTargetHeight();
	}

	protected void updateTargetHeight()
	{
		targetHeight = (int)(hoverHeight + groundHeight);
		if (targetHeight > 255)
		{
			targetHeight = 255;
		} 
	}

	public boolean isActive()
	{
		return (!mage.isDead() && mage.isOnline());
	}

	protected void applyForce()
	{
		if (!isActive()) 
		{
			return;
		}

		// testing out a perf hack- don't send chunks while flinging!
		/*
        CraftPlayer cp = (CraftPlayer)player;
        EntityPlayer ep = cp.getHandle();
        Chunk chunk = player.getLocation().getBlock().getChunk();
        ep.chunkCoordIntPairQueue.clear();
        ep.chunkCoordIntPairQueue.add(new ChunkCoordIntPair(chunk.getX(), chunk.getZ()));
		 */

		// Calculate speeds based on previous delta, to try to adjust for server lag
		float timeDeltaSeconds = (System.currentTimeMillis() - lastTick) / 1000.0f;
		Vector force = new Vector(0, gravity * timeDeltaSeconds, 0);

		float elevateMagnitude = (float)elevateRate * timeDeltaSeconds;
		float speedMinMagnitude =  (float)minSpeed * timeDeltaSeconds;
		float speedMaxMagnitude =  (float)maxSpeed * timeDeltaSeconds;

		Location playerLocation = mage.getEntity().getLocation();

		float pitch = playerLocation.getPitch();
		float yaw = playerLocation.getYaw();

		Vector scaledForce = force.clone();

		// scaled based on distance from target height
		/// this is the main levitate action
		int playerHeight = playerLocation.getBlockY();
		int heightDelta = targetHeight - playerHeight;
		if (heightDelta > 0)
		{
			int heightBoost = heightDelta > 16 ? 16 : heightDelta;
			scaledForce.multiply(heightBoost);
		}
		else if (heightDelta < 0)
		{
			scaledForce.setY(0);
		}

		// Trying out a suggestion, in a hacky way- adjust pitch so that "level" is really looking down a bit
		pitch += 15;

		// Adjust target height based on aim
		Vector aim = new Vector
				(
						(0 - Math.sin(Math.toRadians(yaw))), 
						(0 - Math.sin(Math.toRadians(pitch))), 
						Math.cos(Math.toRadians(yaw))
						);
		aim.normalize();

		// only ascend if aiming mostly up, and if near the target
		if (heightDelta < 5 && pitch < -45)
		{
			hoverHeight += (elevateMagnitude * aim.getY());

			// We'll let the player go up higher than max height.
			if (hoverHeight > 255) hoverHeight = 255;
			if (hoverHeight < defaultHoverHeight) hoverHeight = defaultHoverHeight;
			updateTargetHeight();
		}

		// Periodically poll for ground level changes
		if (checkCounter++ > checkFrequency)
		{
			checkForGround();
		}

		// Steer- faster at higher altitudes, and scaled based on angle away from center (look up or down to stop)
		float multiplier = speedMinMagnitude;
        Player player = mage.getPlayer();
		if (player == null || !player.isSneaking())
		{
			int heightFactor = hoverHeight > maxSpeedAtElevation ? maxSpeedAtElevation : (int)hoverHeight;
			multiplier *= (float)speedMaxMagnitude * heightFactor / maxSpeedAtElevation;
		}
		float verticalMultipler =  1.0f - (float)Math.abs(aim.getY());
		aim.multiply(multiplier * verticalMultipler);
		aim.setY(0);
		scaledForce.add(aim);

		mage.getEntity().setVelocity(scaledForce);

		this.lastTick = System.currentTimeMillis();
	}

	@Override
	public SpellResult onCast(ConfigurationSection parameters) 
	{
		lastTick = System.currentTimeMillis();
		hoverHeight = defaultHoverHeight;

		if (action == null)
		{
			action = new LevitateAction(controller.getPlugin());
		}
		return SpellResult.CAST;
	}
}
