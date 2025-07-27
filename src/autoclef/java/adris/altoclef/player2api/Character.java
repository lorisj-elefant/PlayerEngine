package adris.altoclef.player2api;

import java.util.Arrays;

public class Character {
  public final String name;
  
  public final String shortName;
  
  public final String greetingInfo;
  
  public final String description;
  
  public final String[] voiceIds;
  
  public Character(String characterName, String shortName, String greetingInfo, String description, String[] voiceIds) {
    this.name = characterName;
    this.shortName = shortName;
    this.greetingInfo = greetingInfo;
    this.voiceIds = voiceIds;
    this.description = description;
  }
  
  public String toString() {
    return String.format("Character{name='%s', shortName='%s', greeting='%s', voiceIds=%s}", new Object[] { this.name, this.shortName, this.greetingInfo, 
          
          Arrays.toString(this.voiceIds) });
  }
}
