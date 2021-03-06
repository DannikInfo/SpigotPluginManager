package net.Lenni0451.SpigotPluginManager.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.event.Event;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.UnknownDependencyException;
import org.bukkit.plugin.java.JavaPlugin;

public class PluginUtils {
	
	/**
	 * @return The plugin directory
	 */
	public File getPluginDir() {
		return new File(".", "plugins");
	}
	
	/**
	 * @return The current PluginManager instance
	 */
	public PluginManager getPluginManager() {
		return Bukkit.getPluginManager();
	}
	
	/**
	 * @return The current PluginLoader instance
	 */
	public PluginLoader getPluginLoader() {
		return net.Lenni0451.SpigotPluginManager.PluginManager.getInstance().getPluginLoader();
	}
	
	/**
	 * @return The array of loaded plugins
	 */
	public Plugin[] getPlugins() {
		return Bukkit.getPluginManager().getPlugins();
	}
	
	/**
	 * Get a plugin by its name.
	 * 
	 * @param name The name of the plugin you want to get
	 * @return The plugin instance of the plugin
	 * @throws IllegalArgumentException if the plugin could not be found
	 */
	public Plugin getPlugin(final String name) {
		for(Plugin plugin : this.getPlugins()) {
			if(plugin.getName().equalsIgnoreCase(name)) {
				return plugin;
			}
		}
		throw new IllegalArgumentException("The plugin does not exist");
	}
	
	/**
	 * @param name The name of the plugin you want to check if its loaded
	 * @return if the plugin is loaded
	 */
	public boolean isPluginLoaded(final String name) {
		try {
			this.getPlugin(name);
			return true;
		} catch (RuntimeException e) {}
		
		return false;
	}
	
	/**
	 * @param name The name of the plugin you want to check if its enabled
	 * @return if the plugin is enabled
	 * @throws IllegalArgumentException if the plugin could not be found
	 */
	public boolean isPluginEnabled(final String name) {
		return this.getPlugin(name).isEnabled();
	}

	/**
	 * Enable a plugin by its name
	 * 
	 * @param name The name of the plugin you want to enable
	 * @return if the plugin could be enabled (false if already enabled)
	 * @throws IllegalArgumentException if the plugin could not be found
	 */
	public boolean enablePlugin(final String name) {
		return this.enablePlugin(this.getPlugin(name));
	}
	
	/**
	 * Enable a plugin by its instance
	 * 
	 * @param plugin The plugin you want to enable
	 * @return if the plugin could be enabled (false if already enabled)
	 */
	public boolean enablePlugin(final Plugin plugin) {
		if(plugin.isEnabled()) {
			return false;
		} else {
			this.getPluginManager().enablePlugin(plugin);
//			plugin.onEnable();
			return true;
		}
	}
	
	/**
	 * Disable a plugin by its name
	 * 
	 * @param name The name of the plugin you want to disable
	 * @return if the plugin could be disabled (false if already disabled)
	 * @throws IllegalArgumentException if the plugin could not be found
	 */
	public boolean disablePlugin(final String name) {
		return this.disablePlugin(this.getPlugin(name));
	}
	
	/**
	 * Disable a plugin by its instance
	 * 
	 * @param name The plugin you want to disable
	 * @return if the plugin could be disabled (false if already disabled)
	 */
	public boolean disablePlugin(final Plugin plugin) {
		if(!plugin.isEnabled()) {
			return false;
		} else {
//			plugin.onDisable();
			this.getPluginManager().disablePlugin(plugin);
			return true;
		}
	}
	
	
	/**
	 * Reload the config of a plugin by its name
	 * 
	 * @param name The name of the plugin you want the config of to be reloaded
	 * @throws IllegalArgumentException if the plugin could not be found
	 */
	public void reloadConfig(final String name) {
		this.reloadConfig(this.getPlugin(name));
	}
	
	/**
	 * Reload the config of a plugin by its instance
	 * 
	 * @param plugin The plugin you want the config of to be reloaded
	 */
	public void reloadConfig(final Plugin plugin) {
		plugin.reloadConfig();
	}
	
	
	public Plugin reloadPlugin(final Plugin plugin) {
		this.unloadPlugin(plugin);
		return this.loadPlugin(plugin);
	}
	
	
	/**
	 * Load a plugin by its old instance
	 * 
	 * @param plugin The old plugin instance
	 * @return the plugin instance if loaded
	 */
	public Plugin loadPlugin(final Plugin plugin) {
		return this.loadPlugin(plugin.getName());
	}
	
	/**
	 * Load a plugin by its name
	 * 
	 * @param name The plugin name you want to load
	 * @return the plugin instance if loaded
	 */
	public Plugin loadPlugin(final String name) {
		File targetFile = new File(this.getPluginDir(), name + (name.toLowerCase().endsWith(".jar") ? "" : ".jar"));
		Plugin targetPlugin;
		
		if(!targetFile.exists()) {
			targetFile = null;
			
			for(File pluginFile : this.getPluginDir().listFiles()) {
				if(pluginFile.isFile() && pluginFile.isFile()) {
					if(!pluginFile.getName().toLowerCase().endsWith(".jar") && net.Lenni0451.SpigotPluginManager.PluginManager.getInstance().getConfig().getBoolean("IgnoreNonJarPlugins")) {
						continue;
					}
					try {
		                PluginDescriptionFile desc = this.getPluginLoader().getPluginDescription(pluginFile);
		                if (desc.getName().equalsIgnoreCase(name)) {
		                	targetFile = pluginFile;
		                    break;
		                }
		            } catch (InvalidDescriptionException e) {
//		            	Bukkit.getConsoleSender().sendMessage("�cThe plugin �6" + pluginFile.getName() + " �chas an invalid plugin description.");
		            }
				}
			}
		}
		if(targetFile == null) {
			throw new IllegalStateException("The plugin file could not be found");
		}
		
		try {
			targetPlugin = this.getPluginLoader().loadPlugin(targetFile);
		} catch (UnknownDependencyException e) {
			throw new IllegalStateException("The plugin could not be loaded because there is a missing dependency");
		} catch (InvalidPluginException e) {
			throw new IllegalStateException("The plugin could not be loaded because it is invalid");
		}
		
		targetPlugin.onLoad();
		this.enablePlugin(targetPlugin);
		try { //Get plugins list and add plugin if not existent
			Field f = this.getPluginManager().getClass().getDeclaredField("plugins");
			f.setAccessible(true);
			List<Plugin> plugins = (List<Plugin>) f.get(this.getPluginManager());
			if(!plugins.contains(targetPlugin)) {
				plugins.add(targetPlugin);
			}
		} catch (Throwable e) {
			throw new IllegalStateException("The plugin could not be added to the plugin list");
		}
		return targetPlugin;
	}
	
	
	/**
	 * Unload a plugin by its name
	 * 
	 * @param name The name of the plugin you want to unload
	 * @throws IllegalArgumentException if the plugin could not be found
	 */
	public void unloadPlugin(final String name) {
		this.unloadPlugin(this.getPlugin(name));
	}
	
	/**
	 * Unload a plugin by its instance
	 * 
	 * @param plugin The plugin you want to unload
	 * @throws IllegalArgumentException if the plugin could not be found
	 * @throws IllegalStateException if the plugin could not be unloaded
	 */
	public void unloadPlugin(final Plugin plugin) {
		this.disablePlugin(plugin);
		
		PluginManager pluginManager = this.getPluginManager();
		List<Plugin> plugins;
		Map<String, Plugin> lookupNames;
		SimpleCommandMap commandMap;
		Map<String, Command> knownCommands;
		Map<Event, SortedSet<RegisteredListener>> listeners;
		
		try { //Get plugins list
			Field f = pluginManager.getClass().getDeclaredField("plugins");
			f.setAccessible(true);
			plugins = (List<Plugin>) f.get(pluginManager);
		} catch (Throwable e) {
			throw new IllegalStateException("Could not unload plugin (could not load plugins list)");
		}
		try { //Get lookup names
			Field f = pluginManager.getClass().getDeclaredField("lookupNames");
			f.setAccessible(true);
			lookupNames = (Map<String, Plugin>) f.get(pluginManager);
		} catch (Throwable e) {
			throw new IllegalStateException("Could not unload plugin (could not load lookup names)");
		}
		try { //Get command map
			Field f = pluginManager.getClass().getDeclaredField("commandMap");
			f.setAccessible(true);
			commandMap = (SimpleCommandMap) f.get(pluginManager);
		} catch (Throwable e) {
			throw new IllegalStateException("Could not unload plugin (could not load command map)");
		}
		try { //Get known commands
			Field f = SimpleCommandMap.class.getDeclaredField("knownCommands");
			f.setAccessible(true);
			knownCommands = (Map<String, Command>) f.get(commandMap);
		} catch (Throwable e) {
			e.printStackTrace();
			throw new IllegalStateException("Could not unload plugin (could not load known commands)");
		}
		try {
			Field f = pluginManager.getClass().getDeclaredField("listeners");
            f.setAccessible(true);
            listeners = (Map<Event, SortedSet<RegisteredListener>>) f.get(pluginManager);
		} catch (Throwable e) {
			listeners = null;
		}
		
		plugins.remove(plugin);
		lookupNames.remove(plugin.getName());
		{ //Remove plugin commands
			Iterator<Map.Entry<String, Command>> iterator = knownCommands.entrySet().iterator();
			while(iterator.hasNext()) {
				Map.Entry<String, Command> entry = iterator.next();
				if(entry.getValue() instanceof PluginCommand) {
					PluginCommand command = (PluginCommand) entry.getValue();
					if(command.getPlugin().equals(plugin)) {
						iterator.remove();
					}
				}
			}
		}
		if(listeners != null) {
			for(Set<RegisteredListener> registeredListeners : listeners.values()) {
				Iterator<RegisteredListener> iterator = registeredListeners.iterator();
				while(iterator.hasNext()) {
					if(iterator.next().getPlugin().equals(plugin)) {
						iterator.remove();
					}
				}
			}
		}
		
		if(plugin.getClass().getClassLoader() instanceof URLClassLoader) {
			URLClassLoader classLoader = (URLClassLoader) plugin.getClass().getClassLoader();
			
			try {
				for(Field f : classLoader.getClass().getDeclaredFields()) {
					f.setAccessible(true);
					if(Modifier.isFinal(f.getModifiers())) {
						Field mf = f.getClass().getDeclaredField("modifiers");
						mf.setAccessible(true);
						mf.setInt(f, f.getModifiers() & ~Modifier.FINAL);
					}
					f.set(classLoader, null);
				}
			} catch (Throwable e) {
				throw new IllegalStateException("Could not unload plugin (remove class loader handles)", e);
			}
		}
		
		System.gc();
	}
	
	
	/**
	 * @return all plugins in the load order
	 */
	public List<Plugin> getPluginsByLoadOrder() {
		List<Plugin> plugins = new ArrayList<>();
		List<String> ignoredPlugins = net.Lenni0451.SpigotPluginManager.PluginManager.getInstance().getConfig().getStringList("IgnoredPlugins");
		
		for(Plugin plugin : this.getPlugins()) {
			if(ignoredPlugins.contains(plugin.getName())) {
				continue;
			}
			
			plugins.add(plugin);
		}
		
		for(int i = 0; i < plugins.size(); i++) {
			Plugin plugin = plugins.get(i);

			List<String> deps = new ArrayList<>();
			deps.addAll(plugin.getDescription().getDepend());
			deps.addAll(plugin.getDescription().getSoftDepend());
			{
				for(Plugin reloadPlugin : plugins) {
					if(reloadPlugin.getDescription().getLoadBefore().contains(plugin.getName())) {
						if(!deps.contains(reloadPlugin.getName()))
							deps.add(reloadPlugin.getName());
					}
				}
			}
			if(!deps.isEmpty()) {
				for(String dependName : deps) {
					try {
						Plugin depend = this.getPlugin(dependName);
						int dependPos = plugins.indexOf(depend);
						int pluginPos = plugins.indexOf(plugin);
						
						if(dependPos > pluginPos) {
							plugins.remove(pluginPos);
							plugins.add(dependPos + 1, plugin);
							
							i = 0;
						}
					} catch (Throwable e) {}
				}
			}
		}
		
		return plugins;
	}

	
	/**
	 * Get a list with all register commands and aliases from a plugin
	 * 
	 * @param plugin The plugin of which you want the commands from
	 * @return the list of commands a plugin has registered
	 */
	public List<String> getCommands(Plugin plugin) {
		List<String> commands = new ArrayList<>();
		Map<String, List<String>> aliases = new HashMap<>();
		
		Map<String, Map<String, Object>> commandMap = plugin.getDescription().getCommands();
		if(commandMap != null && !commandMap.isEmpty()) {
			for(String command : commandMap.keySet()) {
				commands.add(command.toLowerCase());
				
				if(commandMap.get(command).containsKey("aliases")) {
					Object aliasesObject = commandMap.get(command).get("aliases");
					List<String> commandAliases = new ArrayList<>();
					aliases.put(command, commandAliases);
					
					if(aliasesObject instanceof String) {
						commandAliases.add(aliasesObject.toString().toLowerCase());
					} else if(aliasesObject instanceof Collection) {
						for(Object alias : (Collection<?>) aliasesObject) {
							commandAliases.add(alias.toString().toLowerCase());
						}
					} else if(aliasesObject instanceof String[]) {
						for(String alias : (String[]) aliasesObject) {
							commandAliases.add(alias.toLowerCase());
						}
					}
				}
			}
		}
		
		Collections.sort(commands);
		for(int i = 0; i < commands.size(); i++) {
			String command = commands.get(i);
			List<String> commandAliases = aliases.get(command);
			if(commandAliases != null) {
				Collections.sort(commandAliases);
				for(int i2 = commandAliases.size() - 1; i2 >= 0; i2--) {
					String alias = commandAliases.get(i2);
					commands.add(i + 1, " " + alias);
				}
			}
		}
		
		return commands;
	}

	
	/**
	 * Get the jar file path of a plugin
	 * 
	 * @param plugin The plugin you want the jar file path of
	 * @return File the jar file path of the plugin
	 * @throws FileNotFoundException if the plugin file could not be found
	 */
	public File getPluginFile(Plugin plugin) throws FileNotFoundException {
		try {
			Method method = JavaPlugin.class.getDeclaredMethod("getFile");
			method.setAccessible(true);
			return (File) method.invoke(plugin);
		} catch (Throwable e) {
			for(File pluginFile : this.getPluginDir().listFiles()) {
				if(pluginFile.isFile() && pluginFile.isFile()) {
					if(!pluginFile.getName().toLowerCase().endsWith(".jar") && net.Lenni0451.SpigotPluginManager.PluginManager.getInstance().getConfig().getBoolean("IgnoreNonJarPlugins")) {
						continue;
					}
					
					try {
		                PluginDescriptionFile desc = this.getPluginLoader().getPluginDescription(pluginFile);
		                if (desc.getName().equalsIgnoreCase(plugin.getName())) {
		                	return pluginFile;
		                }
		            } catch (Throwable e2) {}
				}
			}
		}
		throw new FileNotFoundException();
	}
	
}
