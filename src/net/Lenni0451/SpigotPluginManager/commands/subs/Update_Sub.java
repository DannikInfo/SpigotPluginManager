package net.Lenni0451.SpigotPluginManager.commands.subs;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import com.google.gson.JsonObject;

import net.Lenni0451.SpigotPluginManager.PluginManager;
import net.Lenni0451.SpigotPluginManager.commands.subs.types.ISubCommand;
import net.Lenni0451.SpigotPluginManager.utils.DownloadUtils;
import net.Lenni0451.SpigotPluginManager.utils.Logger;
import net.Lenni0451.SpigotPluginManager.utils.PluginInfo;

public class Update_Sub implements ISubCommand {

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if(args.length != 1) {
			return false;
		}
		
		if(args[0].equalsIgnoreCase("*")) {
			List<Plugin> plugins = PluginManager.getInstance().getPluginUtils().getPluginsByLoadOrder();
			List<String> ignoredPlugins = PluginManager.getInstance().getConfig().getStringList("IgnoredPlugins");
			
			for(Plugin plugin : plugins) {
				if(ignoredPlugins.contains(plugin.getName())) {
					continue;
				}

				try {
					this.checkForUpdate(sender, true, plugin);
					Logger.sendPrefixMessage(sender, "�aSuccessfully updated the plugin �6" + plugin.getName() + " �ato the newest version.");
				} catch (ArrayIndexOutOfBoundsException e) {
					Logger.sendPrefixMessage(sender, "�aThe plugin �6" + plugin.getName() + " �ais already up to date.");
				} catch (IllegalArgumentException e) {
					//Do nothing
				} catch (IOException e) {
					Logger.sendPrefixMessage(sender, "�cCould not reach the spiget api or plugin �6" + plugin.getName() + " �cis not on spigotmc. Please try again later.");
				} catch (IllegalStateException e) {
					Logger.sendPrefixMessage(sender, "�cCould not save the plugin file of �6" + plugin.getName() + "�c.");
				} catch (Throwable e) {
					Logger.sendPrefixMessage(sender, "�cThe plugin �6" + plugin.getName() + " �ccould not be updated.");
				}
			}
			Logger.sendPrefixMessage(sender, "�aChecked all plugins in the config for updates.");
		} else {
			try {
				Plugin plugin = PluginManager.getInstance().getPluginUtils().getPlugin(args[0]);
				
				try {
					this.checkForUpdate(sender, false, plugin);
					Logger.sendPrefixMessage(sender, "�aSuccessfully updated the plugin to the newest version.");
				} catch (ArrayIndexOutOfBoundsException e) {
					Logger.sendPrefixMessage(sender, "�aThe plugin is already up to date.");
				} catch (IllegalArgumentException e) {
					Logger.sendPrefixMessage(sender, "�cThe plugin �6" + plugin.getName() + " �cis not in the config file.");
//					Logger.sendPrefixMessage(sender, "�aYou can add it using �6/pm addupdater <Plugin> <Plugin Id>�a."); //TODO: Add command in the future
				} catch (IOException e) {
					Logger.sendPrefixMessage(sender, "�cCould not reach the spiget api or plugin is not on spigotmc. Please try again later.");
				} catch (IllegalStateException e) {
					Logger.sendPrefixMessage(sender, "�cCould not save the plugin file.");
				} catch (Throwable e) {
					Logger.sendPrefixMessage(sender, "�cThe plugin �6" + plugin.getName() + " �ccould not be updated.");
				}
			} catch (Throwable e) {
				Logger.sendPrefixMessage(sender, "�cThe plugin could not be found.");
			}
		}
		
		return true;
	}

	@Override
	public void getTabComplete(List<String> tabs, String[] args) {
		if(args.length == 0) {
			for(Plugin plugin : PluginManager.getInstance().getPluginUtils().getPlugins()) {
				tabs.add(plugin.getName());
			}
		}
	}

	@Override
	public String getUsage() {
		return "update <Plugin>/*";
	}
	
	private void checkForUpdate(final CommandSender messageReceiver, final boolean sendPluginName, final Plugin plugin) throws IOException {
		PluginInfo info = PluginManager.getInstance().getInstalledPlugins().getPluginInfo(plugin.getName());
		if(info == null) {
			throw new IllegalArgumentException();
		}
		
		JsonObject response = DownloadUtils.getSpigotMcPluginInfo(info.getId());
		if(!response.get("version").getAsJsonObject().get("id").getAsString().equalsIgnoreCase(info.getInstalledVersion())) {
			if(sendPluginName) {
				Logger.sendPrefixMessage(messageReceiver, "�aThe plugin �6" + plugin.getName() + " �ahas an update available.");
			} else {
				Logger.sendPrefixMessage(messageReceiver, "�aThe plugin has an update available.");
			}
			try {
				DownloadUtils.downloadSpigotMcPlugin(info.getId(), new File("plugins", info.getFileName()));
				PluginManager.getInstance().getInstalledPlugins().setPlugin(info.getName(), info.getId(), response.get("version").getAsJsonObject().get("id").getAsString(), info.getFileName());
				if(PluginManager.getInstance().getConfig().getBoolean("AutoReloadUpdated")) {
					PluginManager.getInstance().getPluginUtils().reloadPlugin(plugin);
				}
			} catch (Throwable e) {
				throw new IllegalStateException();
			}
		} else {
			throw new ArrayIndexOutOfBoundsException();
		}
	}
	
}
