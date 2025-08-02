package adris.altoclef.multiversion;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.registry.tag.DamageTypeTags;

public class DamageSourceWrapper {
    private final DamageSource source;

    public static adris.altoclef.multiversion.DamageSourceWrapper of(DamageSource source) {
        if (source == null)
            return null;
        return new adris.altoclef.multiversion.DamageSourceWrapper(source);
    }

    private DamageSourceWrapper(DamageSource source) {
        this.source = source;
    }

    public DamageSource getSource() {
        return this.source;
    }

    public boolean bypassesArmor() {
        return this.source.isTypeIn(DamageTypeTags.BYPASSES_ARMOR);
    }

    public boolean bypassesShield() {
        return this.source.isTypeIn(DamageTypeTags.BYPASSES_SHIELD);
    }

    public boolean isOutOfWorld() {
        return this.source.isType(DamageTypes.OUT_OF_WORLD);
    }
}
