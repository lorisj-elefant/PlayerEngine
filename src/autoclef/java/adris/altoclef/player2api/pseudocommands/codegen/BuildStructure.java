package adris.altoclef.player2api.pseudocommands.codegen;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import adris.altoclef.AltoClefController;
import adris.altoclef.player2api.ConversationHistory;
import adris.altoclef.player2api.LLMCompleter;
import adris.altoclef.player2api.LockManager;
import adris.altoclef.player2api.Player2APIService;
import adris.altoclef.player2api.Prompts;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BuildStructure {
    public static final Logger LOGGER = LogManager.getLogger();

    private static int numErrors = 0;
    private static final int maxNumErrors = 2;

    private static String llmResponseToCode(String input) {
        // TODO: later can strip markdown ```, language hints, etc.
        return input;
    }

    private static void appendUserRegenerationPrompt(
            ConversationHistory history,
            Player2APIService service,
            String description,
            String errorMsg) {

        StringBuilder sb = new StringBuilder();
        if (errorMsg != null && !errorMsg.isEmpty()) {
            sb.append("The previous attempt failed with this error:\n")
                    .append(errorMsg)
                    .append("\n\n");
            sb.append("Try again and generate code using the same description: \"")
                    .append(description)
                    .append("\".\n")
                    .append("Only output valid executable code (no explanations, no markdown).");
        } else {
            sb.append("Generate code using this description:\"")
                    .append(description)
                    .append("\"");
        }

        history.addUserMessage(sb.toString(), service);
    }

    private static void requestCodeFromLLM(
            ConversationHistory history,
            LLMCompleter completer,
            Player2APIService service,
            String description,
            AltoClefController mod) {
        LOGGER.info("Requesting code from llm: history={} description={}", history, description);
        completer.processWithStringResponse(
                service,
                history,
                (llmResponse, completerParam, serviceParam) -> onLLMResponse(llmResponse, completerParam, serviceParam,
                        description, history, mod),
                (errMsg) -> onLLMTransportError(errMsg, completer, service, description, history));
    }

    private static void onLLMResponse(
            String llmResponse,
            LLMCompleter completer,
            Player2APIService service,
            String description,
            ConversationHistory history,
            AltoClefController mod) {
        LOGGER.info("LLM responded with code as string={}", llmResponse);

        String code = llmResponseToCode(llmResponse);
        LOGGER.info("Processed response into code={}", code);

        history.addAssistantMessage(code, service);
        // for now do it sync
        BuildStructureFromCode.buildStructureFromCode(
                code,
                (setBlockData) -> {
                    LOGGER.info("setBlock(x={}, y={}, z={}, blockName={})");
                    // setBlockData.x, setBlockData.y, setBlockData.z, setBlockData.blockName);
                    ResourceLocation id = new ResourceLocation("minecraft", setBlockData.blockName);
                    Block block = BuiltInRegistries.BLOCK.get(id);
                    // 3 means send to clients (2) and notify neighbors/update block states (1).
                    // maybe do 2 if you dont want
                    // redstone/etc updating/torches falling probably
                    mod.getWorld().setBlock(new BlockPos(setBlockData.x, setBlockData.y, setBlockData.z),
                            block.defaultBlockState(), 3);

                },
                (errStr) -> {
                    LOGGER.error("While building got err={}", errStr);
                    onCodeValidationError(errStr, code, completer, service, description, history, mod);
                },
                () -> {
                    LOGGER.info("Building structure done, releasing code gen lock");
                    LockManager.setCodeGenLock(false);
                });

    }

    private static void onLLMTransportError(
            String errMsg,
            LLMCompleter completer,
            Player2APIService service,
            String description,
            ConversationHistory history) {

        LOGGER.error("LLM transport/call error={}", errMsg);
        LockManager.setCodeGenLock(false);
    }

    private static void onCodeValidationError(
            String errMsg,
            String lastCode,
            LLMCompleter completer,
            Player2APIService service,
            String description,
            ConversationHistory history,
            AltoClefController mod) {

        if (numErrors < maxNumErrors) {
            LOGGER.info("onCodeValidationError: trying again, errMsg={}", errMsg);
            numErrors += 1;

            appendUserRegenerationPrompt(history, service, description, errMsg);
            requestCodeFromLLM(history, completer, service, description, mod);
            return;
        }
        LOGGER.info("Too many erorrs, exiting. Last errMsg={}", errMsg);
        numErrors = 0;
        LockManager.setCodeGenLock(false);
    }

    private static void buildStructureInternal(
            String description,
            LLMCompleter completer,
            Player2APIService service,
            ConversationHistory history,
            AltoClefController mod) {
        appendUserRegenerationPrompt(history, service, description, null);
        requestCodeFromLLM(history, completer, service, description, mod);
    }

    public static void buildStructure(
            String description,
            LLMCompleter completer,
            Player2APIService service,
            AltoClefController mod) {

        LockManager.setCodeGenLock(true);
        numErrors = 0;

        ConversationHistory history = new ConversationHistory(Prompts.getBuildStructurePrompt());

        history.addUserMessage(
                "Generate code for the following description. "
                        + "Only output valid executable code (no explanations, no markdown). "
                        + "Description: \"" + description + "\"",
                service);

        buildStructureInternal(description, completer, service, history, mod);
    }
}