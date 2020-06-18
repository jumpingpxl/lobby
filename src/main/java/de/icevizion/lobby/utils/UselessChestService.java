package de.icevizion.lobby.utils;

import com.mongodb.client.MongoCollection;
import de.icevizion.aves.util.LocationUtil;
import de.icevizion.lobby.Lobby;
import io.sentry.Sentry;
import net.titan.spigot.Cloud;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Patrick Zdarsky / Rxcki
 * @version 1.0
 * @since 17/04/2020 17:21
 */

public class UselessChestService implements Listener {

    private final Lobby lobby;
    private final MongoCollection<Document> dataCollection;
    private ReentrantLock lock;

    private Chunk chunk;
    private long count;
    private ArmorStand armorStand;
    private ArmorStand textStand;

    public UselessChestService(Lobby lobby) {
        this.lobby = lobby;

        lock = new ReentrantLock();
        dataCollection = Cloud.getInstance().getCloudMongo().getCollection("data");

        Location location = lobby.getMapService().getLobbyMap().get().getUselessChest();
        chunk = location.getChunk();

        location.getWorld().loadChunk(chunk);

        if (location != null) {


            armorStand = location.getWorld().spawn(location.clone().add(0.5, -0.75, 0.5), ArmorStand.class);
            armorStand.setFireTicks(0);
            armorStand.setGravity(false);
            armorStand.setCustomName("Test");
            armorStand.setCustomNameVisible(true);
            armorStand.setVisible(false);

            textStand = location.getWorld().spawn(location.clone().add(0.5, -0.4, 0.5), ArmorStand.class);
            textStand.setFireTicks(0);
            textStand.setGravity(false);
            textStand.setVisible(false);
            textStand.setCustomName("§6Sinnlose Kiste");
            textStand.setCustomNameVisible(true);

            loadFromDatabase();
            startScheduler();

            Bukkit.getPluginManager().registerEvents(this, lobby);
        } else {
            Sentry.capture("The given location for the useless chest is null");
        }
    }

    // =======

    public void despawn() {
        armorStand.remove();
        textStand.remove();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock().getType() != Material.CHEST) {
            return;
        }

        //Code to check if the chest is the useless chest
        if (LocationUtil.compare(event.getClickedBlock().getLocation(),
                lobby.getMapService().getLobbyMap().get().getUselessChest(), false)) {
            //It is our chest, yay
            event.setCancelled(false);
            //Perhaps call this async since it is being synchronized?
            increase();
        }
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        if (event.getChunk().equals(chunk))
            event.setCancelled(true);
    }

    // =======

    private void startScheduler() {
        Bukkit.getScheduler().runTaskTimer(lobby, () -> {
            loadFromDatabase();
        }, 20, 40);
    }

    private void loadFromDatabase() {
        lock.lock();
        Document document = dataCollection.find(new Document("name", "UselessChest")).first();
        if (document == null) {
            document = new Document("name", "UselessChest");
            document.append("count", 0L);
            dataCollection.insertOne(document);
        }

        count = document.getLong("count");
        displayCurrentCount();

        try {
            Cloud.getInstance().getCloudMongo().getCollection("data");
        } finally {
            lock.unlock();
        }
    }

    private void displayCurrentCount() {
        //Update hologram here
        if (armorStand == null)
            return;

        armorStand.setCustomName("§3" + count + "§7x geöffnet!");
    }

    private void increase() {
        lock.lock();
        try {
            count++;
            dataCollection.updateOne(new Document("name", "UselessChest"),
                    new Document("$inc", new Document("count", 1)));
            displayCurrentCount();
        } finally {
            lock.unlock();
        }
    }
}
