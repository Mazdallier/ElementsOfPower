package gigaherz.elementsofpower;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.server.gui.IUpdatePlayerListBox;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.common.util.Constants;

public class TileEssentializer
        extends TileEntity
        implements ISidedInventory, IUpdatePlayerListBox {
    public static final int MaxEssentializerMagic = 1000;

    private ItemStack[] inventory;

    boolean stateChanged = false;

    public TileEssentializer() {
        super();
        // 0..7: magics
        // 8: input
        // 9: output
        this.inventory = new ItemStack[10];
    }

    @Override
    public String getName() {
        return "essentializer";
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public IChatComponent getDisplayName() {
        return null;
    }

    @Override
    public int getSizeInventory() {
        return this.inventory.length;
    }

    @Override
    public ItemStack getStackInSlot(int slotIndex) {
        if (slotIndex >= this.inventory.length) {
            System.out.println("Tried to access slot " + slotIndex);
            return null;
        }

        return this.inventory[slotIndex];
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        this.inventory[slot] = stack;

        if (stack != null && stack.stackSize > getInventoryStackLimit()) {
            stack.stackSize = getInventoryStackLimit();
        }
    }

    @Override
    public ItemStack decrStackSize(int slotIndex, int amount) {
        ItemStack stack = getStackInSlot(slotIndex);

        if (stack != null) {
            if (stack.stackSize <= amount) {
                setInventorySlotContents(slotIndex, null);
            } else {
                stack = stack.splitStack(amount);

                if (stack.stackSize == 0) {
                    setInventorySlotContents(slotIndex, null);
                }
            }
        }

        return stack;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slotIndex) {
        ItemStack stack = getStackInSlot(slotIndex);

        if (slotIndex < 8)
            return null;

        if (stack != null) {
            setInventorySlotContents(slotIndex, null);
        }

        return stack;
    }

    @Override
    public int getInventoryStackLimit() {
        return MaxEssentializerMagic;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        boolean a = this.worldObj.getTileEntity(this.pos) == this;
        boolean b = player.getDistanceSq(
                getPos().getX() + 0.5,
                getPos().getY() + 0.5,
                getPos().getZ() + 0.5) < 64;
        return a && b;
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);

        NBTTagList tagList = tagCompound.getTagList("Inventory", Constants.NBT.TAG_COMPOUND);

        for (int i = 0; i < tagList.tagCount(); i++) {
            NBTTagCompound tag = (NBTTagCompound) tagList.get(i);
            byte slot = tag.getByte("Slot");

            if (slot >= 8 && slot < inventory.length) {
                inventory[slot] = ItemStack.loadItemStackFromNBT(tag);
            }
        }

        tagList = tagCompound.getTagList("Essences", Constants.NBT.TAG_COMPOUND);

        for (int i = 0; i < tagList.tagCount(); i++) {
            NBTTagCompound tag = (NBTTagCompound) tagList.get(i);
            byte slot = tag.getByte("Type");

            if (slot >= 0 && slot < 8) {
                inventory[slot] = new ItemStack(ElementsOfPower.magicOrb, tag.getInteger("Count"), slot);
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);

        NBTTagList itemList = new NBTTagList();

        for (int i = 8; i < inventory.length; i++) {
            ItemStack stack = inventory[i];

            if (stack != null) {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setByte("Slot", (byte) i);
                stack.writeToNBT(tag);
                itemList.appendTag(tag);
            }
        }

        tagCompound.setTag("Inventory", itemList);

        itemList = new NBTTagList();

        for (int i = 0; i < 8; i++) {
            ItemStack stack = inventory[i];

            if (stack != null) {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setByte("Type", (byte) i);
                tag.setInteger("Count", stack.stackSize);
                itemList.appendTag(tag);
            }
        }

        tagCompound.setTag("Essences", itemList);
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {
    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {
    }

    @Override
    public void openInventory(EntityPlayer player) {
        // TODO
    }

    @Override
    public void closeInventory(EntityPlayer player) {
        // TODO
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound tag = new NBTTagCompound();
        this.writeToNBT(tag);
        return new S35PacketUpdateTileEntity(this.pos, 0, tag);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet) {
        super.onDataPacket(net, packet);
        readFromNBT(packet.getNbtCompound());
        this.worldObj.markBlockForUpdate(this.pos);
    }

    @Override
    public int[] getSlotsForFace(EnumFacing side) {
        if (EnumFacing.UP == side || EnumFacing.DOWN == side) {
            return new int[]{9};
        }

        return new int[]{8};
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return index == 8;
    }

    @Override
    public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
        return index == 8;
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
        return index == 9;
    }

    public void updateProgressBar(int bar, int value) {
        if (bar == 0) {
            convertInput();
        } else if (bar == 1) {
            addMagicToOutput(value);
        }
    }

    @Override
    public void update() {
        convertInput();
        addMagicToOutput(1);
    }

    public int getInputEssencesOfType(int type) {
        ItemStack inputItem = inventory[8];

        if (inputItem == null) {
            return 0;
        }

        MagicAmounts amounts = MagicDatabase.getContainedMagic(inputItem);

        if (amounts == null) {
            amounts = MagicDatabase.getEssences(inputItem);

            if (amounts == null) {
                return 0;
            }
        }

        return amounts.getAmountOfType(type);
    }

    public void convertInput() {
        ItemStack input = inventory[8];

        if (input == null) {
            return;
        }

        if (MagicDatabase.itemContainsMagic(input)) {
            MagicAmounts amounts = MagicDatabase.getContainedMagic(input);

            if (amounts == null) {
                return;
            }

            if (!tryAddAmountsToTile(amounts)) {
                return;
            }

            inventory[8] = MagicDatabase.setContainedMagic(input, null);
        } else {
            MagicAmounts amounts = MagicDatabase.getEssences(input);

            if (amounts == null) {
                return;
            }

            if (!tryAddAmountsToTile(amounts)) {
                return;
            }

            input.stackSize--;

            if (input.stackSize <= 0) {
                inventory[8] = null;
            }
        }

        this.stateChanged = true;
    }

    private boolean tryAddAmountsToTile(MagicAmounts amounts) {
        int[] am = amounts.amounts;

        // test if we can truly add the magic
        for (int i = 0; i < 8; i++) {
            int amount = am[i];

            if (amount > 0) {
                continue;
            }

            ItemStack magic = inventory[i];

            if (magic == null) {
                continue;
            }

            if (magic.stackSize + amount > MaxEssentializerMagic) {
                return false;
            }
        }

        // we can, add it
        for (int i = 0; i < 8; i++) {
            int amount = am[i];

            if (amount == 0) {
                continue;
            }

            addMagicOfType(i, amount);
        }

        return true;
    }

    private void addMagicOfType(int type, int amount) {
        ItemStack magic = inventory[type];

        if (magic == null) {
            inventory[type] = new ItemStack(ElementsOfPower.magicOrb, amount, type);
        } else {
            magic.stackSize += amount;
        }
    }

    public void addMagicToOutput(int slot) {
        ItemStack output = inventory[9];

        if (output == null) {
            return;
        }

        if (output.stackSize != 1) {
            return;
        }

        MagicAmounts limits = MagicDatabase.getMagicLimits(output);
        MagicAmounts amounts = MagicDatabase.getContainedMagic(output);

        if (limits == null)
            return;

        if (amounts == null) {
            amounts = new MagicAmounts();
        }

        for (int i = 0; i < 8; i++) {
            ItemStack magic = inventory[i];

            if (magic == null)
                continue;

            int transfer = Math.min(limits.amounts[i] - amounts.amounts[i], magic.stackSize);

            if (transfer == 0) {
                continue;
            }

            if (transfer > 1)
                transfer = 1;

            magic.stackSize -= transfer;

            if (magic.stackSize <= 0) {
                inventory[i] = null;
            }

            amounts.amounts[i] += transfer;
        }

        inventory[9] = MagicDatabase.setContainedMagic(output, amounts);
    }

}
