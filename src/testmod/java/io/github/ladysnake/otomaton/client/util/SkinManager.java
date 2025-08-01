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

package io.github.ladysnake.otomaton.client.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Identifier;

import java.io.File;

public class SkinManager {

    private static final Identifier STEVE_SKIN_ID = new Identifier("textures/entity/player/wide/steve.png");

    public static Identifier getSkinIdentifier(String skinUrl) {
        if (skinUrl == null || skinUrl.isEmpty()) {
            return STEVE_SKIN_ID;
        }

        Identifier location = ResourceDownloader.getUrlResourceLocation(skinUrl, true);

        if (MinecraftClient.getInstance().getTextureManager().getOrDefault(location, null) != null) {
            return location;
        }

        File cacheFile = ResourceDownloader.getUrlFile(skinUrl, true);
        ImageDownloadAlt downloader = new ImageDownloadAlt(cacheFile, skinUrl, location, STEVE_SKIN_ID, true, () -> {});
        ResourceDownloader.load(downloader);

        return STEVE_SKIN_ID;
    }

    public static void renderSkinHead(GuiGraphics graphics, int x, int y, int size, Identifier skinIdentifier) {
        RenderSystem.setShaderTexture(0, skinIdentifier);
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();

        int faceU = 8;
        int faceV = 8;
        int faceWidth = 8;
        int faceHeight = 8;
        int textureWidth = 64;
        int textureHeight = 64;

        graphics.drawTexture(skinIdentifier, x, y, size, size, faceU, faceV, faceWidth, faceHeight, textureWidth, textureHeight);

        int hatU = 40;
        int hatV = 8;
        int hatWidth = 8;
        int hatHeight = 8;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.drawTexture(skinIdentifier, x, y, size, size, hatU, hatV, hatWidth, hatHeight, textureWidth, textureHeight);
        RenderSystem.disableBlend();
    }
}