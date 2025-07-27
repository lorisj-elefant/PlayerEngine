package adris.altoclef.player2api.status;

import java.util.HashMap;
import java.util.Map;

public class ObjectStatus {
  protected final Map<String, String> fields = new HashMap<>();
  
  public adris.altoclef.player2api.status.ObjectStatus add(String key, String value) {
    this.fields.put(key, value);
    return this;
  }
  
  public String toString() {
    StringBuilder sb = new StringBuilder("{\n");
    for (Map.Entry<String, String> entry : this.fields.entrySet())
      sb.append(entry.getKey()).append(" : \"").append(entry.getValue()).append("\",\n"); 
    sb.append("}");
    return sb.toString();
  }
}
