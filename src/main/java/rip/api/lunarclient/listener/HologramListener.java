package rip.api.lunarclient.listener;

import rip.api.lunarclient.LunarClientAPI;
import rip.api.lunarclient.event.impl.AuthenticateEvent;
import rip.api.lunarclient.module.hologram.Hologram;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.IOException;

public class HologramListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAuthenticate(AuthenticateEvent event) {
        try {
            LunarClientAPI.getInstance().getHologramManager().reloadHolograms(event.getPlayer());
        } catch (IOException e) {
            //ignore
        }
    }

    @EventHandler
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        try {
            LunarClientAPI.getInstance().getHologramManager().reloadHolograms(event.getPlayer());
        } catch (IOException e) {
            //ignore
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event){
        for (Hologram hologram : LunarClientAPI.getInstance().getHologramManager().getHologramList()){
            try {
                hologram.disable(event.getPlayer());
            } catch (IOException e) {
                //ignore
            }
        }
    }

    @EventHandler
    public void onKick(PlayerKickEvent event){
        for (Hologram hologram : LunarClientAPI.getInstance().getHologramManager().getHologramList()){
            try {
                hologram.disable(event.getPlayer());
            } catch (IOException e) {
                //ignore
            }
        }
    }
}
