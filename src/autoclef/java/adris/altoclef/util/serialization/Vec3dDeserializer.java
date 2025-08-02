//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package adris.altoclef.util.serialization;

import com.fasterxml.jackson.core.JsonToken;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class Vec3dDeserializer extends AbstractVectorDeserializer<Vec3d, Double> {
    public Vec3dDeserializer() {
    }

    protected String getTypeName() {
        return "Vec3d";
    }

    protected String[] getComponents() {
        return new String[]{"x", "y"};
    }

    protected Double parseUnit(String unit) throws Exception {
        return Double.parseDouble(unit);
    }

    protected Vec3d deserializeFromUnits(List<Double> units) {
        return new Vec3d((Double) units.get(0), (Double) units.get(1), (Double) units.get(2));
    }

    protected boolean isUnitTokenValid(JsonToken token) {
        return token == JsonToken.VALUE_NUMBER_INT || token == JsonToken.VALUE_NUMBER_FLOAT;
    }
}
