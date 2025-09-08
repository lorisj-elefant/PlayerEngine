package adris.altoclef.player2api.pseudocommands.codegen;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import adris.altoclef.player2api.ConversationHistory;
import adris.altoclef.player2api.LLMCompleter;
import adris.altoclef.player2api.LockManager;
import adris.altoclef.player2api.Player2APIService;
import adris.altoclef.player2api.Prompts;

public class BuildStructure {
    public static final Logger LOGGER = LogManager.getLogger();

    private static String llmReponseToCode(String input) {
        // remove formatting, markdown, etc.
        return input;
    }

    private static void onLLMResponse(String llmResponse) {
        LockManager.setCodeGenLock(false);
        LOGGER.info("llm responsed with={}", llmResponse);
        String code = llmReponseToCode(llmResponse);
        LOGGER.info("processed code into={}", code);
        BuildStructureFromCode.buildStructureFromCode(code, (setBlockData) -> {
            LOGGER.info("Setblock(x={}, y={}, z={}, blockName={})", setBlockData.x, setBlockData.y, setBlockData.z,
                    setBlockData.blockName);
        }, (errStr) -> {
            LOGGER.error("Building err={}", errStr);
        });
    }

    private static void onLLMErrMsg(String errMsg) {
        LOGGER.error("Err calling LLM={}", errMsg);
        LockManager.setCodeGenLock(false);
    }

    public static void buildStructure(String description, Player2APIService service) {
        LockManager.setCodeGenLock(true);
        // get code from LLM
        ConversationHistory history = new ConversationHistory(Prompts.getBuildStructurePrompt());
        history.addUserMessage(String.format("Generate the code given this description: %s", description), service);
        LLMCompleter.processUsingAvailibleCompleter(
                (cmp) -> {
                    cmp.processWithStringResponse(service, history,
                            BuildStructure::onLLMResponse,
                            BuildStructure::onLLMErrMsg);
                });
    }
}
