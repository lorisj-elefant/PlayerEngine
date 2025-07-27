package adris.altoclef.util;

import adris.altoclef.AltoClefController;
import adris.altoclef.util.Dimension;
import adris.altoclef.util.helpers.WorldHelper;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Objects;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

public class BlockRange {
  public BlockPos start;
  
  public BlockPos end;
  
  public Dimension dimension = Dimension.OVERWORLD;
  
  private BlockRange() {}
  
  public BlockRange(BlockPos start, BlockPos end, Dimension dimension) {
    this.start = start;
    this.end = end;
    this.dimension = dimension;
  }
  
  public boolean contains(AltoClefController controller, BlockPos pos) {
    return contains(pos, WorldHelper.getCurrentDimension(controller));
  }
  
  public boolean isValid() {
    return (this.start != null && this.end != null);
  }
  
  public boolean contains(BlockPos pos, Dimension dimension) {
    if (this.dimension != dimension)
      return false; 
    return (this.start.getX() <= pos.getX() && pos.getX() <= this.end.getX() && this.start
      .getZ() <= pos.getZ() && pos.getZ() <= this.end.getZ() && this.start
      .getY() <= pos.getY() && pos.getY() <= this.end.getY());
  }
  
  @JsonIgnore
  public BlockPos getCenter() {
    BlockPos sum = this.start.add((Vec3i)this.end);
    return new BlockPos(sum.getX() / 2, sum.getY() / 2, sum.getZ() / 2);
  }
  
  public String toString() {
    return "[" + this.start.toShortString() + " -> " + this.end.toShortString() + ", (" + String.valueOf(this.dimension) + ")]";
  }
  
  public boolean equals(Object o) {
    if (this == o)
      return true; 
    if (o == null || getClass() != o.getClass())
      return false; 
    adris.altoclef.util.BlockRange that = (adris.altoclef.util.BlockRange)o;
    return (Objects.equals(this.start, that.start) && Objects.equals(this.end, that.end));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.start, this.end });
  }
}
