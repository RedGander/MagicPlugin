package com.elmakers.mine.bukkit.plugins.magic.spells;

import java.lang.reflect.Field;
import java.util.Random;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class FireworkSpell extends Spell
{ 
	private Type fireworkType;
	private Color color1;
	private Color color2;
	private int power;
	private boolean flicker;
	private boolean trail;
	
	static private Type[] types = { Type.BALL, Type.BALL_LARGE, Type.BURST, Type.CREEPER, Type.STAR };
	static private Color[] colors = {Color.AQUA, Color.BLACK, Color.BLUE, Color.FUCHSIA, Color.GRAY,
			Color.GREEN, Color.LIME, Color.MAROON, Color.NAVY, Color.OLIVE, Color.ORANGE, Color.PURPLE, 
			Color.RED, Color.SILVER, Color.TEAL, Color. WHITE, Color.YELLOW };
	
	@Override
	public boolean onCast(ConfigurationNode parameters) 
	{
		Random rand = new Random();
		color1 = getColor(rand.nextInt(17));
		color2 = getColor(rand.nextInt(17));
		power = rand.nextInt(2) + 1;
		fireworkType = getType(rand.nextInt(5));
		flicker = rand.nextBoolean();
		trail = rand.nextBoolean();
		
		// Configuration overrides
		power = parameters.getInt("size", power);
		if (parameters.containsKey("color1")) {
			color1 = getColor(parameters.getString("color1"));
		}
		if (parameters.containsKey("color2")) {
			color2 = getColor(parameters.getString("color2"));
		}
		if (parameters.containsKey("type")) {
			fireworkType = getType(parameters.getString("type"));
		}
		flicker = parameters.getBoolean("flicker", flicker);
		trail = parameters.getBoolean("trail", trail);
		
		int flareCount = parameters.getInt("count", 1);
		Block target = getTarget().getBlock();

		for (int i = 0; i < flareCount; i++)
		{
			spawnFirework(target.getLocation(), flareCount);
		}

		castMessage(player, "You fire some magical flares");

		return true;
	}
	
	protected void spawnFirework(Location location, int spread) {
		Firework firework = (Firework)player.getWorld().spawnEntity(location, EntityType.FIREWORK);
        FireworkMeta meta = firework.getFireworkMeta();
     
        FireworkEffect effect = FireworkEffect.builder().flicker(flicker).withColor(color1).withFade(color2).with(fireworkType).trail(trail).build();
        meta.addEffect(effect);
        meta.setPower(power);
        firework.setFireworkMeta(meta);     
	}
	
	protected Type getType(int i) {
		if (i < types.length) {
			return types[i];
		}

		return types[0];
	}

	protected Color getColor(int i) {
		if (i < colors.length) {
			return colors[i];
		}

		return colors[0];
	}
	
	protected Color getColor(String name) {
		try {
			Field colorConstant = Color.class.getField(name.toUpperCase());
			return (Color)colorConstant.get(null);
		} catch (Exception ex) {
		}
		
		return Color.WHITE;
	}
	
	protected Type getType(String name) {
		for (Type t : Type.values()) {
			if (t.name().equalsIgnoreCase(name)) {
				return t;
			}
		}
		
		return Type.BALL;
	}

	@Override
	public void onLoad(ConfigurationNode parameters)
	{
	}
}