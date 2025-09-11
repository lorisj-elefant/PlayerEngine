package adris.altoclef.player2api;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

public class LLMCompleter {
    public static final Logger LOGGER = LogManager.getLogger();
    private boolean isCallingLLM = false;

    private static final ExecutorService llmThread = Executors.newSingleThreadExecutor();
    private Player2APIService service;

    public LLMCompleter(Player2APIService service) {
        this.service = service;
    }

    public class StringResponseRequest {
        ConversationHistory history;
        Consumer<String> extOnLLMResponse;
        Consumer<String> extOnErrMsg;

        public StringResponseRequest(
                ConversationHistory history,
                Consumer<String> extOnLLMResponse,
                Consumer<String> extOnErrMsg) {
            this.history = history;
            this.extOnLLMResponse = extOnLLMResponse;
            this.extOnErrMsg = extOnErrMsg;
        }
    }

    public void processWithStringResponse(
            StringResponseRequest req) {
        if (isCallingLLM) {
            LOGGER.error(
                    "Called llmcompleter.process when it was already processing! This should not happen. Cancelling call.");
            return;
        }

        LockManager.setOnLLMResponseLock(true);
        Consumer<String> onLLMResponse = resp -> {
            LOGGER.info("Done processing (string llm resp), isprocessing -> false");
            isCallingLLM = false;
            try {
                req.extOnLLMResponse.accept(resp);
            } catch (Exception e) {
                LOGGER.error(
                        "[EventQueueManager/LLMCompleter/process/onLLMResponse]: Error in external llm resp, errMsg={} llmResp={}",
                        e.getMessage(), resp.toString());
            } finally {
                LockManager.setOnLLMResponseLock(false);
            }
        };
        Consumer<String> onErrMsg = errMsg -> {
            LOGGER.info("Done processing (string err), isprocessing -> false");
            isCallingLLM = false;
            try {
                req.extOnErrMsg.accept(errMsg);
            } catch (Exception e) {
                LOGGER.error(
                        "[EventQueueManager/LLMCompleter/process/onErrMsg]: Error in external onErrmsg, errMsgFromException={} errMsg={}",
                        e.getMessage(), errMsg);
            } finally {
                LockManager.setOnLLMResponseLock(false);
            }
        };
        isCallingLLM = true;
        llmThread.submit(() -> {
            try {
                String response = service.completeConversationToString(req.history);
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
        if (isCallingLLM) {
            LOGGER.warn("Called llmcompleter.process when it was already processing! This should not happen.");
            return;
        }

        LockManager.setOnLLMResponseLock(true);
        Consumer<JsonObject> onLLMResponse = resp -> {
            LOGGER.info("Done processing (json llm resp), isprocessing -> false");
            isCallingLLM = false;
            try {
                extOnLLMResponse.accept(resp, this);
            } catch (Exception e) {
                LOGGER.error(
                        "[EventQueueManager/LLMCompleter/process/onLLMResponse]: Error in external llm resp, errMsg={} llmResp={}",
                        e.getMessage(), resp.toString());
            } finally {
                LockManager.setOnLLMResponseLock(false);
            }
        };

        Consumer<String> onErrMsg = errMsg -> {
            LOGGER.info("Done processing (json err), isprocessing -> false");
            isCallingLLM = false;
            try {
                extOnErrMsg.accept(errMsg);
            } catch (Exception e) {
                LOGGER.error(
                        "[EventQueueManager/LLMCompleter/process/onErrMsg]: Error in external onErrmsg, errMsgFromException={} errMsg={}",
                        e.getMessage(), errMsg);
            } finally {
                LockManager.setOnLLMResponseLock(false);
            }
        };

        isCallingLLM = true;

        llmThread.submit(() -> {
            try {
                JsonObject response = player2apiService.completeConversation(history);
                LOGGER.info("LLMCompleter returned json={}", response);
                onLLMResponse.accept(response);
            } catch (Exception e) {
                onErrMsg.accept(
                        e.getMessage() == null ? "Unknown error from CompleteConversation API" : e.getMessage());
            } finally {
                LockManager.setOnLLMResponseLock(false);
            }
        });
    }

    public boolean isAvailible() {
        return !isCallingLLM;
    }

    // public static void processUsingAvailibleCompleter(Consumer<LLMCompleter>
    // processer) {
    // if (LockManager.isConversationLocked()) {
    // return;
    // }
    // Stream<LLMCompleter> availibles =
    // llmCompleters.stream().filter(LLMCompleter::isAvailible);
    // if (availibles.toArray().length < 1) {
    // LOGGER.error("ALL LLM COMPLETERS BUSY, should not happen. Some locking error
    // has occured");
    // }
    // llmCompleters.stream().filter(LLMCompleter::isAvailible).forEach(processer);
    // }
}
