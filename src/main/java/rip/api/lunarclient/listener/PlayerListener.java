package rip.api.lunarclient.listener;

import rip.api.lunarclient.LunarClientAPI;
import rip.api.lunarclient.user.User;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        User data = new User(event.getPlayer().getUniqueId(), event.getPlayer().getName());
        LunarClientAPI.getInstance().getUserManager().setPlayerData(event.getPlayer().getUniqueId(), data);

        LunarClientAPI.getInstance().getServer().getScheduler().runTaskLater(LunarClientAPI.getInstance(), () -> {
            User user = LunarClientAPI.getInstance().getUserManager().getPlayerData(event.getPlayer());
            if (user != null) {
                if (user.isLunarClient()) {
                    if (LunarClientAPI.getInstance().getConfig().getBoolean("player.authenticate-message.enable")) {
                        event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', LunarClientAPI.getInstance().getConfig().getString("player.authenticate-message.message")));
                    }
                } else {
                    if (LunarClientAPI.getInstance().getConfig().getBoolean("kick-player.enabled")) {
                        event.getPlayer().kickPlayer(ChatColor.translateAlternateColorCodes('&', LunarClientAPI.getInstance().getConfig().getString("kick-player.message").replaceAll("%SPACER%", "\n")));
                    }
                }
            } else {
                event.getPlayer().kickPlayer(ChatColor.RED + "An error occurred while loading your data.");
            }
        }, 40L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        LunarClientAPI.getInstance().getUserManager().removePlayerData(event.getPlayer().getUniqueId());
    }
}