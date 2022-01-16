package rip.api.lunarclient.listener;

import rip.api.lunarclient.LunarClientAPI;
import rip.api.lunarclient.event.impl.AuthenticateEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public class WaypointListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAuthenticate(AuthenticateEvent event) {
        LunarClientAPI.getInstance().getWaypointManager().reloadWaypoints(event.getPlayer(), true);
    }

    @EventHandler
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        LunarClientAPI.getInstance().getWaypointManager().reloadWaypoints(event.getPlayer(), true);
    }
}
