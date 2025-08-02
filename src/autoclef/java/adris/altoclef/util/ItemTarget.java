package adris.altoclef.util;

import adris.altoclef.TaskCatalogue;
import adris.altoclef.util.helpers.ItemHelper;
import net.minecraft.item.Item;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ItemTarget {
    private static final int BASICALLY_INFINITY = 99999999;

    public static adris.altoclef.util.ItemTarget[] of(Item... items) {
        return (adris.altoclef.util.ItemTarget[]) Arrays.<Item>stream(items).map(adris.altoclef.util.ItemTarget::new).toArray(x$0 -> new adris.altoclef.util.ItemTarget[x$0]);
    }

    public static adris.altoclef.util.ItemTarget EMPTY = new adris.altoclef.util.ItemTarget(new Item[0], 0);

    private Item[] itemMatches;

    private final int targetCount;

    private String catalogueName = null;

    private boolean infinite = false;

    public ItemTarget(Item[] items, int targetCount) {
        this.itemMatches = items;
        this.targetCount = targetCount;
        this.infinite = false;
    }

    public ItemTarget(String catalogueName, int targetCount) {
        this.catalogueName = catalogueName;
        this.itemMatches = TaskCatalogue.getItemMatches(catalogueName);
        this.targetCount = targetCount;
    }

    public ItemTarget(String catalogueName) {
        this(catalogueName, 1);
    }

    public ItemTarget(Item item, int targetCount) {
        this(new Item[]{item}, targetCount);
    }

    public ItemTarget(Item... items) {
        this(items, 1);
    }

    public ItemTarget(Item item) {
        this(item, 1);
    }

    public ItemTarget(adris.altoclef.util.ItemTarget toCopy, int newCount) {
        if (toCopy.itemMatches != null) {
            this.itemMatches = new Item[toCopy.itemMatches.length];
            System.arraycopy(toCopy.itemMatches, 0, this.itemMatches, 0, toCopy.itemMatches.length);
        }
        this.catalogueName = toCopy.catalogueName;
        this.targetCount = newCount;
        this.infinite = toCopy.infinite;
    }

    public static boolean nullOrEmpty(adris.altoclef.util.ItemTarget target) {
        return (target == null || target == EMPTY);
    }

    public static Item[] getMatches(adris.altoclef.util.ItemTarget... targets) {
        Set<Item> result = new HashSet<>();
        for (adris.altoclef.util.ItemTarget target : targets)
            result.addAll(Arrays.asList(target.getMatches()));
        return (Item[]) result.toArray(x$0 -> new Item[x$0]);
    }

    public adris.altoclef.util.ItemTarget infinite() {
        this.infinite = true;
        return this;
    }

    public Item[] getMatches() {
        return (this.itemMatches != null) ? this.itemMatches : new Item[0];
    }

    public int getTargetCount() {
        if (this.infinite)
            return 99999999;
        return this.targetCount;
    }

    public boolean matches(Item item) {
        if (this.itemMatches != null)
            for (Item match : this.itemMatches) {
                if (match != null &&
                        match.equals(item))
                    return true;
            }
        return false;
    }

    public boolean isCatalogueItem() {
        return (this.catalogueName != null);
    }

    public String getCatalogueName() {
        return this.catalogueName;
    }

    public boolean equals(Object obj) {
        if (obj instanceof adris.altoclef.util.ItemTarget) {
            adris.altoclef.util.ItemTarget other = (adris.altoclef.util.ItemTarget) obj;
            if (this.infinite) {
                if (!other.infinite)
                    return false;
            } else if (this.targetCount != other.targetCount) {
                return false;
            }
            if (((other.itemMatches == null) ? true : false) != ((this.itemMatches == null) ? true : false))
                return false;
            if (this.itemMatches != null) {
                if (this.itemMatches.length != other.itemMatches.length)
                    return false;
                for (int i = 0; i < this.itemMatches.length; i++) {
                    if (other.itemMatches[i] == null) {
                        if (((other.itemMatches[i] == null) ? true : false) != ((this.itemMatches[i] == null) ? true : false))
                            return false;
                    } else if (!other.itemMatches[i].equals(this.itemMatches[i])) {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    public boolean isEmpty() {
        return (this.itemMatches == null || this.itemMatches.length == 0);
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        if (isEmpty()) {
            result.append("(empty)");
        } else if (isCatalogueItem()) {
            result.append(this.catalogueName);
        } else {
            result.append("[");
            int counter = 0;
            if (this.itemMatches != null)
                for (Item item : this.itemMatches) {
                    if (item == null) {
                        result.append("(null??)");
                    } else {
                        result.append(ItemHelper.trimItemName(item.getTranslationKey()));
                    }
                    if (++counter != this.itemMatches.length)
                        result.append(",");
                }
            result.append("]");
        }
        if (!this.infinite && !isEmpty() && this.targetCount > 1) {
            result.append(" x ").append(this.targetCount);
        } else if (this.infinite) {
            result.append(" x infinity");
        }
        return result.toString();
    }
}
