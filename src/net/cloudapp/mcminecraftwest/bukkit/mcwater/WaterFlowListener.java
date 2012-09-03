package net.cloudapp.mcminecraftwest.bukkit.mcwater;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
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
				if(toAcceptsWater() || findValidAdjacentBlock()) {
					subtractWaterFrom();
					addWaterTo();
				} else {
					m_plugin.getLogger().info("Water provider can't add to id: " + toId);
				}
			}
			cancel();
		}

	}

	private void addWaterTo() {
		if(toId == AIR_ID) {
			toLevel = LIQUID_FALLING_FLAG;
		}

		toLevel--;
		if(toLevel == 0x0) {
			to.setTypeId(WATER_STATIC_ID);
		} else {
			to.setTypeId(WATER_FLOWING_ID);
			setFlowLevel(to, toMeta, toLevel);
		}
	}

	private void subtractWaterFrom() {
		int cost = waterCost();

		if(cost != 0) {
			bLevel += cost;
			if(bLevel == LIQUID_FALLING_FLAG) {
				// We're empty!
				block.setTypeId(AIR_ID);
			} else {
				block.setTypeId(WATER_FLOWING_ID);
				setFlowLevel(block, bMeta, bLevel);
			}
		}
	}

	private byte waterCost() {
		if(block.getBiome() == Biome.OCEAN)
			return 0;
		return 1;
	}

	public Vector[] getDirections() {
		if(m_directions == null) {
			m_directions = new Vector[4];
			m_directions[0] = new Vector( 1, 0,  0);
			m_directions[1] = new Vector(-1, 0, -1);
			m_directions[2] = new Vector(-1, 0,  1);
			m_directions[3] = new Vector( 1, 0,  1);
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

	private byte getFlowLevel(Block b) {
		return (byte)(b.getData() & LIQUID_LEVEL_MASK);
	}

	private void setFlowLevel(Block b, byte data, byte level) {
		b.setData( (byte)((data & N_LIQUID_LEVEL_MASK) | level));
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
				currentRating = getFlowLevel(current);
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
		} else if(toId == AIR_ID) {
			return true;
		}
		// For now we won't deal with water hitting anything but air.
		return false;
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
			// Don't provide water from almost empty blocks unless they are falling.
			if(bFalling)
				return true;
			return bLevel != LIQUID_LEVEL_MASK;
		}
		return false;
	}
}
