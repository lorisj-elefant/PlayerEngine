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

package altoclef.control;

import altoclef.AltoClefController;
import baritone.api.utils.Rotation;
import baritone.api.utils.input.Input;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

public class InputControls {
   AltoClefController controller;
   private final Queue<Input> toUnpress = new ArrayDeque<>();
   private final Set<Input> waitForRelease = new HashSet<>();

   public InputControls(AltoClefController controller) {
      this.controller = controller;
   }

   public void tryPress(Input input) {
      if (!this.waitForRelease.contains(input)) {
         this.controller.getBaritone().getInputOverrideHandler().setInputForceState(input, true);
         this.toUnpress.add(input);
         this.waitForRelease.add(input);
      }
   }

   public void hold(Input input) {
      this.controller.getBaritone().getInputOverrideHandler().setInputForceState(input, true);
   }

   public void release(Input input) {
      this.controller.getBaritone().getInputOverrideHandler().setInputForceState(input, false);
   }

   public boolean isHeldDown(Input input) {
      return this.controller.getBaritone().getInputOverrideHandler().isInputForcedDown(input);
   }

   public void forceLook(float yaw, float pitch) {
      this.controller.getBaritone().getLookBehavior().updateTarget(new Rotation(yaw, pitch), true);
   }

   public void onTickPre() {
      while (!this.toUnpress.isEmpty()) {
         this.controller.getBaritone().getInputOverrideHandler().setInputForceState(this.toUnpress.remove(), false);
      }
   }

   public void onTickPost() {
      this.waitForRelease.clear();
   }
}
