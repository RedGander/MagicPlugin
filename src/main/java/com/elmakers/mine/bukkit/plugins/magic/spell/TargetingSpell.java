package com.elmakers.mine.bukkit.plugins.magic.spell;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.spell.TargetType;
import com.elmakers.mine.bukkit.block.BlockAction;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.block.MaterialBrush;
import com.elmakers.mine.bukkit.plugins.magic.Mage;
import com.elmakers.mine.bukkit.utilities.ConfigurationUtils;
import com.elmakers.mine.bukkit.utilities.Target;

public abstract class TargetingSpell extends BaseSpell {

	private static final int  MAX_RANGE  = 511;
	
	private Target								target					= null;
	private String								targetName			    = null;
	private TargetType							targetType				= TargetType.OTHER;
	private boolean								targetNPCs				= false;
	private int                                 verticalSearchDistance  = 8;
	private boolean                             targetingComplete		= false;
	private boolean                             targetSpaceRequired     = false;
	private Class<? extends Entity>             targetEntityType        = null;
	private Location                            targetLocation;
	private Vector								targetLocationOffset;
	private World								targetLocationWorld;
	protected Location                          targetLocation2;
	private Entity								targetEntity = null;

	private boolean                             allowMaxRange           = false;

	private int                                 range                   = 32;
	
	private Set<Material>                       targetThroughMaterials  = new HashSet<Material>();
	private boolean                             reverseTargeting        = false;
	
	private BlockIterator						blockIterator = null;
	private	Block								currentBlock = null;
	private	Block								previousBlock = null;
	private	Block								previousPreviousBlock = null;

	private boolean     						pvpRestricted           	= false;
	private boolean								bypassPvpRestriction    	= false;
	
	@Override
	protected void preCast()
	{
		super.preCast();
		initializeTargeting();
	}

	protected void initializeTargeting()
	{
		blockIterator = null;
		targetSpaceRequired = false;
		reverseTargeting = false;
		targetingComplete = false;
	}
	
	public void setTargetType(TargetType t) {
		this.targetType = t;
		if (target != null) {
			target = null;
			initializeTargeting();
		}
	}
	
	public String getMessage(String messageKey, String def) {
		String message = super.getMessage(messageKey, def);
		
		// Escape targeting parameters
		String useTargetName = targetName;
		if (useTargetName == null) {
			if (target != null && target.hasEntity()) {
				if (target.getEntity() instanceof Player) {
					useTargetName = ((Player)target.getEntity()).getName();
				} else {
					useTargetName = target.getEntity().getType().name().toLowerCase().replace('_', ' ');
				}
			}
			else {
				useTargetName = "Unknown";
			}
		}
		message = message.replace("$target", useTargetName);
		
		return message;
	}
	
	protected void setTargetName(String name) {
		targetName = name;
	}

	public void targetThrough(Material mat)
	{
		targetThroughMaterials.add(mat);
	}

	public void targetThrough(Set<Material> mat)
	{
		targetThroughMaterials.clear();
		targetThroughMaterials.addAll(mat);
	}

	public void noTargetThrough(Material mat)
	{
		targetThroughMaterials.remove(mat);
	}
	
	public boolean isTargetable(Material mat)
	{
		if (!allowPassThrough(mat)) {
			return true;
		}
		boolean targetThrough = targetThroughMaterials.contains(mat);
		if (reverseTargeting)
		{
			return(targetThrough);
		}
		return !targetThrough;
	}

	public void setReverseTargeting(boolean reverse)
	{
		reverseTargeting = reverse;
	}

	public boolean isReverseTargeting()
	{
		return reverseTargeting;
	}

	public void setTargetSpaceRequired()
	{
		targetSpaceRequired = true;
	}

	public void setTarget(Location location) {
		target = new Target(getLocation(), location.getBlock());
	}

	protected void offsetTarget(int dx, int dy, int dz) {
		Location location = getLocation();
		if (location == null) {
			return;
		}
		location.add(dx, dy, dz);
		initializeBlockIterator(location);
	}
	
	protected boolean initializeBlockIterator(Location location) {
		if (location.getBlockY() < 0) {
			location = location.clone();
			location.setY(0);
		}
		if (location.getBlockY() > Spell.MAX_Y) {
			location = location.clone();
			location.setY(Spell.MAX_Y);
		}
		
		try {
			blockIterator = new BlockIterator(location, VIEW_HEIGHT, getMaxRange());
		} catch (Exception ex) {
			// This seems to happen randomly, like when you use the same target.
			// Very annoying, and I now kind of regret switching to BlockIterator.
			// At any rate, we're going to just re-use the last target block and 
			// cross our fingers!
			return false;
		}
		
		return true;
	}
	
	/**
	 * Move "steps" forward along line of vision and returns the block there
	 * 
	 * @return The block at the new location
	 */
	protected Block getNextBlock()
	{
		previousPreviousBlock = previousBlock;
		previousBlock = currentBlock;
		if (blockIterator == null || !blockIterator.hasNext()) {
			currentBlock = null;
		} else {
			currentBlock = blockIterator.next();
		}
		return currentBlock;
	}

	/**
	 * Returns the current block along the line of vision
	 * 
	 * @return The block
	 */
	public Block getCurBlock()
	{
		return currentBlock;
	}

	/**
	 * Returns the previous block along the line of vision
	 * 
	 * @return The block
	 */
	public Block getPreviousBlock()
	{
		return previousBlock;
	}
	
	public TargetType getTargetType()
	{ 
		return targetType;
	}
	
	protected Target getTarget()
	{
		target = findTarget();
		
		if (targetLocationOffset != null) {
			target.add(targetLocationOffset);
		}
		if (targetLocationWorld != null) {
			target.setWorld(targetLocationWorld);
		}
		return target;
	}

	/**
	 * Returns the block at the cursor, or null if out of range
	 * 
	 * @return The target block
	 */
	public Target findTarget()
	{
		if (targetType != TargetType.NONE && targetType != TargetType.BLOCK && targetEntity != null) {
			return new Target(getLocation(), targetEntity);
		}
		
		Player player = getPlayer();
		if (targetType == TargetType.SELF && player != null) {
			return new Target(getLocation(), player);
		}
		
		CommandSender sender = mage.getCommandSender();
		if (targetType == TargetType.SELF && player == null && sender != null && (sender instanceof BlockCommandSender)) {
			BlockCommandSender commandBlock = (BlockCommandSender)mage.getCommandSender();
			return new Target(commandBlock.getBlock().getLocation(), commandBlock.getBlock());
		}
		
		Location location = getLocation();
		if (targetType == TargetType.SELF && location != null) {
			return new Target(location, location.getBlock());
		}
		
		if (targetType == TargetType.SELF) {
			return new Target(location);
		}

		if (targetType != TargetType.NONE && targetLocation != null) {
			return new Target(getLocation(), targetLocation.getBlock());
		}
		
		if (targetType == TargetType.NONE) {
			return new Target(getLocation());
		}
		
		findTargetBlock();
		Block block = getCurBlock();

		if (targetType == TargetType.BLOCK) {
			return new Target(getLocation(), block);
		}

		Target targetBlock = block == null ? null : new Target(getLocation(), block);
		Target targetEntity = getEntityTarget();

		// Don't allow targeting entities in no-PVP areas.
		boolean noPvp = targetEntity != null && (targetEntity instanceof Player) && pvpRestricted && !bypassPvpRestriction && !controller.isPVPAllowed(targetEntity.getLocation());
		if (noPvp) {
			targetEntity = null;
			// Don't let the target the block, either.
			targetBlock = null;
		}
		
		if (targetEntity == null && targetType == TargetType.ANY && player != null) {
			return new Target(getLocation(), player, targetBlock == null ? null : targetBlock.getBlock());
		}
		
		if (targetBlock != null && targetEntity != null) {
			if (targetBlock.getDistance() < targetEntity.getDistance()) {
				targetEntity = null;
			} else {
				targetBlock = null;
			}
		}
		
		if (targetEntity != null) {
			return targetEntity;
		} else if (targetBlock != null) {
			return targetBlock;
		} 
		
		return new Target(getLocation());
	}
	
	public Target getCurrentTarget()
	{
		return target;
	}
	
	public void clearTarget()
	{
		target = null;
	}

	public Block getTargetBlock()
	{
		return getTarget().getBlock();
	}

	protected Target getEntityTarget()
	{
		if (targetEntityType == null) return null;
		List<Target> scored = getAllTargetEntities();
		if (scored.size() <= 0) return null;
		return scored.get(0);
	}
	
	protected List<Target> getAllTargetEntities() {
		List<Target> scored = new ArrayList<Target>();
		World world = getWorld();
		if (world == null) return scored;
		List<Entity> entities = world.getEntities();
		for (Entity entity : entities)
		{
			if (entity == getPlayer()) continue;
			if (!targetNPCs && entity.hasMetadata("NPC")) continue;
			if (targetEntityType != null && !(targetEntityType.isAssignableFrom(entity.getClass()))) continue;
			if (entity instanceof Player) {
				Mage targetMage = controller.getMage((Player)entity);
				if (targetMage.isSuperProtected()) continue;
			}

			Target newScore = new Target(getLocation(), entity, getMaxRange());
			if (newScore.getScore() > 0)
			{
				scored.add(newScore);
			}
		}

		Collections.sort(scored);
		return scored;
	}

	protected int getMaxRange()
	{
		if (allowMaxRange) return Math.min(MAX_RANGE, range);
		return Math.min(MAX_RANGE, (int)(mage.getRangeMultiplier() * range));
	}

	protected int getMaxRangeSquared()
	{
		int maxRange = getMaxRange();
		return maxRange * maxRange;
	}

	protected void setMaxRange(int range, boolean allow)
	{
		this.range = range;
		this.allowMaxRange = allow;
	}

	protected void setMaxRange(int range)
	{
		this.range = range;
	}
	
	protected boolean isTransparent(Material material)
	{
		return targetThroughMaterials.contains(material);
	}
	
	protected void applyPotionEffects(Location location, int radius, Collection<PotionEffect> potionEffects) {
		if (potionEffects == null || radius <= 0 || potionEffects.size() == 0) return;
		
		int radiusSquared = radius * 2;
		List<Entity> entities = location.getWorld().getEntities();
		for (Entity entity : entities) {
			if (entity instanceof LivingEntity) {
				if (entity instanceof Player) {
					Player targetPlayer = (Player)entity;
					boolean isSourcePlayer = targetPlayer.getName().equals(mage.getName());
					if (isSourcePlayer && getTargetType() != TargetType.ANY && getTargetType() != TargetType.SELF) {
						continue;
					}
					
					Mage targetMage = controller.getMage(targetPlayer);
					
					// Check for protected players
					if (targetMage.isSuperProtected() && !isSourcePlayer) {
						continue;
					}
				}
				
				if (targetEntityType != null && !(targetEntityType.isAssignableFrom(entity.getClass()))) continue;
				
				if (entity.getLocation().distanceSquared(location) < radiusSquared) {
					LivingEntity living = (LivingEntity)entity;
					living.addPotionEffects(potionEffects);
				}
			}
		}
	}

	protected void findTargetBlock()
	{
		Location location = getLocation();
		if (location == null) {
			return;
		}
		if (targetingComplete)
		{
			return;
		}
		if (!initializeBlockIterator(location)) {
			return;
		}
		currentBlock = null;
		previousBlock = null;
		previousPreviousBlock = null;

		Block block = getNextBlock();
		while (block != null)
		{
			if (targetSpaceRequired) {
				if (isOkToStandIn(block.getType()) && isOkToStandIn(block.getRelative(BlockFace.UP).getType())) {
					break;
				}
			} else {
				if (isTargetable(block.getType())) {
					break;
				}
			}
			block = getNextBlock();
		}
		if (block == null && allowMaxRange) {
			currentBlock = previousBlock;
			previousBlock = previousPreviousBlock;
		}
		targetingComplete = true;
	}
	
	public Block getInteractBlock() {
		Location location = getEyeLocation();
		if (location == null) return null;
		Block playerBlock = location.getBlock();
		if (isTargetable(playerBlock.getType())) return playerBlock;
		Vector direction = location.getDirection().normalize();
		return location.add(direction).getBlock();
	}

	public void coverSurface(Location center, int radius, BlockAction action)
	{   
		int y = center.getBlockY();
		for (int dx = -radius; dx < radius; ++dx)
		{
			for (int dz = -radius; dz < radius; ++dz)
			{
				if (isInCircle(dx, dz, radius))
				{
					int x = center.getBlockX() + dx;
					int z = center.getBlockZ() + dz;
					Block block = getWorld().getBlockAt(x, y, z);
					int depth = 0;

					if (targetThroughMaterials.contains(block.getType()))
					{
						while (depth < verticalSearchDistance && targetThroughMaterials.contains(block.getType()))
						{
							depth++;
							block = block.getRelative(BlockFace.DOWN);
						}   
					}
					else
					{
						while (depth < verticalSearchDistance && !targetThroughMaterials.contains(block.getType()))
						{
							depth++;
							block = block.getRelative(BlockFace.UP);
						}
						block = block.getRelative(BlockFace.DOWN);
					}
					Block coveringBlock = block.getRelative(BlockFace.UP);
					if (!targetThroughMaterials.contains(block.getType()) && targetThroughMaterials.contains(coveringBlock.getType()))
					{
						action.perform(block);
					}  
				} 
			}
		}
	}
	
	@Override
	protected void reset()
	{
		super.reset();
		
		this.target = null;
		this.targetName = null;
		this.targetLocation = null;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void processParameters(ConfigurationSection parameters) {
		super.processParameters(parameters);
		range = parameters.getInt("range", range);
		allowMaxRange = parameters.getBoolean("allow_max_range", allowMaxRange);
		
		bypassPvpRestriction = parameters.getBoolean("bypass_pvp", false);
		bypassPvpRestriction = parameters.getBoolean("bp", bypassPvpRestriction);
		
		if (parameters.contains("transparent")) {
			targetThroughMaterials.clear();
			targetThroughMaterials.addAll(controller.getMaterialSet(parameters.getString("transparent")));
		} else {
			targetThroughMaterials.clear();
			targetThroughMaterials.addAll(controller.getMaterialSet("transparent"));			
		}
		
		if (parameters.contains("target")) {
			String targetTypeName = parameters.getString("target");
			try {
				 targetType = TargetType.valueOf(targetTypeName.toUpperCase());
			} catch (Exception ex) {
				controller.getLogger().warning("Invalid target_type: " + targetTypeName);
				targetType = TargetType.OTHER;
			}
		} else {
			targetType = TargetType.OTHER;
		}
		
		targetNPCs = parameters.getBoolean("target_npc", false);
		
		if (parameters.contains("target_type")) {
			String entityTypeName = parameters.getString("target_type");
			try {
				 Class<?> typeClass = Class.forName("org.bukkit.entity." + entityTypeName);
				 if (Entity.class.isAssignableFrom(typeClass)) {
					 targetEntityType = (Class<? extends Entity>)typeClass;
				 } else {
					 controller.getLogger().warning("Entity type: " + entityTypeName + " not assignable to Entity");
				 }
			} catch (Throwable ex) {
				controller.getLogger().warning("Unknown entity type: " + entityTypeName);
				targetEntityType = null;
			}
		}
		
		Location defaultLocation = getLocation();
		targetLocation = ConfigurationUtils.getLocationOverride(parameters, "t", defaultLocation);
		targetLocationOffset = null;
		
		Double otxValue = ConfigurationUtils.getDouble(parameters, "otx", null);
		Double otyValue = ConfigurationUtils.getDouble(parameters, "oty", null);
		Double otzValue = ConfigurationUtils.getDouble(parameters, "otz", null);
		if (otxValue != null || otzValue != null || otyValue != null) {
			targetLocationOffset = new Vector(
					(otxValue == null ? 0 : otxValue),
					(otyValue == null ? 0 : otyValue), 
					(otzValue == null ? 0 : otzValue));
		}
		targetLocationWorld = null;
		String otWorldName = parameters.getString("otworld", null);
		if (otWorldName != null && otWorldName.length() > 0) {
			targetLocationWorld = Bukkit.getWorld(otWorldName);
		}
		
		// For two-click construction spells
		defaultLocation = targetLocation == null ? defaultLocation : targetLocation;		
		targetLocation2 = ConfigurationUtils.getLocationOverride(parameters, "t2", defaultLocation);
		
		if (parameters.contains("player")) {
			Player player = controller.getPlugin().getServer().getPlayer(parameters.getString("player"));
			if (player != null) {
				targetLocation = player.getLocation();
				targetEntity = player;
			}
		} else {
			targetEntity = null;
		}
		
		// Special hack that should work well in most casts.
		if (isUnderwater()) {
			targetThroughMaterials.add(Material.WATER);
			targetThroughMaterials.add(Material.STATIONARY_WATER);
		}
	}
	
	@Override
	protected void loadTemplate(ConfigurationSection node)
	{
		super.loadTemplate(node);
		pvpRestricted = node.getBoolean("pvp_restricted", pvpRestricted);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected String getDisplayMaterialName()
	{
		if (target != null && target.isValid()) {
			return MaterialBrush.getMaterialName(target.getBlock().getType(), target.getBlock().getData());
		}
		
		return super.getDisplayMaterialName();
	}
	
	@Override
	protected boolean canCast() {
		return !pvpRestricted || bypassPvpRestriction || controller.isPVPAllowed(mage.getLocation()) || mage.isSuperPowered();
	}
	
	@Override
	protected void onBackfire() {
		targetType = TargetType.SELF;
	}
	
	@Override
	public Location getTargetLocation() {
		if (target != null && target.isValid()) {
			return target.getLocation();
		}
		
		return null;
	}
	
	@Override
	public Entity getTargetEntity() {
		if (target != null && target.isValid()) {
			return target.getEntity();
		}
		
		return null;
	}

	@Override
	public MaterialAndData getEffectMaterial()
	{
		if (target != null && target.isValid()) {
			Block block = target.getBlock();
			MaterialAndData targetMaterial = new MaterialAndData(block);
			if (targetMaterial.getMaterial() == Material.AIR) {
				targetMaterial.setMaterial(DEFAULT_EFFECT_MATERIAL);
			}
			return targetMaterial;
		}
		return super.getEffectMaterial();
	}
}