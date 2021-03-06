package com.elmakers.mine.bukkit.block.batch;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.block.BlockData;
import com.elmakers.mine.bukkit.block.UndoList;

public class BlockRecurse
{
	protected int maxRecursion = 8;

	public int recurse(Block startBlock, BlockAction recurseAction)
	{
		recurse(startBlock, recurseAction, null, 0);
		return recurseAction.getBlocks().size();
	}

	protected void recurse(Block block, BlockAction recurseAction, BlockFace nextFace, int rDepth)
	{
		UndoList affectedBlocks = recurseAction.getBlocks();
		if (nextFace != null)
		{
			block = block.getRelative(nextFace);
		}
		if (affectedBlocks.contains(block))
		{
			return;
		}
		affectedBlocks.add(block);

		if (recurseAction.perform(block) != SpellResult.CAST)
		{
			return;
		}

		if (rDepth < maxRecursion)
		{
			for (BlockFace face : BlockData.FACES)
			{
				if (nextFace == null || nextFace != BlockData.getReverseFace(face))
				{
					recurse(block, recurseAction, face, rDepth + 1);
				}
			}
		}
	}

	public int getMaxRecursion() {
		return maxRecursion;
	}

	public void setMaxRecursion(int maxRecursion) {
		this.maxRecursion = maxRecursion;
	}
}
