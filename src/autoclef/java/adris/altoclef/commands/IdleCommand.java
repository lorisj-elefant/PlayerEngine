package adris.altoclef.commands;

import adris.altoclef.AltoClefController;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.tasks.LookAtOwnerTask;

public class IdleCommand extends Command {
   public IdleCommand() {
      super("idle", "Stand still");
   }

   @Override
   protected void call(AltoClefController mod, ArgParser parser) {
      mod.runUserTask(new LookAtOwnerTask(), () -> this.finish());
   }
}
