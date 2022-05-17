package io.github.pronze.sba.utils.citizens;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.event.SpawnReason;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import io.github.pronze.sba.SBA;

import java.util.UUID;

public class RespawnTrait extends Trait {

    // <--[language]
    // @name Health Trait
    // @group NPC Traits
    // @description
    // By default, NPCs are invulnerable, unable to be damaged. If you want your NPC
    // to be able to take damage, or use the left click as an interaction, it must
    // have the health trait. The Health trait is automatically enabled if you set
    // the damage trigger state to true.
    //
    // You can use the denizen vulnerable command to make your NPCs react to left
    // click, but not take damage. - vulnerable state:false
    //
    // Enable Damage trigger via dScript: - trigger name:damage state:true
    // Enable Health trait via dScript: - trait state:true health
    // Enable Health trait via npc command: /npc health --set # (-r)
    //
    // Enable automatic respawn (default delay 300t): /npc health --respawndelay [delay as a duration]
    // Set respawn location: - flag <npc> respawn_location:<location>
    //
    // Related Tags
    // <@link tag EntityTag.health>
    // <@link tag EntityTag.formatted_health>
    // <@link tag EntityTag.health_max>
    // <@link tag EntityTag.health_percentage>
    // <@link tag NPCTag.has_trait[health]>
    //
    // Related Mechanisms
    // <@link mechanism EntityTag.health>
    // <@link mechanism EntityTag.max_health>
    //
    // Related Commands
    // <@link command heal>
    // <@link command health>
    // <@link command vulnerable>
    //
    // Related Actions
    // <@link action on damage>
    // <@link action on damaged>
    // <@link action on no damage trigger>
    //
    // -->

    // Saved to the C2 saves.yml
    @Persist("animatedeath")
    private boolean animatedeath = true;

    @Persist("respawnondeath")
    private boolean respawn = true;

    @Persist("respawndelayinseconds")
    private String respawnDelay = "0";

    @Persist("respawnlocation")
    private String respawnLocation = "<npc.flag[respawn_location].if_null[<npc.location>]>";

    @Persist("blockdrops")
    private boolean blockDrops = true;

    // internal
    private boolean dying = false;
    private Location loc;
    private UUID entityId = null;

    public void setRespawnLocation(String string) {
        respawnLocation = string;
    }

    public void setRespawnDelay(int seconds) {
        respawnDelay = String.valueOf(seconds);
    }

    public void setRespawnDelay(String string) {
        respawnDelay = string;
    }

    public String getRespawnLocationAsString() {
        return respawnLocation;
    }

    public Location getRespawnLocation() {
        return npc.getStoredLocation();
    }

    public void setRespawnable(boolean respawnable) {
        respawn = respawnable;
    }

    public boolean isRespawnable() {
        return respawn;
    }

    public void animateOnDeath(boolean animate) {
        animatedeath = animate;
    }

    public boolean animatesOnDeath() {
        return animatedeath;
    }

    public Integer void_watcher_task = null;

    /**
     * Listens for spawn of an NPC and updates its health with the max health
     * information for this trait.
     */
    @Override
    public void onSpawn() {
        dying = false;
        setHealth();

        void_watcher_task = Bukkit.getScheduler().scheduleSyncRepeatingTask(SBA.getPluginInstance(), () -> {
            if (!npc.isSpawned()) {
                Bukkit.getScheduler().cancelTask(void_watcher_task);
                return;
            }
            if (npc.getStoredLocation().getY() < -1000) {
                npc.despawn(DespawnReason.DEATH);
                if (respawn) {
                    Location res = getRespawnLocation();
                    if (res.getY() < 1) {
                        res.setY(res.getWorld().getHighestBlockYAt(res.getBlockX(), res.getBlockZ()));
                    }
                    if (npc.isSpawned()) {
                        npc.getEntity().teleport(res);
                    }
                    else {
                        npc.spawn(res);
                    }
                }
            }
        }, 200, 200);

    }

    public RespawnTrait() {
        super("RespawnTrait");
    }

    /**
     * Gets the current health of this NPC.
     *
     * @return current health points
     */
    public double getHealth() {
        if (!npc.isSpawned()) {
            return 0;
        }
        else {
            return ((LivingEntity) npc.getEntity()).getHealth();
        }
    }

    /**
     * Sets the maximum health for this NPC. Default max is 20.
     *
     * @param newMax new maximum health
     */
    public void setMaxhealth(int newMax) {
        ((LivingEntity) npc.getEntity()).setMaxHealth(newMax);
    }

    /**
     * Gets the maximum health for this NPC.
     *
     * @return maximum health
     */
    public double getMaxhealth() {
        return ((LivingEntity) npc.getEntity()).getMaxHealth();
    }

    /**
     * Heals the NPC.
     *
     * @param health number of health points to heal
     */
    public void heal(int health) {
        setHealth(getHealth() + health);
    }

    /**
     * Sets the NPCs health to maximum.
     */
    public void setHealth() {
        setHealth(((LivingEntity) npc.getEntity()).getMaxHealth());
    }

    /**
     * Sets the NPCs health to a specific amount.
     *
     * @param health total health points
     */
    public void setHealth(double health) {
        if (npc.getEntity() != null) {
            ((LivingEntity) npc.getEntity()).setHealth(health);
        }
    }

    public void die() {
        ((LivingEntity) npc.getEntity()).damage(((LivingEntity) npc.getEntity()).getHealth());
    }

    // Listen for deaths to clear drops
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDeath(EntityDeathEvent event) {

        if (entityId == null || !event.getEntity().getUniqueId().equals(entityId)) {
            return;
        }

        if (blockDrops) {
            event.getDrops().clear();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        // Check if the event pertains to this NPC
        if (event.getEntity() != npc.getEntity() || dying) {
            return;
        }

        // Make sure this is a killing blow
        if (this.getHealth() - event.getFinalDamage() > 0) {
            return;
        }

        dying = true;

        // Save entityId for EntityDeath event
        entityId = npc.getEntity().getUniqueId();

        if (npc.getEntity() == null) {
            return;
        }

        loc = getRespawnLocation();// TODO: debug option?

        if (loc == null) {
            loc = npc.getStoredLocation();
        }

        if (animatedeath) {
            // Cancel navigation to keep the NPC from damaging players
            // while the death animation is being carried out.
            npc.getNavigator().cancelNavigation();
            // Reset health now to avoid the death from happening instantly
            //setHealth();
            // Play animation (TODO)
            // playDeathAnimation(npc.getEntity());

        }

        //die();

        npc.despawn(DespawnReason.DEATH);
        if (respawn) {
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(SBA.getPluginInstance(),
                    () -> {
                        if (npc.isSpawned()) {
                            return;
                        }
                    else {
                            npc.spawn(loc, SpawnReason.RESPAWN);
                        }
                    }, (1));
        }

    }
}