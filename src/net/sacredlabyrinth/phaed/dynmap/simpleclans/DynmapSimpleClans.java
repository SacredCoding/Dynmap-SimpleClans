package net.sacredlabyrinth.phaed.dynmap.simpleclans;

import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;
import net.sacredlabyrinth.phaed.simpleclans.managers.ClanManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.MarkerAPI;
import net.sacredlabyrinth.phaed.dynmap.simpleclans.layers.ClanHomes;
import net.sacredlabyrinth.phaed.dynmap.simpleclans.layers.Kills;
import net.sacredlabyrinth.phaed.dynmap.simpleclans.managers.CommandManager;
import net.sacredlabyrinth.phaed.dynmap.simpleclans.managers.PlayerManager;

import java.util.logging.Level;
import java.util.logging.Logger;

public class DynmapSimpleClans extends JavaPlugin
{
    private static DynmapSimpleClans instance;
    private static final Logger log = Logger.getLogger("Minecraft");
    private static final String LOG_PREFIX = "[Dynmap-SimpleClans] ";

    private Plugin dynmap;
    private DynmapAPI dynmapApi;
    private MarkerAPI markerApi;
    private SimpleClans simpleclans;
    private ClanManager clanManager;
    private FileConfiguration cfg;

    private PlayerManager playerManager;
    private CommandManager commandManager;
    private ClanHomes clanHomes;
    private Kills kills;
    private Toggles toggles;

    public void onEnable()
    {
        instance = this;
        info("initializing");

        playerManager = new PlayerManager();
        commandManager = new CommandManager();

        initDynmap();
        initSimpleClans();
        activate();

        getServer().getPluginManager().registerEvents(new DynmapSimpleClansListener(), this);
        getCommand("map").setExecutor(commandManager);
    }

    public void activate()
    {
        if (!dynmap.isEnabled() || !simpleclans.isEnabled())
        {
            return;
        }

        initApis();

        // load configuration

        cfg = getConfig();
        cfg.options().copyDefaults(true);
        saveConfig();

        // set up layers

        clanHomes = new ClanHomes();
        toggles = new Toggles();
        kills = new Kills();

        info("version " + this.getDescription().getVersion() + " is activated");
    }

    public void onDisable()
    {
        if (clanHomes != null)
        {
            clanHomes.cleanup();
        }
        if (toggles != null)
        {
            toggles.cleanup();
        }
        if (kills != null)
        {
            kills.cleanup();
        }
    }

    private void initDynmap()
    {
        dynmap = getServer().getPluginManager().getPlugin("dynmap");

        if (dynmap == null)
        {
            severe("Cannot find dynmap!");
            return;
        }
        dynmapApi = (DynmapAPI) dynmap;
    }

    private void initSimpleClans()
    {
        Plugin p = getServer().getPluginManager().getPlugin("SimpleClans");

        if (p == null)
        {
            severe("Cannot find SimpleClans!");
            return;
        }
        simpleclans = (SimpleClans) p;
    }

    private void initApis()
    {
        markerApi = dynmapApi.getMarkerAPI();

        if (markerApi == null)
        {
            severe("Error loading Dynmap marker API!");
            return;
        }

        // simpleclans api

        clanManager = simpleclans.getClanManager();

        if (clanManager == null)
        {
            info("SimpleClans not found - support disabled");
        }
    }

    public static void info(String msg)
    {
        log.log(Level.INFO, LOG_PREFIX + msg);
    }

    public static void severe(String msg)
    {
        log.log(Level.SEVERE, LOG_PREFIX + msg);
    }

    public static DynmapSimpleClans getInstance()
    {
        return instance;
    }

    public MarkerAPI getMarkerApi()
    {
        return markerApi;
    }

    public ClanManager getClanManager()
    {
        return clanManager;
    }

    public DynmapAPI getDynmapApi()
    {
        return dynmapApi;
    }

    public FileConfiguration getCfg()
    {
        return cfg;
    }

    public ClanHomes getClanHomes()
    {
        return clanHomes;
    }

    public Kills getKills()
    {
        return kills;
    }

    public PlayerManager getPlayerManager()
    {
        return playerManager;
    }
}
