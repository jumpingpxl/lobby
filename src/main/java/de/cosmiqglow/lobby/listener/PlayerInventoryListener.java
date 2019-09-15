package de.cosmiqglow.lobby.listener;

import de.cosmiqglow.lobby.Lobby;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

public class PlayerInventoryListener implements Listener {

    private final Lobby plugin;

    public PlayerInventoryListener(Lobby plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventory(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;
        if (event.getSlotType().equals(InventoryType.SlotType.OUTSIDE)) return;
        if (event.getCurrentItem() == null) return;
        if (!event.getCurrentItem().hasItemMeta()) return;

        Player player = (Player) event.getWhoClicked();

        ItemStack stack = event.getCurrentItem();

        event.setCancelled(true);
        event.setResult(Event.Result.DENY);

        //Don´t remove this line of code
        player.updateInventory();

        switch (event.getView().getTitle()) {
            case "Einstellungen":
                if (stack.getItemMeta().getDisplayName().equals("§cSchließen")) {
                    player.closeInventory();
                } else {
                    plugin.getSettingsUtil().changeSettingsValue(event);
                }
            break;
            case "Minispiele":
                String locationName = ChatColor.stripColor(stack.getItemMeta().getDisplayName());
                player.teleport(plugin.getMapService().getLocation(locationName));
                break;
            case "Freunde":
                Bukkit.broadcastMessage(stack.getType().name());
                if (stack.getType().equals(Material.PLAYER_HEAD) || (stack.getType().equals(Material.SKELETON_SKULL))) {
                    String displayName = ChatColor.stripColor(stack.getItemMeta().getDisplayName());
                    player.openInventory(plugin.getInventoryUtil().loadActionInventory(displayName));
                }
                break;
        }
    }
}