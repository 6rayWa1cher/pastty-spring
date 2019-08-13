package com.a6raywa1cher.pasttyspring.components.coderunner.commandcompiler;

public class UnixCommandCompiler extends AbstractCommandCompiler {

	@Override
	protected String getDefaultCommand() {
		return "cp {1} {2}";
	}

	@Override
	protected String[] decorateCommand(String cmd) {
		return new String[]{"sh", "-c", cmd};
	}
}
