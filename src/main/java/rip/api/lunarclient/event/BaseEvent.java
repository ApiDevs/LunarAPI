package rip.api.lunarclient.event;

import rip.api.lunarclient.LunarClientAPI;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BaseEvent extends Event {

	private static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public boolean call(LunarClientAPI lunarClient) {
		lunarClient.getServer().getPluginManager().callEvent(this);
		return this instanceof Cancellable && ((Cancellable) this).isCancelled();
	}

}
