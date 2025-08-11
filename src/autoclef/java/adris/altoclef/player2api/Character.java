package adris.altoclef.player2api;

import java.util.Arrays;

public class Character {
   public final String name;
   public final String shortName;
   public final String greetingInfo;
   public final String description;
   public final String skinURL;
   public final String[] voiceIds;

   public Character(String characterName, String shortName, String greetingInfo, String description, String skinURL, String[] voiceIds) {
      this.name = characterName;
      this.shortName = shortName;
      this.greetingInfo = greetingInfo;
      this.voiceIds = voiceIds;
      this.skinURL = skinURL;
      this.description = description;
   }

   @Override
   public String toString() {
      return String.format(
         "Character{name='%s', shortName='%s', greeting='%s', voiceIds=%s}",
         this.name,
         this.shortName,
         this.greetingInfo,
         Arrays.toString((Object[])this.voiceIds)
      );
   }
}
