package com.elmakers.mine.bukkit.block.batch;

import org.bukkit.block.Block;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.block.UndoList;

public class SimpleBlockAction implements BlockAction
{
	protected final MageController controller;
	protected final UndoList modified;
	
	public SimpleBlockAction(MageController controller, UndoList undoList)
	{
		modified = undoList;
		this.controller = controller;
	}

	public SpellResult perform(Block block)
	{
		if (controller != null)
		{
			controller.updateBlock(block);
		}
		if (modified != null)
		{
			modified.add(block);
		}
		return SpellResult.CAST;
	}

	public UndoList getBlocks()
	{
		return modified;
	}
}
