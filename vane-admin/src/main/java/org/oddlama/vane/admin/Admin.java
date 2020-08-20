package org.oddlama.vane.admin;

import org.bukkit.event.Listener;

import org.oddlama.vane.annotation.VaneModule;
import org.oddlama.vane.annotation.config.ConfigVersion;
import org.oddlama.vane.annotation.lang.LangString;
import org.oddlama.vane.annotation.lang.LangVersion;
import org.oddlama.vane.core.Module;

@VaneModule("admin")
public class Admin extends Module implements Listener {
	// Configuration
	@ConfigVersion(1)
	public long config_version;

	// Language
	@LangVersion(1)
	public long lang_version;
	@LangString
	public String lang_command_spawn_usage;
	@LangString
	public String lang_command_spawn_description;
	@LangString
	public String lang_command_setspawn_usage;
	@LangString
	public String lang_command_setspawn_description;

	@Override
	public void on_enable() {
		// TODO autostop
		// TODO /setspawn
		register_listener(this);
	}

	@Override
	protected void on_disable() {
		unregister_listener(this);
	}

	@Override
	protected void on_config_change() {
	}
}
