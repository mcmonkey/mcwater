package net.cloudapp.mcminecraftwest.bukkit.mcwater;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.util.Vector;

/**
 * Created with IntelliJ IDEA.
 * User: Jason
 * Date: 9/2/12
 * Time: 10:53 PM
 */
public class WaterFlowListener implements Listener {

	public static final int AIR_ID = 0x0;
	public static final int WATER_FLOWING_ID = 0x8;
	public static final int WATER_STATIC_ID = 0x9;

	public static final byte LIQUID_FALLING_FLAG = 0x8;
	/**
	 * Represents the mask to find a liquids level.
	 *
	 * The mask is equivalent to an empty flowing block.
	 * 0x0 is full.
	 * (The inverse is not full, it will have extra bits set)
	 */
	public static final byte LIQUID_LEVEL_MASK = 0x7;

	/** Inverse of level mask. Helpful for clearing and setting */
	public static final byte N_LIQUID_LEVEL_MASK = ~LIQUID_LEVEL_MASK;

	private static Vector[] m_directions = null;

	BlockFromToEvent event;

	Block block;
	byte bMeta;
	boolean bFalling;
	byte bLevel;
	int bId;

	Block to;
	byte toMeta;
	int toId;
	boolean toFalling;
	byte toLevel;

	World world;

	private McWater m_plugin;

	public WaterFlowListener(McWater plugin) {
		m_plugin = plugin;
	}

	@EventHandler
	public void onWaterFromTo(BlockFromToEvent event) {
		this.event = event;
		world = event.getBlock().getWorld();

		block = event.getBlock();
		bMeta = block.getData();
		bFalling = (bMeta & LIQUID_FALLING_FLAG) > 0;
		bLevel = (byte)(bMeta & LIQUID_LEVEL_MASK);
		bId = block.getTypeId();

		setTo(event.getToBlock());

		if(isWater()) {
			if(blockProvidesWater()) {
				if(toAcceptsWater()) {

				} else if(findValidAdjacentBlock()) {

				}
			} else {
				cancel();
			}
		}

	}

	public Vector[] getDirections() {
		if(m_directions == null) {
			m_directions = new Vector[8];
			m_directions[0] = new Vector( 1, 0,  0);
			m_directions[1] = new Vector( 0, 0, -1);
			m_directions[2] = new Vector(-1, 0,  0);
			m_directions[3] = new Vector(-1, 0,  0);
			m_directions[4] = new Vector( 0, 0,  1);
			m_directions[5] = new Vector( 0, 0,  1);
			m_directions[6] = new Vector( 1, 0,  0);
			m_directions[7] = new Vector( 1, 0,  0);
		}
		return m_directions;
	}

	private void setTo(Block toBlock) {
		to = toBlock;
		toMeta = to.getData();
		toId = to.getTypeId();
		toFalling = (toMeta & LIQUID_FALLING_FLAG) > 0;
		toLevel = (byte)(toMeta & LIQUID_LEVEL_MASK);
	}

	private byte flowLevel(Block b) {
		return (byte)(b.getData() & LIQUID_LEVEL_MASK);
	}

	private boolean findValidAdjacentBlock() {
		Block best = null;
		int bestRating = -1;
		Block current = null;
		int currentRating = -1;
		final int maxRating = LIQUID_LEVEL_MASK + 1;

		Location loc = block.getLocation();
		Vector[] directions = getDirections();
		for(int i = 0; i < directions.length && bestRating != maxRating; ++i ) {
			loc.add(directions[i]);
			current = world.getBlockAt(loc);

			int id = current.getTypeId();

			currentRating = -1;
			if(id == AIR_ID) {
				currentRating = maxRating;
			} else if(id == WATER_FLOWING_ID) {
				currentRating = flowLevel(current);
			}

			if(currentRating > bestRating) {
				bestRating = currentRating;
				best = current;
			}
		}

		if(best != null) {
			setTo(best);
			return true;
		}
		return false;
	}

	private boolean toAcceptsWater() {
		if(toId == WATER_STATIC_ID) {
			return false;
		} else if(toId == WATER_FLOWING_ID) {
			return toLevel != LIQUID_FALLING_FLAG;
		}
		// Assume the event would not be firing if we were hitting an otherwise valid block.
		return true;
	}

	private void cancel(boolean physics) {
		event.setCancelled(true);
	}

	private void cancel() {
		cancel(true);
	}
	private boolean isWater() {
		return bId == WATER_FLOWING_ID || bId == WATER_STATIC_ID;
	}

	private boolean blockProvidesLava() {
		return true;
	}

	private boolean blockProvidesWater() {
		if(bId == WATER_STATIC_ID) {
			return true;
		} else if(bId == WATER_FLOWING_ID) {
			// Don't provide water from almost empty blocks.
			return bLevel != LIQUID_LEVEL_MASK;
		}
		return false;
	}
}
