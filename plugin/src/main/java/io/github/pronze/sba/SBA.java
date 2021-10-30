package io.github.pronze.sba;

import io.github.pronze.sba.commands.CommandManager;
import io.github.pronze.sba.config.Configurator;
import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.game.GameStorage;
import io.github.pronze.sba.game.GameWrapper;
import io.github.pronze.sba.game.GameWrapperManagerImpl;
import io.github.pronze.sba.game.tasks.GameTaskManagerImpl;
import io.github.pronze.sba.inventories.GamesInventory;
import io.github.pronze.sba.inventories.SBAStoreInventory;
import io.github.pronze.sba.inventories.SBAUpgradeStoreInventory;
import io.github.pronze.sba.lib.lang.SBALanguageService;
import io.github.pronze.sba.listener.*;
import io.github.pronze.sba.manager.GameWrapperManager;
import io.github.pronze.sba.manager.PartyManager;
import io.github.pronze.sba.party.PartyManagerImpl;
import io.github.pronze.sba.placeholderapi.SBAExpansion;
import io.github.pronze.sba.service.*;
import io.github.pronze.sba.specials.listener.BridgeEggListener;
import io.github.pronze.sba.specials.listener.PopupTowerListener;
import io.github.pronze.sba.utils.DateUtils;
import io.github.pronze.sba.utils.FirstStartConfigReplacer;
import io.github.pronze.sba.utils.Logger;
import io.github.pronze.sba.visuals.LobbyScoreboardManager;
import io.github.pronze.sba.visuals.MainLobbyVisualsManager;
import io.github.pronze.sba.wrapper.BedWarsAPIWrapper;
import io.github.pronze.sba.wrapper.SBAPlayerWrapper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.BedwarsAPI;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.lib.sgui.listeners.InventoryListener;
import org.screamingsandals.lib.event.EventManager;
import org.screamingsandals.lib.healthindicator.HealthIndicatorManager;
import org.screamingsandals.lib.hologram.HologramManager;
import org.screamingsandals.lib.npc.NPCManager;
import org.screamingsandals.lib.packet.PacketMapper;
import org.screamingsandals.lib.plugin.PluginContainer;
import org.screamingsandals.lib.sidebar.SidebarManager;
import org.screamingsandals.lib.tasker.Tasker;
import org.screamingsandals.lib.utils.PlatformType;
import org.screamingsandals.lib.utils.annotations.Init;
import org.screamingsandals.lib.utils.annotations.Plugin;
import org.screamingsandals.lib.utils.annotations.PluginDependencies;
import org.screamingsandals.simpleinventories.SimpleInventoriesCore;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.github.pronze.sba.utils.MessageUtils.showErrorMessage;

@Plugin(
        id = "SBA",
        authors = {"pronze"},
        loadTime = Plugin.LoadTime.POSTWORLD,
        version = "1.5.6-SNAPSHOT"
)
@PluginDependencies(platform = PlatformType.BUKKIT, dependencies = {
        "BedWars"
}, softDependencies =
        "PlaceholderAPI"
)
@Init(services = {
        Tasker.class,
        PacketMapper.class,
        HologramManager.class,
        SidebarManager.class,
        HealthIndicatorManager.class,
        SimpleInventoriesCore.class,
        NPCManager.class,
        UpdateChecker.class,
        Logger.class,
        SBAConfig.class,
        SBALanguageService.class,
        CommandManager.class,
        GameWrapperManagerImpl.class,
        PartyManagerImpl.class,
        GameTaskManagerImpl.class,
        SBAStoreInventory.class,
        SBAUpgradeStoreInventory.class,
        GamesInventory.class,
        PlayerWrapperService.class,
        GamesInventoryService.class,
        HealthIndicatorService.class,
        PacketListener.class,
        DateUtils.class,
        BedWarsListener.class,
        GameChatListener.class,
        PartyListener.class,
        PlayerListener.class,
        GeneratorSplitterListener.class,
        ExplosionVelocityControlListener.class,
        LobbyScoreboardManager.class,
        MainLobbyVisualsManager.class,
        DynamicSpawnerLimiterService.class,
        BedwarsCustomMessageModifierListener.class,
        BridgeEggListener.class,
        PopupTowerListener.class,
        NPCStoreService.class,
        FirstStartConfigReplacer.class,
        BedWarsAPIWrapper.class
})
public class SBA extends PluginContainer implements AddonAPI {

    private static SBA instance;

    public static SBA getInstance() {
        return instance;
    }

    private JavaPlugin cachedPluginInstance;
    private final List<Listener> registeredListeners = new ArrayList<>();

    public static JavaPlugin getPluginInstance() {
        if (instance == null) {
            throw new UnsupportedOperationException("SBA has not yet been initialized!");
        }
        if (instance.cachedPluginInstance == null) {
            instance.cachedPluginInstance = instance.getPluginDescription().as(JavaPlugin.class);
        }
        return instance.cachedPluginInstance;
    }

    @Override
    public void enable() {
        instance = this;
        cachedPluginInstance = instance.getPluginDescription().as(JavaPlugin.class);
    }

    @Override
    public void postEnable() {
        if (Bukkit.getServer().getServicesManager().getRegistration(BedwarsAPI.class) == null) {
            showErrorMessage("Could not find Screaming-BedWars plugin!, make sure " +
                    "you have the right one installed, and it's enabled properly!");
            return;
        }

        if (Main.getVersionNumber() < 109) {
            showErrorMessage("Minecraft server is running versions below 1.9.4, please upgrade!");
            return;
        }

        // init SLib v1 InventoryListener to delegate old SLib actions to new one.
        InventoryListener.init(cachedPluginInstance);

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            Logger.trace("Registering SBAExpansion...");
            new SBAExpansion().register();
        }

        getLogger().info("Plugin has finished loading!");
        registerAPI();
    }

    @Override
    public void disable() {
        EventManager.getDefaultEventManager().unregisterAll();
        EventManager.getDefaultEventManager().destroy();
        Bukkit.getServer().getServicesManager().unregisterAll(getPluginInstance());
    }

    @Override
    public void registerListener(@NotNull Listener listener) {
        if (registeredListeners.contains(listener)) {
            return;
        }
        Bukkit.getServer().getPluginManager().registerEvents(listener, getPluginInstance());
        Logger.trace("Registered listener: {}", listener.getClass().getSimpleName());
    }

    @Override
    public SBAPlayerWrapper wrapPlayer(Player player) {
        return PlayerWrapperService.wrapPlayer(player);
    }

    @Override
    public void unregisterListener(@NotNull Listener listener) {
        if (!registeredListeners.contains(listener)) {
            return;
        }
        HandlerList.unregisterAll(listener);
        registeredListeners.remove(listener);
        Logger.trace("Unregistered listener: {}", listener.getClass().getSimpleName());
    }

    public List<Listener> getRegisteredListeners() {
        return List.copyOf(registeredListeners);
    }

    private void registerAPI() {
        Bukkit.getServer().getServicesManager().register(AddonAPI.class, this, cachedPluginInstance, ServicePriority.Normal);
        Logger.trace("API has been registered!");
    }

    @Override
    public boolean isDebug() {
        return SBAConfig.getInstance().getBoolean("debug.enabled", false);
    }

    @Override
    public boolean isSnapshot() {
        return getVersion().contains("SNAPSHOT");
    }

    @Override
    public String getVersion() {
        return getPluginDescription().getVersion();
    }

    @Override
    public GameWrapperManager getGameWrapperManager() {
        return GameWrapperManagerImpl.getInstance();
    }

    @Override
    public PartyManager getPartyManager() {
        return PartyManagerImpl.getInstance();
    }

    @Override
    public Configurator getConfigurator() {
        return SBAConfig.getInstance();
    }

    @Override
    public boolean isPendingUpgrade() {
        return !getVersion().equalsIgnoreCase(SBAConfig.getInstance().node("version").getString());
    }

    @Override
    public JavaPlugin getJavaPlugin() {
        return cachedPluginInstance;
    }
}