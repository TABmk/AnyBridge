package tab_mk.AnyBridge;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Locale;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class Main extends JavaPlugin implements Listener {

    @Override
    public void onEnable(){
      // copy default config
    	this.saveDefaultConfig();

    	int links = 0;
    	int enabled = 0;

    	ConfigurationSection sec = this.getConfig().getConfigurationSection("links");

        for (String key : sec.getKeys(false)) {
            Boolean en = this.getConfig().getBoolean("links." + key + ".enable");
            String link = this.getConfig().getString("links." + key + ".link");

            String lowerLink = link.toLowerCase(Locale.ENGLISH);

            // count all links
            links++;

            // check if link with http/s protocol
            if (lowerLink.startsWith("http://") || lowerLink.startsWith("https://")) {
                if (en.equals(true)) {
                  // count enabled links
                	enabled++;
                }

              // next loop iteration
            	continue;
            }

            // print error on wrong protocol
            System.out.println("[AnyBridge] (" + key + ") Only HTTP/S protocols are supported");

            // disable this link and save config
            this.getConfig().set("links." + key + ".enable", false);
            this.saveConfig();
        }

    	System.out.println("[AnyBridge] Started with "+ links + " links ("+enabled+" enabled)");

    	Bukkit.getPluginManager().registerEvents(this, this);
    }

	@Override
	public void onDisable() {
		System.out.println("disabled");
	}

  // handle abreload command wich reloads in-memory config
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    // check if "abreload" command used and sender has "anybridge.reload" permission
		if (cmd.getName().equalsIgnoreCase("abreload") && sender.hasPermission("anybridge.reload")) {
			this.reloadConfig();

			sender.sendMessage("[AnyBridge] reloaded");

			return true;
		}
		return false;
	}

    // handle user messages
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent e) {
    	if (this.getConfig().getBoolean("handleMessages")) {
    		send(e.getPlayer().getName(), e.getMessage());
    	}
    }

    // handle commands messages
    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent e) {
    	if (this.getConfig().getBoolean("handleCommands")) {
    		send(e.getPlayer().getName(), e.getMessage());
    	}
    }

    // check, format and send GET request
    public void send(String user, String text) {
    	ConfigurationSection sec = this.getConfig().getConfigurationSection("links");

        for (String key : sec.getKeys(false)) {
            Boolean en = this.getConfig().getBoolean("links." + key + ".enable");
            String format = this.getConfig().getString("links." + key + ".format");

            // send message to each enabled link
            if (en.equals(true)) {
                // if there is no custom format for current link, use "messageFormat"
                if (format == null || format.isEmpty()) {
                	format = this.getConfig().getString("messageFormat");
                }

              // replace variables in templates
            	String message = format
            			.replace("%USER%", user)
            			.replace("%TEXT%", text);

            	String url = this.getConfig().getString("links." + key + ".link")
            			.replace("%RND%", RND().toString())
            			.replace("%TEXT%", message);

              // send GET request
            	get(url);
            }
        }
    }

    // generate random int for %RND% placeholder (vk.com need random_id for each message)
    public static Double RND() {
    	double random = Math.random() * 999998 + 1;

    	return random;
    }

    // sending GET request
    public static String get(String url) {
        String stuff = null;
        try {
            URL link = new URL(url);
            BufferedReader in = new BufferedReader(new InputStreamReader(link.openStream()));
            String str = in.readLine();

            in.close();

            if (str != null) {
              stuff = str;
            }
        } catch (java.io.IOException e1) {
        	System.out.println("[ERROR] (" + url + ")" + e1.getMessage());
            stuff = e1.getMessage();
        }
        return stuff;
    }
}
