package ruby.bamboo.block;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.BonemealEvent;
import ruby.bamboo.BambooCore;
import ruby.bamboo.CustomRenderHandler;
import ruby.bamboo.render.block.RenderCoordinateBlock.ICoordinateRenderType;
import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockBambooShoot extends Block implements ICoordinateRenderType {
    protected static final ArrayList<Block> bambooList = new ArrayList<Block>();

    public BlockBambooShoot() {
        super(MaterialBamboo.instance);
        setTickRandomly(true);
        setBlockBounds(0.3F, 0.0F, 0.3F, 0.7F, 0.5F, 0.7F);
        setHardness(0F);
        setResistance(0F);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onBonemealEvent(BonemealEvent event) {
        if (event.block == this) {
            if (event.entityPlayer.capabilities.isCreativeMode) {
                event.setCanceled(true);
            }
            if (!event.world.isRemote) {
                event.world.playAuxSFX(2005, event.x, event.y, event.z, 0);
            }
            tryBambooGrowth(event.world, event.x, event.y, event.z, 0.75F);
            event.setResult(Result.ALLOW);
        }
    }

    @Override
    public void updateTick(World world, int i, int j, int k, Random random) {
        tryBambooGrowth(world, i, j, k, world.isRaining() ? 0.25F : 0.125F);
    }

    private void tryBambooGrowth(World world, int x, int y, int z, float probability) {
        if (!world.isRemote) {
            if (canChildGrow(world, x, y, z)) {
                world.setBlock(x, y, z, bambooList.get(world.rand.nextInt(bambooList.size())), 0, 3);
            }
        }
    }

    public static boolean canChildGrow(World world, int i, int j, int k) {
        Chunk chunk = world.getChunkFromChunkCoords(i >> 4, k >> 4);
        i &= 0xf;
        k &= 0xf;
        return chunk.getBlockLightValue(i, j, k, 15) > 7;
    }

    @Override
    public void harvestBlock(World world, EntityPlayer entityplayer, int i, int j, int k, int l) {
        if (!world.isRemote) {
            if (entityplayer.getCurrentEquippedItem() != null && (entityplayer.getCurrentEquippedItem().getItem() instanceof ItemHoe)) {
                //上位素材ほど軽減率は高い
                int itemDmg = 64 - getHarvestLevel((ItemHoe) entityplayer.getHeldItem().getItem()) * 10;
                //ダイヤのクワでEfficiency Vの時0以下になる感じに
                itemDmg -= EnchantmentHelper.getEfficiencyModifier(entityplayer) * 7;
                if (itemDmg < 1) {
                    itemDmg = 1;
                }
                entityplayer.addStat(StatList.mineBlockStatArray[getIdFromBlock(this)], 1);
                entityplayer.getCurrentEquippedItem().damageItem(itemDmg, entityplayer);
                if (entityplayer.getCurrentEquippedItem().stackSize == 0) {
                    entityplayer.destroyCurrentEquippedItem();
                }
                //ふぉーちゅんLV*10%で経験値ドロップ
                if (entityplayer.worldObj.rand.nextFloat() < EnchantmentHelper.getFortuneModifier(entityplayer) * 0.1F) {
                    this.dropXpOnBlockBreak(world, i, j, k, 1);
                }
                this.dropBlockAsItem(world, i, j, k, new ItemStack(this, 1, 0));
            }
        }
    }

    private int getHarvestLevel(ItemHoe item) {
        return ToolMaterial.valueOf(item.getToolMaterialName()).getHarvestLevel();
    }

    @Override
    public boolean canBlockStay(World world, int i, int j, int k) {
        Block block = world.getBlock(i, j - 1, k);
        if (block != Blocks.grass && block != Blocks.dirt) {
            return false;
        }
        return true;
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
        if (!canBlockStay(world, x, y, z)) {
            world.setBlockToAir(x, y, z);
        }
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int i, int j, int k) {
        return null;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public int getRenderType() {
        return CustomRenderHandler.coordinateCrossUID;
    }

    @Override
    public int getMobilityFlag() {
        return 1;
    }

    @Override
    public int getCoordinateRenderType() {
        return 0;
    }

    @Override
    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z) {
        return new ItemStack(this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public String getItemIconName() {
        return BambooCore.resourceDomain + "bambooshoot";
    }
}
