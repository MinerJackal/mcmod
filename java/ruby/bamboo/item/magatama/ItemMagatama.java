package ruby.bamboo.item.magatama;

import java.util.HashMap;
import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.World;
import net.minecraftforge.common.ChestGenHooks;
import ruby.bamboo.BambooCore;
import ruby.bamboo.Config;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemMagatama extends Item {
    private static final HashMap<Integer, IMagatama> magatamaMap;

    public enum UseType {
        THROWING,
        HAND;
    };

    static {
        magatamaMap = new HashMap<Integer, IMagatama>();
    }

    public ItemMagatama() {
        super();
        setMaxStackSize(1);
        setMaxDamage(0);
        setHasSubtypes(true);
        addMagatama(0, new MagatamaRed());
        addMagatama(1, new MagatamaPurple());
        addMagatama(2, new MagatamaSilver());
        addMagatama(3, new MagatamaBlue());
        addMagatama(4, new MagatamaGreen());
        addMagatama(5, new MagatamaPink());
        addMagatama(6, new MagatamaOrange());
    }

    public static IMagatama getMagatama(int id) {
        return magatamaMap.get(id);
    }

    public void addMagatama(int meta, IMagatama magatama) {
        magatamaMap.put(meta, magatama);
        if (Config.addMagatama) {
            WeightedRandomChestContent content = new BambooChestContent(new ItemStack(this, 1, meta), 1, 1, magatama.getReality());
            ChestGenHooks.addItem(ChestGenHooks.PYRAMID_DESERT_CHEST, content);
            ChestGenHooks.addItem(ChestGenHooks.PYRAMID_JUNGLE_CHEST, content);
            ChestGenHooks.addItem(ChestGenHooks.STRONGHOLD_CORRIDOR, content);
            ChestGenHooks.addItem(ChestGenHooks.STRONGHOLD_CROSSING, content);
            ChestGenHooks.addItem(ChestGenHooks.DUNGEON_CHEST, content);
            ChestGenHooks.addItem(ChestGenHooks.MINESHAFT_CORRIDOR, content);
            ChestGenHooks.addItem(ChestGenHooks.VILLAGE_BLACKSMITH, content);
            ChestGenHooks.addItem(ChestGenHooks.BONUS_CHEST, content);
        }
    }

    @Override
    public ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer) {
        if (!magatamaMap.containsKey(par1ItemStack.getItemDamage())) {
            return par1ItemStack;
        }
        if (!Config.useMagatama || magatamaMap.get(par1ItemStack.getItemDamage()).getEffectClass() == null) {
            return par1ItemStack;
        }
        magatamaMap.get(par1ItemStack.getItemDamage()).useItem(par2World, par1ItemStack, par3EntityPlayer);
        if (magatamaMap.get(par1ItemStack.getItemDamage()).isDecrease()) {
            par1ItemStack.stackSize--;
        }
        return par1ItemStack;
    }

    @Override
    public void onUpdate(ItemStack par1ItemStack, World par2World, Entity par3Entity, int par4, boolean par5) {
        if (magatamaMap.containsKey(par1ItemStack.getItemDamage())) {
            if (par4 <= 9) {
                magatamaMap.get(par1ItemStack.getItemDamage()).holdingEffect(par3Entity, par4);
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean hasEffect(ItemStack par1ItemStack, int pass) {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getColorFromItemStack(ItemStack par1ItemStack, int par2) {
        if (magatamaMap.containsKey(par1ItemStack.getItemDamage())) {
            return magatamaMap.get(par1ItemStack.getItemDamage()).getColor();
        } else {
            return 0;
        }
    }

    @Override
    public void getSubItems(Item par1, CreativeTabs par2CreativeTabs, List par3List) {
        for (int i : magatamaMap.keySet()) {
            par3List.add(new ItemStack(par1, 1, i));
        }
    }

    @Override
    public String getUnlocalizedName(ItemStack par1ItemStack) {
        return super.getUnlocalizedName(par1ItemStack) + "." + par1ItemStack.getItemDamage();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister par1IconRegister) {
        this.itemIcon = par1IconRegister.registerIcon(BambooCore.resourceDomain + "magatama");
    }
}
