package de.cosmiqglow.lobby.utils;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Set;

public class ParticleUtil {

    private final Set<Player> players;
    private BukkitTask task;

    public ParticleUtil(Plugin plugin, Location location) {
        this.players = new HashSet<>();
        start(plugin, location);
    }

    public void start(Plugin plugin, Location location) {
        task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            for (Player player : players) {
                for (int degree = 0; degree < 360; degree++) {
                    double radians = Math.toRadians(degree);
                    double x = Math.cos(radians);
                    double z = Math.sin(radians);
                    location.add(x, 0, z);
                    player.getWorld().spawnParticle(Particle.REDSTONE, location.clone().add(0,2.0,0), 5, 0, 0, 0, 1,
                            new Particle.DustOptions(Color.YELLOW, 10));
                    location.subtract(x, 0, z);
                }
            }
        }, 0, 8);
    }

    public void addPlayer(Player player) {
        this.players.add(player);
    }

    public void removePlayer(Player player) {
        if (this.players.contains(player))
            this.players.add(player);
    }

    public BukkitTask getTask() {
        return task;
    }

    public Set<Player> getPlayers() {
        return players;
    }
}
