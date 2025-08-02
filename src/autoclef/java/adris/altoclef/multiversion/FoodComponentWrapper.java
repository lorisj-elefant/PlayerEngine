package adris.altoclef.multiversion;

import net.minecraft.item.FoodComponent;

public class FoodComponentWrapper {
    private final FoodComponent component;

    public static adris.altoclef.multiversion.FoodComponentWrapper of(FoodComponent component) {
        if (component == null)
            return null;
        return new adris.altoclef.multiversion.FoodComponentWrapper(component);
    }

    private FoodComponentWrapper(FoodComponent component) {
        this.component = component;
    }

    public int getHunger() {
        return this.component.getHunger();
    }

    public float getSaturationModifier() {
        return this.component.getSaturationModifier();
    }
}
