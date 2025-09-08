package adris.altoclef.player2api;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.gson.JsonObject;

public class LLMCompleter {
    public static final Logger LOGGER = LogManager.getLogger();
    private boolean isProcessing = false;

    private static final ExecutorService llmThread = Executors.newSingleThreadExecutor();
    private static List<LLMCompleter> llmCompleters = List.of(new LLMCompleter());

    public void processWithStringResponse(
            Player2APIService player2apiService,
            ConversationHistory history,
            BiConsumer<String, LLMCompleter> extOnLLMResponse,
            Consumer<String> extOnErrMsg) {
        if (isProcessing) {
            LOGGER.warn("Called llmcompleter.process when it was already processing! This should not happen.");
            return;
        }
        Consumer<String> onLLMResponse = resp -> {
            try {
                extOnLLMResponse.accept(resp, this);
            } catch (Exception e) {
                LOGGER.error(
                        "[EventQueueManager/LLMCompleter/process/onLLMResponse]: Error in external llm resp, errMsg={} llmResp={}",
                        e.getMessage(), resp.toString());
            } finally {
                LOGGER.info("Done processing, isprocessing -> false");
                isProcessing = false;
            }
        };
        Consumer<String> onErrMsg = errMsg -> {
            try {
                extOnErrMsg.accept(errMsg);
            } catch (Exception e) {
                LOGGER.error(
                        "[EventQueueManager/LLMCompleter/process/onErrMsg]: Error in external onErrmsg, errMsgFromException={} errMsg={}",
                        e.getMessage(), errMsg);
            } finally {
                isProcessing = false;
            }
        };
        isProcessing = true;
        llmThread.submit(() -> {
            try {
                String response = player2apiService.completeConversationToString(history);
                LOGGER.info("LLMCompleter returned as string={}", response);
                onLLMResponse.accept(response);
            } catch (Exception e) {
                onErrMsg.accept(
                        e.getMessage() == null ? "Unknown error from CompleteConversation API" : e.getMessage());
            }
        });
    }

    public void processWithJsonResponse(
            Player2APIService player2apiService,
            ConversationHistory history,
            BiConsumer<JsonObject, LLMCompleter> extOnLLMResponse,
            Consumer<String> extOnErrMsg) {
        if (isProcessing) {
            LOGGER.warn("Called llmcompleter.process when it was already processing! This should not happen.");
            return;
        }

        Consumer<JsonObject> onLLMResponse = resp -> {
            try {
                extOnLLMResponse.accept(resp, this);
            } catch (Exception e) {
                LOGGER.error(
                        "[EventQueueManager/LLMCompleter/process/onLLMResponse]: Error in external llm resp, errMsg={} llmResp={}",
                        e.getMessage(), resp.toString());
            } finally {
                LOGGER.info("Done processing, isprocessing -> false");
                isProcessing = false;
            }
        };

        Consumer<String> onErrMsg = errMsg -> {
            try {
                extOnErrMsg.accept(errMsg);
            } catch (Exception e) {
                LOGGER.error(
                        "[EventQueueManager/LLMCompleter/process/onErrMsg]: Error in external onErrmsg, errMsgFromException={} errMsg={}",
                        e.getMessage(), errMsg);
            } finally {
                isProcessing = false;
            }
        };
        isProcessing = true;
        llmThread.submit(() -> {
            try {
                JsonObject response = player2apiService.completeConversation(history);
                LOGGER.info("LLMCompleter returned json={}", response);
                onLLMResponse.accept(response);
            } catch (Exception e) {
                onErrMsg.accept(
                        e.getMessage() == null ? "Unknown error from CompleteConversation API" : e.getMessage());
            }
        });
    }

    public boolean isAvailible() {
        return !isProcessing;
    }

    public static void processUsingAvailibleCompleter(Consumer<LLMCompleter> processer) {
        if (LockManager.globalIsLocked()) {
            return;
        }
        Stream<LLMCompleter> availibles = llmCompleters.stream().filter(LLMCompleter::isAvailible);
        if (availibles.toArray().length < 1) {
            LOGGER.error("ALL LLM COMPLETERS BUSY, should not happen. Some locking error has occured");
        }
        llmCompleters.stream().filter(LLMCompleter::isAvailible).forEach(processer);
    }
}
