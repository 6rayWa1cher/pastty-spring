package com.a6raywa1cher.pasttyspring.components.coderunner.commandcompiler;

public class WindowsCommandCompiler extends AbstractCommandCompiler {

	@Override
	protected String getDefaultCommand() {
		return "copy \"{1}\" \"{2}\"";
	}

	@Override
	protected String[] decorateCommand(String cmd) {
		return new String[]{"cmd.exe", "/C", cmd};
	}
}
