package net.sacredlabyrinth.phaed.dynmap.simpleclans;

import net.sacredlabyrinth.phaed.dynmap.simpleclans.layers.ClanHomes;
import net.sacredlabyrinth.phaed.dynmap.simpleclans.layers.Kills;
import net.sacredlabyrinth.phaed.dynmap.simpleclans.managers.CommandManager;
import net.sacredlabyrinth.phaed.dynmap.simpleclans.managers.PlayerManager;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;
import net.sacredlabyrinth.phaed.simpleclans.managers.ClanManager;
import net.sacredlabyrinth.phaed.simpleclans.managers.SettingsManager;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.MarkerAPI;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DynmapSimpleClans extends JavaPlugin {
	private static DynmapSimpleClans instance;
	private static final Logger log = Logger.getLogger("Minecraft");
	private static final String LOG_PREFIX = "[Dynmap-SimpleClans] ";

	private Plugin dynmap;
	private DynmapAPI dynmapApi;
	private MarkerAPI markerApi;
	private SimpleClans simpleclansCore;

	private PlayerManager playerManager;
	private ClanHomes clanHomes;
	private Kills kills;
	private Toggles toggles;

	@Override
	public void onEnable() {
		instance = this;
		info("initializing");

		playerManager = new PlayerManager();
		CommandManager commandManager = new CommandManager();

		initDynmap();
		initSimpleClans();
		activate();

		getServer().getPluginManager().registerEvents(new DynmapSimpleClansListener(), this);
		Objects.requireNonNull(getCommand("clanmap")).setExecutor(commandManager);
	}

	public void activate() {
		if (!dynmap.isEnabled() || simpleclansCore == null) {
			return;
		}

		initApis();

		reloadConfig();
		saveDefaultConfig();
		// saving images
		saveDefaultImages();

		// set up layers

		clanHomes = new ClanHomes();
		toggles = new Toggles();
		kills = new Kills();

		info("version " + this.getDescription().getVersion() + " is activated");
	}

	public void saveDefaultImages() {
		if (!new File(getDataFolder(), "/images/clanhome").exists()) {
			saveResource("images/clanhome/clanhome.png", false);
		}
		if (!new File(getDataFolder(), "/images/blood.png").exists()) {
			saveResource("images/blood.png", false);
		}
	}

	@Override
	public void onDisable() {
		cleanup();
	}

	public void cleanup() {
		if (clanHomes != null) {
			clanHomes.cleanup();
		}
		if (toggles != null) {
			toggles.cleanup();
		}
		if (kills != null) {
			kills.cleanup();
		}
	}

	private void initDynmap() {
		dynmap = getServer().getPluginManager().getPlugin("dynmap");

		if (dynmap == null) {
			severe("Cannot find dynmap!");
			return;
		}
		dynmapApi = (DynmapAPI) dynmap;
	}

	private void initSimpleClans() {
		Plugin p = getServer().getPluginManager().getPlugin("SimpleClans");

		if (p != null) {
			simpleclansCore = (SimpleClans) p;
			return;
		}

		severe("Cannot find SimpleClans!");
    }

    private void initApis() {
        markerApi = dynmapApi.getMarkerAPI();

        if (markerApi == null) {
            severe("Error loading Dynmap marker API!");
        }
    }

    public static void info(@NotNull String msg) {
        log.log(Level.INFO, LOG_PREFIX + msg);
    }

    public static void severe(@NotNull String msg) {
        log.log(Level.SEVERE, LOG_PREFIX + msg);
    }

    public static DynmapSimpleClans getInstance() {
        return instance;
    }

    public MarkerAPI getMarkerApi() {
        return markerApi;
    }

    public ClanManager getClanManager() {
		return simpleclansCore.getClanManager();
	}

	public DynmapAPI getDynmapApi() {
		return dynmapApi;
    }

    public ClanHomes getClanHomes() {
        return clanHomes;
    }

    public Kills getKills() {
        return kills;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public SettingsManager getSettingsManager() {
        return simpleclansCore.getSettingsManager();
    }

    /**
     * @param messageKey The key
     * @return Translated colored message from key
     */
    public String getLang(@NotNull String messageKey) {
        String msg = getConfig().getString("language." + messageKey);
        return msg == null ?
                String.format("Missing language for %s key", messageKey) :
                ChatColor.translateAlternateColorCodes('&', msg);
    }
}
