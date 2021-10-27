package io.github.pronze.sba.mock;

import io.github.pronze.sba.data.GamePlayerData;
import io.github.pronze.sba.game.*;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.api.game.GameStore;
import org.screamingsandals.lib.npc.NPC;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class MockArena implements GameWrapper {
    private final Map<UUID, MockInvisiblePlayer> invisiblePlayers = new HashMap<>();
    private final Map<UUID, GamePlayerData> playerDataMap = new HashMap<>();
    private final Game game;
    private final GameStorageImpl gameStorage;

    @Override
    public @NotNull GameStorageImpl getStorage() {
        return gameStorage;
    }

    @Override
    public @NotNull Game getGame() {
        return game;
    }

    @Override
    public Optional<GamePlayerData> getPlayerData(@NotNull UUID playerUUID) {
        return Optional.empty();
    }

    @Override
    public void registerPlayerData(@NotNull UUID uuid, @NotNull GamePlayerData data) {
        if (playerDataMap.containsKey(uuid)) {
            throw new UnsupportedOperationException("PlayerData of uuid: " + uuid.toString() + " is already registered!");
        }
        playerDataMap.put(uuid, data);
    }

    @Override
    public void unregisterPlayerData(@NotNull UUID uuid) {
        if (!playerDataMap.containsKey(uuid)) {
            throw new UnsupportedOperationException("PlayerData of uuid: " + uuid.toString() + " is not registered!");
        }
        playerDataMap.remove(uuid);
    }

    @Override
    public boolean isPlayerHidden(@NotNull Player player) {
        return invisiblePlayers.containsKey(player.getUniqueId());
    }

    @Override
    public void removeHiddenPlayer(@NotNull Player player) {
        final var invisiblePlayer = invisiblePlayers.get(player.getUniqueId());
        if (invisiblePlayer != null) {
            invisiblePlayer.showPlayer();
            invisiblePlayers.remove(player.getUniqueId());
        }
    }

    @Override
    public void addHiddenPlayer(@NotNull Player player) {
        if (invisiblePlayers.containsKey(player.getUniqueId())) return;
        final var invisiblePlayer = new MockInvisiblePlayer(player, this);
        invisiblePlayer.vanish();
        invisiblePlayers.put(player.getUniqueId(), invisiblePlayer);
    }

    @Override
    public @NotNull List<Player> getInvisiblePlayers() {
        return invisiblePlayers
                .values()
                .stream()
                .map(InvisiblePlayerImpl::getHiddenPlayer)
                .collect(Collectors.toList());
    }

    @Override
    public void createRotatingGenerator(org.screamingsandals.bedwars.api.game.@NotNull ItemSpawner itemSpawner, @NotNull Material rotationMaterial) {

    }
    @Override
    public @NotNull List<NPC> getStoreNPCS() {
        return List.of();
    }

    @Override
    public @NotNull List<NPC> getUpgradeStoreNPCS() {
        return List.of();
    }

    @Override
    public Optional<InvisiblePlayer> getHiddenPlayer(@NotNull UUID playerUUID) {
        return Optional.empty();
    }

    @Override
    public List<Player> getConnectedPlayers() {
        return null;
    }

    @Override
    public List<org.screamingsandals.bedwars.api.game.ItemSpawner> getItemSpawners() {
        return null;
    }

    @Override
    public List<GameStore> getGameStores() {
        return null;
    }

    @Override
    public World getGameWorld() {
        return null;
    }

    @Override
    public Location getSpectatorSpawn() {
        return null;
    }
}
