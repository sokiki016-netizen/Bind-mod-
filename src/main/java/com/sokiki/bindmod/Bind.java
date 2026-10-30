package com.sokiki.bindmod;

/**
 * Один бинд: клавиша + команда/сообщение в чат.
 */
public final class Bind {
	public BindKey key;
	public String command;

	public Bind(BindKey key, String command) {
		this.key = key;
		this.command = command == null ? "" : command;
	}

	public String shortCommand(int max) {
		String cmd = this.command == null ? "" : this.command.trim();
		if (cmd.length() <= max) {
			return cmd;
		}
		return cmd.substring(0, Math.max(0, max - 1)) + "…";
	}
}
