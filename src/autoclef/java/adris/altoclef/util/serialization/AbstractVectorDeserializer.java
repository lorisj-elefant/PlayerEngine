package adris.altoclef.util.serialization;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractVectorDeserializer<T, UnitType> extends StdDeserializer<T> {
  protected AbstractVectorDeserializer() {
    this(null);
  }
  
  protected AbstractVectorDeserializer(Class<T> vc) {
    super(vc);
  }
  
  protected abstract String getTypeName();
  
  protected abstract String[] getComponents();
  
  protected abstract UnitType parseUnit(String paramString) throws Exception;
  
  protected abstract T deserializeFromUnits(List<UnitType> paramList);
  
  protected abstract boolean isUnitTokenValid(JsonToken paramJsonToken);
  
  UnitType trySet(JsonParser p, Map<String, UnitType> map, String key) throws JsonParseException {
    if (map.containsKey(key))
      return map.get(key); 
    throw new JsonParseException(p, getTypeName() + " should have key for " + getTypeName() + " key, but one was not found.");
  }
  
  UnitType tryParse(JsonParser p, String whole, String part) throws JsonParseException {
    try {
      return parseUnit(part.trim());
    } catch (Exception e) {
      throw new JsonParseException(p, "Failed to parse " + getTypeName() + " string \"" + whole + "\", specificaly part \"" + part + "\".");
    } 
  }
  
  public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    String[] neededComponents = getComponents();
    if (p.getCurrentToken() == JsonToken.VALUE_STRING) {
      String bposString = p.getValueAsString();
      String[] parts = bposString.split(",");
      if (parts.length != neededComponents.length)
        throw new JsonParseException(p, "Invalid " + getTypeName() + " string: \"" + bposString + "\", must be in form \"" + String.join(",", neededComponents) + "\"."); 
      ArrayList<UnitType> resultingUnits = new ArrayList<>();
      for (String part : parts)
        resultingUnits.add(tryParse(p, bposString, part)); 
      return deserializeFromUnits(resultingUnits);
    } 
    if (p.getCurrentToken() == JsonToken.START_OBJECT) {
      Map<String, UnitType> parts = new HashMap<>();
      p.nextToken();
      while (p.getCurrentToken() != JsonToken.END_OBJECT) {
        if (p.getCurrentToken() == JsonToken.FIELD_NAME) {
          p.nextToken();
          if (!isUnitTokenValid(p.currentToken()))
            throw new JsonParseException(p, "Invalid token for " + getTypeName() + ". Got: " + String.valueOf(p.getCurrentToken())); 
          try {
            parts.put(p.getCurrentName(), parseUnit(p.getValueAsString()));
          } catch (Exception e) {
            throw new JsonParseException(p, "Failed to parse unit " + p.getCurrentName());
          } 
          p.nextToken();
          continue;
        } 
        throw new JsonParseException(p, "Invalid structure, expected field name (like " + String.join(",", neededComponents) + ")");
      } 
      if (parts.size() != neededComponents.length)
        throw new JsonParseException(p, "Expected [" + String.join(",", neededComponents) + "] keys to be part of a blockpos object. Got " + Arrays.toString(parts.keySet().toArray(x$0 -> new String[x$0]))); 
      ArrayList<UnitType> resultingUnits = new ArrayList<>();
      for (String componentName : neededComponents)
        resultingUnits.add(trySet(p, parts, componentName)); 
      return deserializeFromUnits(resultingUnits);
    } 
    throw new JsonParseException(p, "Invalid token: " + String.valueOf(p.getCurrentToken()));
  }
}
