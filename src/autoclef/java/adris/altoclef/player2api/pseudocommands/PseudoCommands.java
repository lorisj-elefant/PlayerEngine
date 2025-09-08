package adris.altoclef.player2api.pseudocommands;

import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import adris.altoclef.player2api.LLMCompleter;
import adris.altoclef.player2api.Player2APIService;
import adris.altoclef.player2api.pseudocommands.codegen.BuildStructure;

public class PseudoCommands {
    public static Logger LOGGER = LogManager.getLogger();

    public static class PseudoCommand {
        String name;
        String description;

        public PseudoCommand(String name, String description) {
            this.name = name;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }
    }

    public static List<PseudoCommand> pseudoCommands = List.of(
            new PseudoCommand("build_structure",
                    "you provide a description, and the agent will build a structure matching that description. Example call would be `build_structure a gray modern house with a garden of roses in front of it`"));

    public static Optional<PseudoCommand> getPseudocommandOption(String cmd) {
        return pseudoCommands.stream().filter((p) -> cmd.contains(p.getName())).findFirst();
    }

    public static void process(PseudoCommand cmd, String commandWithPrefix, LLMCompleter completer,
            Player2APIService service) {
        LOGGER.info("Processing PseudoCommand={}, commandWithPrefix={}", cmd, commandWithPrefix);
        switch (cmd.name) {
            case "build_structure":
                String description = commandWithPrefix.split("build_structure")[1].strip();
                BuildStructure.buildStructure(description, completer, service);
                break;
            default:
                LOGGER.error("for cmdWithPrefix={} could not find matching pseudocommand", commandWithPrefix);
        }
    }
}
