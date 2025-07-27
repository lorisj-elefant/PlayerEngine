package adris.altoclef.control;

import adris.altoclef.AltoClefController;
import baritone.api.utils.Rotation;
import baritone.api.utils.input.Input;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

public class InputControls {
  AltoClefController controller;

  public InputControls(AltoClefController controller){
    this.controller = controller;
  }

  private final Queue<Input> toUnpress = new ArrayDeque<>();
  
  private final Set<Input> _waitForRelease = new HashSet<>();
  
  public void tryPress(Input input) {
    if (this._waitForRelease.contains(input))
      return;

    controller.getBaritone().getInputOverrideHandler().setInputForceState(input, true);
    this.toUnpress.add(input);
    this._waitForRelease.add(input);
  }
  
  public void hold(Input input) {
    controller.getBaritone().getInputOverrideHandler().setInputForceState(input, true);
  }
  
  public void release(Input input) {
    controller.getBaritone().getInputOverrideHandler().setInputForceState(input, false);
  }


  public boolean isHeldDown(Input input) {
    return controller.getBaritone().getInputOverrideHandler().isInputForcedDown(input);
  }
  
  public void forceLook(float yaw, float pitch) {
    controller.getBaritone().getLookBehavior().updateTarget(new Rotation(yaw, pitch), true);
  }
  
  public void onTickPre() {
    while (!this.toUnpress.isEmpty())
      controller.getBaritone().getInputOverrideHandler().setInputForceState(this.toUnpress.remove(), false);
  }
  
  public void onTickPost() {
    this._waitForRelease.clear();
  }
}
