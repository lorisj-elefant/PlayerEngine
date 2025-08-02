package adris.altoclef.util;

import net.minecraft.item.Item;

import java.util.Objects;

public class SmeltTarget {
    private final ItemTarget item;

    private final Item[] optionalMaterials;

    private ItemTarget material;

    public SmeltTarget(ItemTarget item, ItemTarget material, Item... optionalMaterials) {
        this.item = item;
        this.material = material;
        this.material = new ItemTarget(material, this.item.getTargetCount());
        this.optionalMaterials = optionalMaterials;
    }

    public ItemTarget getItem() {
        return this.item;
    }

    public ItemTarget getMaterial() {
        return this.material;
    }

    public Item[] getOptionalMaterials() {
        return this.optionalMaterials;
    }

    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        adris.altoclef.util.SmeltTarget that = (adris.altoclef.util.SmeltTarget) o;
        return (Objects.equals(this.material, that.material) && Objects.equals(this.item, that.item));
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.material, this.item});
    }
}
