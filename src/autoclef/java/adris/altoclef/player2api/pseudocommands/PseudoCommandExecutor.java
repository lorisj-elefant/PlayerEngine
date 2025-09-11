package adris.altoclef.player2api.pseudocommands;

import java.util.Optional;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import adris.altoclef.AltoClefController;
import adris.altoclef.player2api.LLMCompleter;
import adris.altoclef.player2api.Player2APIService;
import adris.altoclef.player2api.pseudocommands.PseudoCommands.PseudoCommand;
import adris.altoclef.player2api.pseudocommands.codegen.BuildStructure;

public class PseudoCommandExecutor {

    public interface PseudoCommandRunner {
        public void onStart();

        public void onStop();
    }

    public static Logger LOGGER = LogManager.getLogger();
    private AltoClefController mod;
    private Player2APIService service;

    private Optional<String> cmdStatus = Optional.empty();

    public static LLMCompleter toolLLM;

    public static Optional<LLMCompleter> tryToStartUsingLLM() {
        if (!toolLLM.isAvailible()) {
            return Optional.empty();
        }
        return Optional.of(toolLLM);
    }

    public PseudoCommandExecutor(AltoClefController mod, Player2APIService service) {
        this.mod = mod;
        this.service = service;
        toolLLM = new LLMCompleter(service);
    }

    public void setCmdStatus(String status) {
        cmdStatus = Optional.of(status);
    }

    public Optional<String> getStatus() {
        return cmdStatus;
    }

    public void execute(PseudoCommands.PseudoCommand pcmd, String commandWithPrefix, Runnable onStopExt,
            Consumer<String> onErrMsg) {
        LOGGER.info("Processing PseudoCommand={}, commandWithPrefix={}", pcmd.name, commandWithPrefix);

        Runnable onStop = () -> {
            cmdStatus = Optional.empty();
            onStopExt.run();
        };

        switch (pcmd.name) {
            case "build_structure":
                String description = commandWithPrefix.split("build_structure")[1].strip();
                BuildStructure.buildStructure(description, toolLLM, service, mod);
        }
    }
}
