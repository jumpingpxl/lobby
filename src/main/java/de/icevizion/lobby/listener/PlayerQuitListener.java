package de.icevizion.lobby.listener;

import de.icevizion.lobby.profile.ProfileCache;
import net.titan.spigot.Cloud;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    private final ProfileCache profileCache;

    public PlayerQuitListener(ProfileCache profileCache) {
        this.profileCache = profileCache;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);
        profileCache.removeProfile(event.getPlayer());

        Cloud.getInstance().getPlayer(event.getPlayer()).extradataSet("location",
                event.getPlayer().getLocation().serialize());
    }
}