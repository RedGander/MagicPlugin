package com.elmakers.mine.bukkit.block.batch;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.block.MaterialBrush;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.spell.BrushSpell;

public class ReplaceMaterialAction extends SimpleBlockAction
{
	protected Mage mage;
	protected MaterialBrush brush;
	protected Set<MaterialAndData> replaceable = new HashSet<MaterialAndData>();
	
	private boolean spawnFallingBlocks = false;
	private Vector fallingBlockVelocity = null;
	
	public ReplaceMaterialAction(BrushSpell spell, Block targetBlock)
	{
		super(spell.getMage().getController(), spell.getUndoList());
		this.mage = spell.getMage();
		this.brush = spell.getBrush();
		if (targetBlock != null) {
			replaceable.add(new MaterialAndData(targetBlock));
		}
	}

	public ReplaceMaterialAction(BrushSpell spell)
	{
		this(spell, null);
	}

	public void addReplaceable(Material material)
	{
		replaceable.add(new MaterialAndData(material));
	}

	public void addReplaceable(Material material, byte data)
	{
		replaceable.add(new MaterialAndData(material, data));
	}

	@SuppressWarnings("deprecation")
	public SpellResult perform(Block block)
	{
		if (brush == null)
		{
			return SpellResult.FAIL;
		}
		
		if (!mage.hasBuildPermission(block))
		{
			return SpellResult.INSUFFICIENT_PERMISSION;
		}
		
		if (mage.isIndestructible(block))
		{
			return SpellResult.FAIL;
		}

		if (replaceable == null || replaceable.contains(new MaterialAndData(block)))
		{
			Material previousMaterial = block.getType();
			byte previousData = block.getData();
			
			if (brush.isDifferent(block)) {
				brush.update(mage, block.getLocation());
				brush.modify(block);
				mage.getController().updateBlock(block);
				
				if (spawnFallingBlocks) {
					FallingBlock falling = block.getWorld().spawnFallingBlock(block.getLocation(), previousMaterial, previousData);
					falling.setDropItem(false);
					if (fallingBlockVelocity != null) {
						falling.setVelocity(fallingBlockVelocity);
					}
				}
			}
			super.perform(block);
			return SpellResult.CAST;
		}

		return SpellResult.FAIL;
	}
}
