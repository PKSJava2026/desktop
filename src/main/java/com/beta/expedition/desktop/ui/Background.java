package com.beta.expedition.desktop.ui;

import com.beta.expedition.desktop.api.ApiException;
import com.beta.expedition.desktop.service.ValidationException;
import javafx.beans.property.BooleanProperty;
import javafx.concurrent.Task;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class Background {

    private static final System.Logger LOGGER = System.getLogger(Background.class.getName());
    private static final String UNEXPECTED_ERROR = "Непредвиденная ошибка. Повторите действие.";
    private static final Executor EXECUTOR = Executors.newCachedThreadPool(runnable -> {
        Thread thread = new Thread(runnable, "api-call");
        thread.setDaemon(true);
        return thread;
    });

    private Background() {
    }

    public static <T> void run(BooleanProperty busy, Supplier<T> call, Consumer<T> onSuccess,
                               Consumer<String> onError) {
        Task<T> task = new Task<>() {
            @Override
            protected T call() {
                return call.get();
            }
        };
        busy.set(true);
        task.setOnSucceeded(event -> {
            busy.set(false);
            onSuccess.accept(task.getValue());
        });
        task.setOnFailed(event -> {
            busy.set(false);
            onError.accept(messageOf(task.getException()));
        });
        EXECUTOR.execute(task);
    }

    private static String messageOf(Throwable error) {
        if (error instanceof ApiException || error instanceof ValidationException) {
            return error.getMessage();
        }
        LOGGER.log(System.Logger.Level.ERROR, "Необработанная ошибка вызова", error);
        return UNEXPECTED_ERROR;
    }
}
