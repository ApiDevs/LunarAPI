package rip.api.lunarclient.event.impl;

import rip.api.lunarclient.event.PlayerEvent;
import org.bukkit.entity.Player;

public class AuthenticateEvent extends PlayerEvent {

    public AuthenticateEvent(Player player) {
        super(player);
    }

}
