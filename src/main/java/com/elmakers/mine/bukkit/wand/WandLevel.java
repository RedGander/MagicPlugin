package com.elmakers.mine.bukkit.wand;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

import com.elmakers.mine.bukkit.api.spell.CastingCost;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.block.MaterialBrush;
import com.elmakers.mine.bukkit.spell.BrushSpell;
import com.elmakers.mine.bukkit.utility.RandomUtils;
import com.elmakers.mine.bukkit.utility.WeightedPair;

public class WandLevel {
	private static TreeMap<Integer, WandLevel> levelMap = null;
	private static int[] levels = null;
	
	private final LinkedList<WeightedPair<Integer>> spellCountProbability = new LinkedList<WeightedPair<Integer>>();
	private final LinkedList<WeightedPair<Integer>> materialCountProbability = new LinkedList<WeightedPair<Integer>>();
	private final LinkedList<WeightedPair<String>> spellProbability = new LinkedList<WeightedPair<String>>();
	private final LinkedList<WeightedPair<String>> materialProbability = new LinkedList<WeightedPair<String>>();
	private final LinkedList<WeightedPair<Integer>> useProbability = new LinkedList<WeightedPair<Integer>>();
	private final LinkedList<WeightedPair<Integer>> addUseProbability = new LinkedList<WeightedPair<Integer>>();

	private final LinkedList<WeightedPair<Integer>> propertyCountProbability = new LinkedList<WeightedPair<Integer>>();
	private final LinkedList<WeightedPair<Float>> costReductionProbability = new LinkedList<WeightedPair<Float>>();
	private final LinkedList<WeightedPair<Float>> powerProbability = new LinkedList<WeightedPair<Float>>();
	private final LinkedList<WeightedPair<Float>> damageReductionProbability = new LinkedList<WeightedPair<Float>>();
	private final LinkedList<WeightedPair<Float>> damageReductionPhysicalProbability = new LinkedList<WeightedPair<Float>>();
	private final LinkedList<WeightedPair<Float>> damageReductionProjectilesProbability = new LinkedList<WeightedPair<Float>>();
	private final LinkedList<WeightedPair<Float>> damageReductionFallingProbability = new LinkedList<WeightedPair<Float>>();
	private final LinkedList<WeightedPair<Float>> damageReductionFireProbability = new LinkedList<WeightedPair<Float>>();
	private final LinkedList<WeightedPair<Float>> damageReductionExplosionsProbability = new LinkedList<WeightedPair<Float>>();
	
	private final LinkedList<WeightedPair<Integer>> xpRegenerationProbability = new LinkedList<WeightedPair<Integer>>();
	private final LinkedList<WeightedPair<Integer>> xpMaxProbability = new LinkedList<WeightedPair<Integer>>();
	private final LinkedList<WeightedPair<Integer>> healthRegenerationProbability = new LinkedList<WeightedPair<Integer>>();
	private final LinkedList<WeightedPair<Integer>> hungerRegenerationProbability = new LinkedList<WeightedPair<Integer>>();
	
	private final LinkedList<WeightedPair<Float>> hasteProbability = new LinkedList<WeightedPair<Float>>();
	
	// TODO- Config-driven!
	public static float maxValue = 1.0f;
	public static int maxUses = 500;
	public static int maxMaxXp = 1500;
	public static int maxXpRegeneration = 150;
	public static float maxHungerRegeneration = 5;
	public static float maxHealthRegeneration = 5;
	public static float maxDamageReduction = 0.4f;
	public static float maxDamageReductionExplosions = 0.3f;
	public static float maxDamageReductionFalling = 0.9f;
	public static float maxDamageReductionFire = 0.5f;
	public static float maxDamageReductionPhysical = 0.1f;
	public static float maxDamageReductionProjectiles = 0.2f;
	public static float maxCostReduction = 0.5f;
	public static float maxCooldownReduction = 0.5f;
	public static float maxHasteLevel = 20;
	public static int minLevel = 10;
	public static int maxLevel = 40;
	
	public static void load(ConfigurationSection properties) {
		maxValue = (float)properties.getDouble("max_property_value", maxValue);
		maxUses = properties.getInt("max_uses", maxUses);
		maxMaxXp = properties.getInt("max_mana", maxMaxXp);
		maxXpRegeneration = properties.getInt("max_mana_regeneration", maxXpRegeneration);
		maxHealthRegeneration = (float)properties.getDouble("max_health_regeneration", maxHealthRegeneration);
		maxHungerRegeneration = (float)properties.getDouble("max_hunger_regeneration", maxHungerRegeneration);

		minLevel = properties.getInt("min_enchant_level", minLevel);
		maxLevel = properties.getInt("max_enchant_level", maxLevel);

		maxDamageReduction = (float)properties.getDouble("max_damage_reduction", maxDamageReduction);
		maxDamageReduction = (float)properties.getDouble("max_damage_reduction_explosions", maxDamageReductionExplosions);
		maxDamageReduction = (float)properties.getDouble("max_damage_reduction_falling", maxDamageReductionFalling);
		maxDamageReduction = (float)properties.getDouble("max_damage_reduction_fire", maxDamageReductionFire);
		maxDamageReduction = (float)properties.getDouble("max_damage_reduction_physical", maxDamageReductionPhysical);
		maxDamageReduction = (float)properties.getDouble("max_damage_reduction_projectiles", maxDamageReductionProjectiles);
		maxCostReduction = (float)properties.getDouble("max_cost_reduction", maxCostReduction);
		maxCooldownReduction = (float)properties.getDouble("max_cooldown_reduction", maxCooldownReduction);
		maxHasteLevel = (float)properties.getDouble("max_haste", maxHasteLevel);
	}
	
	public static WandLevel getLevel(int level) {
		if (levelMap == null) return null;
		
		if (!levelMap.containsKey(level)) {
			if (level > levelMap.lastKey()) {
				return levelMap.lastEntry().getValue();
			}
			
			return levelMap.firstEntry().getValue();
		}
		
		return levelMap.get(level);
	}
	
	public static void mapLevels(ConfigurationSection template) {
		// Parse defined levels
		levelMap = new TreeMap<Integer, WandLevel>();
		String[] levelStrings = StringUtils.split(template.getString("levels"), ",");
		levels = new int[levelStrings.length];
		for (int i = 0; i < levels.length; i++) {
			levels[i] = Integer.parseInt(levelStrings[i]);
		}
		
		for (int level = 1; level <= levels[levels.length - 1]; level++) {
			levelMap.put(level, new WandLevel(level, template));
		}
	}
	
	private WandLevel(int level, ConfigurationSection template) {
		int levelIndex = 0;
		int nextLevelIndex = 0;
		float distance = 1;
		for (levelIndex = 0; levelIndex < levels.length; levelIndex++) {
			if (level == levels[levelIndex] || levelIndex == levels.length - 1) {
				nextLevelIndex = levelIndex;
				distance = 0;
				break;
			}
			
			if (level > levels[levelIndex]) {
				nextLevelIndex = levelIndex + 1;
				int previousLevel = levels[levelIndex];
				int nextLevel = levels[nextLevelIndex];				
				distance = (float)(level - previousLevel) / (float)(nextLevel - previousLevel);
			}
		}
		
		// Fetch spell probabilities
		com.elmakers.mine.bukkit.utility.RandomUtils.populateStringProbabilityMap(spellProbability, template.getConfigurationSection("spells"), levelIndex, nextLevelIndex, distance);
		
		// Fetch spell count probabilities
		com.elmakers.mine.bukkit.utility.RandomUtils.populateIntegerProbabilityMap(spellCountProbability, template.getConfigurationSection("spell_count"), levelIndex, nextLevelIndex, distance);
		
		// Fetch material probabilities
		com.elmakers.mine.bukkit.utility.RandomUtils.populateStringProbabilityMap(materialProbability, template.getConfigurationSection("materials"), levelIndex, nextLevelIndex, distance);
		
		// Fetch material count probabilities
		com.elmakers.mine.bukkit.utility.RandomUtils.populateIntegerProbabilityMap(materialCountProbability, template.getConfigurationSection("material_count"), levelIndex, nextLevelIndex, distance);
		
		// Fetch uses
		com.elmakers.mine.bukkit.utility.RandomUtils.populateIntegerProbabilityMap(useProbability, template.getConfigurationSection("uses"), levelIndex, nextLevelIndex, distance);
		com.elmakers.mine.bukkit.utility.RandomUtils.populateIntegerProbabilityMap(addUseProbability, template.getConfigurationSection("add_uses"), levelIndex, nextLevelIndex, distance);
		
		// Fetch property count probability
		com.elmakers.mine.bukkit.utility.RandomUtils.populateIntegerProbabilityMap(propertyCountProbability, template.getConfigurationSection("property_count"), levelIndex, nextLevelIndex, distance);
		
		// Fetch cost and damage reduction
		com.elmakers.mine.bukkit.utility.RandomUtils.populateFloatProbabilityMap(costReductionProbability, template.getConfigurationSection("cost_reduction"), levelIndex, nextLevelIndex, distance);
		com.elmakers.mine.bukkit.utility.RandomUtils.populateFloatProbabilityMap(damageReductionProbability, template.getConfigurationSection("protection"), levelIndex, nextLevelIndex, distance);
		com.elmakers.mine.bukkit.utility.RandomUtils.populateFloatProbabilityMap(damageReductionPhysicalProbability, template.getConfigurationSection("protection_physical"), levelIndex, nextLevelIndex, distance);
		com.elmakers.mine.bukkit.utility.RandomUtils.populateFloatProbabilityMap(damageReductionFallingProbability, template.getConfigurationSection("protection_falling"), levelIndex, nextLevelIndex, distance);
		com.elmakers.mine.bukkit.utility.RandomUtils.populateFloatProbabilityMap(damageReductionProjectilesProbability, template.getConfigurationSection("protection_projectiles"), levelIndex, nextLevelIndex, distance);
		com.elmakers.mine.bukkit.utility.RandomUtils.populateFloatProbabilityMap(damageReductionFireProbability, template.getConfigurationSection("protection_fire"), levelIndex, nextLevelIndex, distance);
		com.elmakers.mine.bukkit.utility.RandomUtils.populateFloatProbabilityMap(damageReductionExplosionsProbability, template.getConfigurationSection("protection_explosions"), levelIndex, nextLevelIndex, distance);

		// Fetch regeneration
		com.elmakers.mine.bukkit.utility.RandomUtils.populateIntegerProbabilityMap(xpRegenerationProbability, template.getConfigurationSection("xp_regeneration"), levelIndex, nextLevelIndex, distance);
		com.elmakers.mine.bukkit.utility.RandomUtils.populateIntegerProbabilityMap(xpMaxProbability, template.getConfigurationSection("xp_max"), levelIndex, nextLevelIndex, distance);
		com.elmakers.mine.bukkit.utility.RandomUtils.populateIntegerProbabilityMap(healthRegenerationProbability, template.getConfigurationSection("health_regeneration"), levelIndex, nextLevelIndex, distance);
		com.elmakers.mine.bukkit.utility.RandomUtils.populateIntegerProbabilityMap(hungerRegenerationProbability, template.getConfigurationSection("hunger_regeneration"), levelIndex, nextLevelIndex, distance);
		
		// Fetch haste
		com.elmakers.mine.bukkit.utility.RandomUtils.populateFloatProbabilityMap(hasteProbability, template.getConfigurationSection("haste"), levelIndex, nextLevelIndex, distance);
		
		// Fetch power
		com.elmakers.mine.bukkit.utility.RandomUtils.populateFloatProbabilityMap(powerProbability, template.getConfigurationSection("power"), levelIndex, nextLevelIndex, distance);		
	}
	
	private boolean randomizeWand(Wand wand, boolean additive) {
		// Add random spells to the wand
		boolean addedSpells = false;
		Set<String> wandSpells = wand.getSpells();
		LinkedList<WeightedPair<String>> remainingSpells = new LinkedList<WeightedPair<String>>();
		for (WeightedPair<String> spell : spellProbability) {
			if (!wandSpells.contains(spell.getValue())) {
				remainingSpells.add(spell);
			}
		}
		
		SpellTemplate firstSpell = null;		
		if (remainingSpells.size() > 0) {
			Integer spellCount = RandomUtils.weightedRandom(spellCountProbability);
			int retries = 10;
			for (int i = 0; i < spellCount; i++) {
				String spellKey = RandomUtils.weightedRandom(remainingSpells);
				
				if (wand.addSpell(spellKey)) {	
					if (firstSpell == null) {
						firstSpell = wand.getMaster().getSpellTemplate(spellKey);
					}
					addedSpells = true;
				} else {
					// Try again up to a certain number if we picked one the wand already had.
					if (retries-- > 0) i--;
				}
			}
		}
		
		// Look through all spells for the max XP casting cost
		// Also look for any material-using spells
		boolean needsMaterials = false;
		int maxXpCost = 0;
		Set<String> spells = wand.getSpells();
		for (String spellName : spells) {
			SpellTemplate spell = wand.getMaster().getSpellTemplate(spellName);
			if (spell != null) {
				needsMaterials = needsMaterials || (spell instanceof BrushSpell) && !((BrushSpell)spell).hasBrushOverride();
				Collection<CastingCost> costs = spell.getCosts();
				if (costs != null) {
					for (CastingCost cost : costs) {
						maxXpCost = Math.max(maxXpCost, cost.getXP());
					}
				}
			}
		}
		
		// Add random materials
		boolean addedMaterials = false;
		Set<String> wandMaterials = wand.getBrushes();
		LinkedList<WeightedPair<String>> remainingMaterials = new LinkedList<WeightedPair<String>>();
		for (WeightedPair<String> material : materialProbability) {
			String materialKey = material.getValue();
			// Fixup @'s to :'s .... kinda hacky, but I didn't think this through unfortunately. :\
			materialKey = materialKey.replace("|", ":");
			if (!wandMaterials.contains(material.getValue()) && MaterialBrush.isValidMaterial(materialKey, false)) {
				remainingMaterials.add(material);
			}
		}
		if (needsMaterials && remainingMaterials.size() > 0) {
			int currentMaterialCount = wand.getBrushes().size();
			Integer materialCount = RandomUtils.weightedRandom(materialCountProbability);
			
			// Make sure the wand has at least one material.
			if (currentMaterialCount == 0) {
				materialCount = Math.max(1, materialCount);
			}
			int retries = 100;
			for (int i = 0; i < materialCount; i++) {
				String materialKey = RandomUtils.weightedRandom(remainingMaterials);
				materialKey = materialKey.replace("|", ":");
				if (!wand.addBrush(materialKey)) {
					// Try again up to a certain number if we picked one the wand already had.
					if (retries-- > 0) i--;
				} else {
					addedMaterials = true;
				}
			}
		}
		
		// Add random wand properties
		boolean addedProperties = false;
		Integer propertyCount = RandomUtils.weightedRandom(propertyCountProbability);
		ConfigurationSection wandProperties = new MemoryConfiguration();
		double costReduction = wand.getCostReduction();
		
		while (propertyCount-- > 0) {
			int randomProperty = (int)(Math.random() * 10);
			switch (randomProperty) {
			case 0: 
				if (costReduction < maxValue) {
					addedProperties = true;
					costReduction = Math.min(maxValue, costReduction + RandomUtils.weightedRandom(costReductionProbability));
					wandProperties.set("cost_reduction", costReduction);
				}
				break;
			case 1:
				float power = wand.getPower();
				if (power < maxValue) {
					addedProperties = true;
					wandProperties.set("power", (Double)(double)(Math.min(maxValue, power + RandomUtils.weightedRandom(powerProbability))));
				}
				break;
			case 2:
				float damageReduction = wand.getDamageReduction();
				if (damageReduction < maxValue) {
					addedProperties = true;
					wandProperties.set("protection", (Double)(double)(Math.min(maxValue, damageReduction + RandomUtils.weightedRandom(damageReductionProbability))));
				}
				break;
			case 3:
				float damageReductionPhysical = wand.getDamageReductionPhysical();
				if (damageReductionPhysical < maxValue) {
					addedProperties = true;
					wandProperties.set("protection_physical", (Double)(double)(Math.min(maxValue, damageReductionPhysical + RandomUtils.weightedRandom(damageReductionPhysicalProbability))));
				}
				break;
			case 4:
				float damageReductionProjectiles = wand.getDamageReductionProjectiles();
				if (damageReductionProjectiles < maxValue) {
					addedProperties = true;
					wandProperties.set("protection_projectiles", (Double)(double)(Math.min(maxValue, damageReductionProjectiles + RandomUtils.weightedRandom(damageReductionProjectilesProbability))));
				}
				break;
			case 5:
				float damageReductionFalling = wand.getDamageReductionFalling();
				if (damageReductionFalling < maxValue) {
					addedProperties = true;
					wandProperties.set("protection_falling", (Double)(double)(Math.min(maxValue, damageReductionFalling + RandomUtils.weightedRandom(damageReductionFallingProbability))));
				}
				break;
			case 6:
				float damageReductionFire = wand.getDamageReductionFire();
				if (damageReductionFire < maxValue) {
					addedProperties = true;
					wandProperties.set("protection_fire", (Double)(double)(Math.min(maxValue, damageReductionFire + RandomUtils.weightedRandom(damageReductionFireProbability))));
				}
				break;
			case 7:
				float damageReductionExplosions = wand.getDamageReductionExplosions();
				if (damageReductionExplosions < maxValue) {
					addedProperties = true;
					wandProperties.set("protection_explosions", (Double)(double)(Math.min(maxValue, damageReductionExplosions + RandomUtils.weightedRandom(damageReductionExplosionsProbability))));
				}
				break;
			case 10:
				float healthRegeneration = wand.getHealthRegeneration();
				if (healthRegeneration < maxValue) {
					addedProperties = true;
					wandProperties.set("health_regeneration", (Integer)(int)(Math.min(maxValue, healthRegeneration + RandomUtils.weightedRandom(healthRegenerationProbability))));
				}
				break;
			case 11:
				float hungerRegeneration = wand.getHungerRegeneration();
				if (hungerRegeneration < maxValue) {
					addedProperties = true;
					wandProperties.set("hunger_regeneration", (Integer)(int)(Math.min(maxValue, hungerRegeneration + RandomUtils.weightedRandom(hungerRegenerationProbability))));
				}
				break;
			}
		}
		
		// The mana system is considered separate from other properties

		if (costReduction > 1) {
			// Cost-Free wands don't need mana.
			wandProperties.set("xp_regeneration", 0);
			wandProperties.set("xp_max", 0);
			wandProperties.set("xp", 0);
		} else {
			int xpRegeneration = wand.getXpRegeneration();
			if (xpRegeneration < maxXpRegeneration) {
				addedProperties = true;
				wandProperties.set("xp_regeneration", (Integer)(int)(Math.min(maxXpRegeneration, xpRegeneration + RandomUtils.weightedRandom(xpRegenerationProbability))));
			}
			int xpMax = wand.getXpMax();
			if (xpMax < maxMaxXp) {
				// Make sure the wand has at least enough xp to cast the highest costing spell it has.
				xpMax = (Integer)(int)(Math.min(maxMaxXp, xpMax + RandomUtils.weightedRandom(xpMaxProbability)));
				xpMax = Math.max(maxXpCost, xpMax);
				wandProperties.set("xp_max", xpMax);
				addedProperties = true;
			}
			
			// Refill the wand's xp, why not
			wandProperties.set("xp", xpMax);
		}
		
		// Add or set uses to the wand
		if (additive) {
			// Only add uses to a wand if it already has some.
			int wandUses = wand.getUses();
			if (wandUses > 0 && wandUses < maxUses) {
				wandProperties.set("uses", Math.min(maxUses, wandUses + RandomUtils.weightedRandom(addUseProbability)));
				addedProperties = true;
			}
		} else {
			wandProperties.set("uses", Math.min(maxUses, RandomUtils.weightedRandom(useProbability)));
			
			// If we are creating a new wand, make a templatized name
			// based on the first spell that was added to it.
			String spellName = "Nothing";
			if (firstSpell != null) {
				spellName = firstSpell.getName();
			} 
			String updatedName = wand.getName();
			wand.setName(updatedName.replace("{Spell}", spellName));
		}

		// Set properties. This also updates name and lore.
		wand.loadProperties(wandProperties);
		
		return addedMaterials || addedSpells || addedProperties;
	}
	
	public static boolean randomizeWand(Wand wand, boolean additive, int level) {
		WandLevel wandLevel = getLevel(level);
		return wandLevel.randomizeWand(wand, additive);
	}
	
	public static Set<Integer> getLevels() {
		if (levels == null) return null;
		Set<Integer> filteredLevels = new HashSet<Integer>();
		for (Integer level : levels) {
			if (level >= minLevel && level <= maxLevel) {
				filteredLevels.add(level);
			}
		}
		return filteredLevels;
	}
	
	public static int getMaxLevel() {
		if (levels == null) return 0;
		
		return Math.min(levels[levels.length - 1], maxLevel);
	}
}
