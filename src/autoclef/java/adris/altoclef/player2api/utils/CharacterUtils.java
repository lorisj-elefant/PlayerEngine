/*
 * This file is part of Baritone.
 *
 * Baritone is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Baritone is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Baritone.  If not, see <https://www.gnu.org/licenses/>.
 */

package adris.altoclef.player2api.utils;

import adris.altoclef.player2api.Character;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.PacketByteBuf;

import java.util.Map;

import static adris.altoclef.player2api.utils.HTTPUtils.sendRequest;

public class CharacterUtils {
    public static Character DEFAULT_CHARACTER = new Character("AI agent", "AI", "Greetings", "You are a helpful AI Agent", "minecraft:textures/entity/player/wide/steve.png", new String[0]);;

    public static Character parseFirstCharacter(Map<String, JsonElement> responseMap) {
        Character[] characters = parseCharacters(responseMap);
        if(characters.length>0){
            return characters[0];
        }
        return DEFAULT_CHARACTER;
    }

    public static Character[] parseCharacters(Map<String, JsonElement> responseMap) {
        try {
            if (!responseMap.containsKey("characters"))
                throw new Exception("No characters found in API response.");
            JsonArray charactersArray = responseMap.get("characters").getAsJsonArray();
            if (charactersArray.isEmpty())
                throw new Exception("Character list is empty.");
            Character[] characters = new Character[charactersArray.size()];
            for (int i = 0; i < charactersArray.size(); i++) {
                JsonObject firstCharacter = charactersArray.get(i).getAsJsonObject();
                String name = Utils.getStringJsonSafely(firstCharacter, "name");
                if (name == null)
                    throw new Exception("Character is missing 'name'.");
                String shortName = Utils.getStringJsonSafely(firstCharacter, "short_name");
                if (shortName == null)
                    throw new Exception("Character is missing 'short_name'.");
                String greeting = Utils.getStringJsonSafely(firstCharacter, "greeting");
                String description = Utils.getStringJsonSafely(firstCharacter, "description");
                String[] voiceIds = Utils.getStringArrayJsonSafely(firstCharacter, "voice_ids");
                JsonObject meta = firstCharacter.get("meta").getAsJsonObject();
                String skinURL = Utils.getStringJsonSafely(meta, "skin_url");
                characters[i] = new Character(name, shortName, greeting, description, skinURL, voiceIds);
            }
            return characters;
        } catch (Exception e) {
            System.err.println("Warning, getSelectedCharacter failed, reverting to default. Error message: " + e.getMessage());
        }
        return new Character[0];
    }

    public static Character[] requestCharacters(){
        try {
            Map<String, JsonElement> responseMap = sendRequest("/v1/selected_characters", false, null);
            return CharacterUtils.parseCharacters(responseMap);
        }catch (Exception e){
            return new Character[0];
        }
    }

    public static Character requestFirstCharacter(){
        try {
            Map<String, JsonElement> responseMap = sendRequest("/v1/selected_characters", false, null);
            return CharacterUtils.parseFirstCharacter(responseMap);
        }catch (Exception e){
            return DEFAULT_CHARACTER;
        }
    }

    public static Character readFromBuf(PacketByteBuf buf){
        String name = buf.readString();
        String shortName = buf.readString();
        String greetingInfo = buf.readString();
        String description = buf.readString();
        String skinURL = buf.readString();
        int arrSize = buf.readInt();
        String[] voiceIds = new String[arrSize];
        for(int i=0;i<arrSize;i++){
            voiceIds[i] = buf.readString();
        }
        return new Character(name, shortName, greetingInfo, description, skinURL, voiceIds);
    }

    public static void writeToBuf(PacketByteBuf buf, Character character){
        buf.writeString(character.name);
        buf.writeString(character.shortName);
        buf.writeString(character.greetingInfo);
        buf.writeString(character.description);
        buf.writeString(character.skinURL);
        buf.writeInt(character.voiceIds.length);
        for(String id: character.voiceIds){
            buf.writeString(id);
        }
    }
}
