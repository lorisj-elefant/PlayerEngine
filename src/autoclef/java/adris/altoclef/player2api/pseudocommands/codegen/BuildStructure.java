package adris.altoclef.player2api.pseudocommands.codegen;

import com.google.gson.JsonObject;

import adris.altoclef.player2api.ConversationHistory;
import adris.altoclef.player2api.LLMCompleter;
import adris.altoclef.player2api.LockManager;
import adris.altoclef.player2api.Player2APIService;
import adris.altoclef.player2api.Prompts;

public class BuildStructure {

    private static void onLLMResponse(JsonObject obj) {

    }

    private static void onLLMErrMsg(String errMsg) {

    }

    public static void buildStructure(String description, Player2APIService service) {
        LockManager.setCodeGenLock(true);
        // 1) get code from LLM
        ConversationHistory history = new ConversationHistory(Prompts.getBuildStructurePrompt());
        LLMCompleter.processUsingAvailibleCompleter(
                (cmp) -> {
                    cmp.process(service, history, BuildStructure::onLLMResponse, BuildStructure::onLLMErrMsg);
                });
    }
}
