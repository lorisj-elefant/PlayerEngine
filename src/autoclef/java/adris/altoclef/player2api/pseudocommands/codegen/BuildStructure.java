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
    private static int numErrors = 0;

    private static final int maxNumErrors = 2;

    private static void onLLMErrMsg(String errMsg, LLMCompleter completer, Player2APIService service) {
        if (numErrors < maxNumErrors) {
            numErrors += 1;
            LOGGER.error("Err calling LLM={}", errMsg);
            buildStructureInternal(errMsg, completer, service);
            return;
        }
        numErrors = 0;
        LockManager.setCodeGenLock(false);
    }

    private static String llmReponseToCode(String input) {
        // remove formatting, markdown, etc.
        return input;
    }

    private static void onLLMResponse(String llmResponse, LLMCompleter completer) {
        LOGGER.info("llm responsed with={}", llmResponse);
        String code = llmReponseToCode(llmResponse);
        LOGGER.info("processed response into code={}", code);
        BuildStructureFromCode.buildStructureFromCode(code, (setBlockData) -> {
            LOGGER.info("setblock(x={}, y={}, z={}, blockName={})",
                    setBlockData.x,
                    setBlockData.y,
                    setBlockData.z,
                    setBlockData.blockName);
        }, (errStr) -> {
            LOGGER.error("While building got err={}", errStr);
        });
    }

    private static void buildStructureInternal(String description, LLMCompleter completer, Player2APIService service) {
        // get code from LLM
        ConversationHistory history = new ConversationHistory(Prompts.getBuildStructurePrompt());
        history.addUserMessage(String.format("Generate the code given this description: %s", description), service);
        completer.processWithStringResponse(
                service,
                history,
                BuildStructure::onLLMResponse,
                (errMsg) -> {
                    onLLMErrMsg(errMsg, completer, service);
                });
    }

    // external
    public static void buildStructure(String description, LLMCompleter completer, Player2APIService service) {
        LockManager.setCodeGenLock(true);
        numErrors = 0;
        buildStructureInternal(description, completer, service);
    }
}
