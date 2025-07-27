// File: adris/altoclef/tasks/squashed/SmithingSquasher.java
package adris.altoclef.tasks.squashed;

import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasks.container.UpgradeInSmithingTableTask;
import adris.altoclef.util.ItemTarget;

import java.util.ArrayList;
import java.util.List;

public class SmithingSquasher extends TypeSquasher<UpgradeInSmithingTableTask> {

  @Override
  protected List<ResourceTask> getSquashed(List<UpgradeInSmithingTableTask> tasks) {
    if (tasks.isEmpty()) {
      return new ArrayList<>();
    }

    List<ResourceTask> result = new ArrayList<>();
    List<ItemTarget> materialsToCollect = new ArrayList<>();

    // Collect all unique materials, tools, and templates needed for all upgrades.
    // The count for each target will be the sum of all required counts.
    // This part is more complex than a simple addAll, but for now, we'll keep it simple.
    // A proper implementation would merge ItemTargets of the same type.
    for (UpgradeInSmithingTableTask task : tasks) {
      // We get the targets directly from the task instance.
      materialsToCollect.add(task.getMaterials());
      materialsToCollect.add(task.getTools());
      materialsToCollect.add(task.getTemplate());
    }

    // Create one big resource collection task.
    // On the server, we use CataloguedResourceTask to handle collecting a list of items.
    if (!materialsToCollect.isEmpty()) {
      // TODO: A more advanced implementation could merge ItemTargets here.
      // For example, if two tasks need 5 diamonds and 3 diamonds, we should
      // create a single ItemTarget for 8 diamonds.
      // For now, this is a functional simplification.
      result.add(new CataloguedResourceTask(materialsToCollect.toArray(new ItemTarget[0])));
    }

    // Add the individual upgrade tasks to be executed after materials are collected.
    // These will run sequentially.
    result.addAll(tasks);

    return result;
  }
}