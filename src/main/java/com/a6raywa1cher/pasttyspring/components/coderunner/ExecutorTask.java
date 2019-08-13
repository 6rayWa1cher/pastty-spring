package com.a6raywa1cher.pasttyspring.components.coderunner;

import java.util.concurrent.Callable;

public interface ExecutorTask extends Callable<CodeRunnerResponse>, AutoCloseable {
}
