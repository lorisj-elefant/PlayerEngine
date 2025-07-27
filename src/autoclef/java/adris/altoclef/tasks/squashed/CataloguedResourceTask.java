package adris.altoclef.tasks.squashed;

import adris.altoclef.AltoClefController;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasks.container.CraftInTableTask;
import adris.altoclef.tasks.container.UpgradeInSmithingTableTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.StorageHelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

public class CataloguedResourceTask extends ResourceTask {
  private final TaskSquasher squasher;
  
  private final ItemTarget[] targets;
  
  private final List<ResourceTask> tasksToComplete;
  
  public CataloguedResourceTask(boolean squash, ItemTarget... targets) {
    super(targets);
    this.squasher = new TaskSquasher();
    this.targets = targets;
    this.tasksToComplete = new ArrayList<>(targets.length);
    for (ItemTarget target : targets) {
      if (target != null)
        this.tasksToComplete.add(TaskCatalogue.getItemTask(target)); 
    } 
    if (squash)
      squashTasks(this.tasksToComplete); 
  }
  
  public CataloguedResourceTask(ItemTarget... targets) {
    this(true, targets);
  }
  
  protected void onResourceStart(AltoClefController mod) {}
  
  protected Task onResourceTick(AltoClefController mod) {
    for (ResourceTask task : this.tasksToComplete) {
      for (ItemTarget target : task.getItemTargets()) {
        if (!StorageHelper.itemTargetsMetInventory(mod, new ItemTarget[] { target }))
          return (Task)task; 
      } 
    } 
    return null;
  }
  
  public boolean isFinished() {
    for (ResourceTask task : this.tasksToComplete) {
      for (ItemTarget target : task.getItemTargets()) {
        if (!StorageHelper.itemTargetsMetInventory(controller, new ItemTarget[] { target }))
          return false; 
      } 
    } 
    return true;
  }
  
  protected boolean shouldAvoidPickingUp(AltoClefController mod) {
    return false;
  }
  
  protected void onResourceStop(AltoClefController mod, Task interruptTask) {}
  
  protected boolean isEqualResource(ResourceTask other) {
    if (other instanceof adris.altoclef.tasks.squashed.CataloguedResourceTask) {
      adris.altoclef.tasks.squashed.CataloguedResourceTask task = (adris.altoclef.tasks.squashed.CataloguedResourceTask)other;
      return Arrays.equals(task.targets, this.targets);
    } 
    return false;
  }
  
  protected String toDebugStringName() {
    return "Get catalogued: " + ArrayUtils.toString(this.targets);
  }
  
  private void squashTasks(List<ResourceTask> tasks) {
    this.squasher.addTasks(tasks);
    tasks.clear();
    tasks.addAll(this.squasher.getSquashed());
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  static class TaskSquasher {

    private final Map<Class, TypeSquasher> _squashMap = new HashMap<>();

    private final List<ResourceTask> _unSquashableTasks = new ArrayList<>();

    public TaskSquasher() {
      _squashMap.put(CraftInTableTask.class, new CraftSquasher());
      _squashMap.put(UpgradeInSmithingTableTask.class, new SmithingSquasher());
      //_squashMap.put(MineAndCollectTask.class)
    }

    public void addTask(ResourceTask t) {
      Class type = t.getClass();
      if (_squashMap.containsKey(type)) {
        _squashMap.get(type).add(t);
      } else {
        //Debug.logMessage("Unsquashable: " + type + ": " + t);
        _unSquashableTasks.add(t);
      }
    }

    public void addTasks(List<ResourceTask> tasks) {
      for (ResourceTask task : tasks) {
        addTask(task);
      }
    }

    public List<ResourceTask> getSquashed() {
      List<ResourceTask> result = new ArrayList<>();

      for (Class type : _squashMap.keySet()) {
        result.addAll(_squashMap.get(type).getSquashed());
      }
      result.addAll(_unSquashableTasks);

      return result;
    }
  }
}
