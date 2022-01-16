package rip.api.lunarclient;

import lombok.Getter;
import lombok.Setter;
import rip.api.lunarclient.command.EmoteCommand;
import rip.api.lunarclient.command.LunarClientCommand;
import rip.api.lunarclient.event.impl.AuthenticateEvent;
import rip.api.lunarclient.listener.ClientListener;
import rip.api.lunarclient.listener.PlayerListener;
import rip.api.lunarclient.module.border.BorderManager;
import rip.api.lunarclient.module.hologram.HologramManager;
import rip.api.lunarclient.module.waypoint.WaypointManager;
import rip.api.lunarclient.user.User;
import rip.api.lunarclient.user.UserManager;
import rip.api.lunarclient.util.type.Notification;
import rip.api.lunarclient.util.type.ServerRule;
import rip.api.lunarclient.util.type.StaffModule;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static rip.api.lunarclient.util.BufferUtils.*;

@Getter
@Setter
public class LunarClientAPI extends JavaPlugin {

    @Getter private static LunarClientAPI instance;

    /* Managers */
    private UserManager userManager;
    private HologramManager hologramManager;
    private WaypointManager waypointManager;
    private BorderManager borderManager;

    @Override
    public void onEnable() {
        // Set instance
        instance = this;

        // Construct manager classes
        this.userManager = new UserManager();
        this.hologramManager = new HologramManager();
        this.waypointManager = new WaypointManager();
        this.borderManager = new BorderManager();

        // Setup configuration
        this.getConfig().options().copyDefaults(true);
        this.saveConfig();

        // Add our channel listeners
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "Lunar-Client");
        this.getServer().getMessenger().registerIncomingPluginChannel(this, "Lunar-Client", (channel, player, bytes) -> {
            if (bytes[0] == 26) {
                User user = LunarClientAPI.getInstance().getUserManager().getPlayerData(player);
                if (user != null && !user.isLunarClient()){
                    user.setLunarClient(true);
                    new AuthenticateEvent(player).call(LunarClientAPI.getInstance());

                    try {
                        LunarClientAPI.getInstance().sendNotification(player, "You have been Lunar Client authenticated", Notification.INFO, 3);
                        LunarClientAPI.getInstance().sendTitle(player, true, ChatColor.GOLD + "Welcome to HYS", 1F, 6, 3, 3);
                    } catch (IOException e) {
                        //ignore
                    }
                }

                for (Player other : Bukkit.getOnlinePlayers()) {
                    if (LunarClientAPI.getInstance().isAuthenticated(other)) {
                        other.sendPluginMessage(this, channel, bytes);
                    }
                }
            }
        });

        // Register listeners
        this.getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        this.getServer().getPluginManager().registerEvents(new ClientListener(), this);

        // Register commands
        this.getCommand("lunarclient").setExecutor(new LunarClientCommand());
        this.getCommand("emote").setExecutor(new EmoteCommand());
    }

    /**
     * Send a cooldown packet to the player.
     *
     * @param player the player instance.
     * @param name the name of the instance.
     * @param material the material instance.
     * @param seconds the seconds the cooldown goes up for.
     * @throws IOException the exception called when something fucks up.
     */
    public void sendCooldown(Player player, String name, Material material, int seconds) throws IOException {
        if (!this.isAuthenticated(player)){
            return;
        }

        ByteArrayOutputStream os = new ByteArrayOutputStream();

        os.write(3);
        os.write(writeString(name));
        os.write(writeLong(TimeUnit.SECONDS.toMillis(seconds)));
        os.write(writeInt(material.getId()));

        os.close();

        player.sendPluginMessage(LunarClientAPI.getInstance(), "Lunar-Client", os.toByteArray());
    }

    /**
     * Send a cooldown packet to the player.
     *
     * @param player the player instance.
     * @param name the name of the instance.
     * @param material the material instance.
     * @param mill the seconds the cooldown goes up for.
     * @throws IOException the exception called when something fucks up.
     */
    public void sendCooldown(Player player, String name, Material material, long mill) throws IOException {
        if (!this.isAuthenticated(player)){
            return;
        }

        ByteArrayOutputStream os = new ByteArrayOutputStream();

        os.write(3);
        os.write(writeString(name));
        os.write(writeLong(mill));
        os.write(writeInt(material.getId()));

        os.close();

        player.sendPluginMessage(LunarClientAPI.getInstance(), "Lunar-Client", os.toByteArray());
    }

    /**
     * send a title packet to the player.
     *
     * @param player the player to send packet to.
     * @param message the message on the title.
     * @param size the size of the title.
     * @param duration the duration of the title.
     * @param fadeIn the fadeIn duration of the title.
     * @param fadeOut the fadeOut duration of the title.
     */
    public void sendTitle(Player player, boolean subTitle, String message, float size, int duration, int fadeIn, int fadeOut) throws IOException {
        if (!this.isAuthenticated(player)){
            return;
        }

        ByteArrayOutputStream os = new ByteArrayOutputStream();

        os.write(14);

        os.write(writeString(subTitle ? "subtitle" : "normal"));
        os.write(writeString(message));
        os.write(writeFloat(size));
        os.write(writeLong(TimeUnit.SECONDS.toMillis(duration)));
        os.write(writeLong(TimeUnit.SECONDS.toMillis(fadeIn)));
        os.write(writeLong(TimeUnit.SECONDS.toMillis(fadeOut)));

        os.close();

        player.sendPluginMessage(LunarClientAPI.getInstance(), "Lunar-Client", os.toByteArray());
    }

    /**
     * Make the player perform an emote.
     *
     * @param player the player which will be performing the emote.
     * @param type the emote
     * @param everyone if the packet should be sent to everyone.
     * @throws IOException exception.
     */
    public void performEmote(Player player, int type, boolean everyone) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        os.write(26);
        os.write(getBytesFromUUID(player.getUniqueId()));
        os.write(writeInt(type));

        os.close();

        if (everyone) {
            for (Player other : Bukkit.getOnlinePlayers()) {
                if (LunarClientAPI.getInstance().isAuthenticated(other)) {
                    other.sendPluginMessage(LunarClientAPI.getInstance(), "Lunar-Client", os.toByteArray());
                }
            }
        } else {
            player.sendPluginMessage(LunarClientAPI.getInstance(), "Lunar-Client", os.toByteArray());
        }
    }

    /**
     * send a notification to the player.
     *
     * @param player the player instance.
     * @param message the message to send.
     * @param level the level to send.
     * @param delay the delay to send the message for.
     * @throws IOException the exception it sends to the server.
     */
    public void sendNotification(Player player, String message, Notification level, int delay) throws IOException {
        if (!this.isAuthenticated(player)){
            return;
        }

        ByteArrayOutputStream os = new ByteArrayOutputStream();

        os.write(9);
        os.write(writeString(message));
        os.write(writeLong(TimeUnit.SECONDS.toMillis(delay)));
        os.write(writeString(level.name()));

        os.close();

        player.sendPluginMessage(LunarClientAPI.getInstance(), "Lunar-Client", os.toByteArray());
    }

    /**
     * enable or disable a staff modules
     *
     * @param player the player instance.
     * @param module the modules name.
     * @param enabled if the module should be enabled or disabled.
     * @throws IOException the exception called if something goes wrong.
     */
    public void toggleStaffModule(Player player, StaffModule module, boolean enabled) throws IOException {
        if (!this.isAuthenticated(player)){
            return;
        }

        ByteArrayOutputStream os = new ByteArrayOutputStream();

        os.write(12);
        os.write(writeString(module.name()));
        os.write(writeBoolean(enabled));

        os.close();

        player.sendPluginMessage(this, "Lunar-Client", os.toByteArray());
    }

    /**
     * update a server rule for the player
     *
     * @param player the player to update server rule for.
     * @param rule the rule to update for the player.
     * @param b the boolean value of the rule.
     * @param i the integer value of the rule.
     * @param f the float value of the rule.
     * @param s the string value of the rule.
     */
    public void updateServerRule(Player player, ServerRule rule, boolean b, int i, float f, String s) throws IOException {
        if (!this.isAuthenticated(player)){
            return;
        }

        ByteArrayOutputStream os = new ByteArrayOutputStream();

        os.write(10);
        os.write(writeString(rule.getName()));
        os.write(writeBoolean(b));
        os.write(writeInt(i));
        os.write(writeFloat(f));
        os.write(writeString(s));

        os.close();

        player.sendPluginMessage(this, "Lunar-Client", os.toByteArray());
    }

    /**
     * update the name tag of a player.
     *
     * @param player the player to send the update to.
     * @param target the player to change name tag.
     * @param tags the tags to add for the player.
     */
    public void updateNameTag(Player player, Player target, String... tags) throws IOException {
        if (!this.isAuthenticated(player)){
            return;
        }

        ByteArrayOutputStream os = new ByteArrayOutputStream();

        os.write(7);
        os.write(getBytesFromUUID(target.getUniqueId()));

        os.write(writeBoolean(true));
        os.write(writeVarInt(tags.length));
        for (String tag : tags){
            os.write(writeString(tag));
        }

        os.close();

        player.sendPluginMessage(this, "Lunar-Client", os.toByteArray());
    }

    /**
     * send the teammate packet to the two players.
     *
     * @param player the player that is gonna see his team mates.
     * @param targets the targets the player should be teamed with.
     */
    public void sendTeamMate(Player player, Player... targets) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        os.write(13);

        os.write(writeBoolean(true));
        os.write(getBytesFromUUID(player.getUniqueId()));
        os.write(writeLong(10L));

        Map<UUID, Map<String, Double>> playerMap = new HashMap<>();


        for (Player member : targets) {
            Map<String, Double> posMap = new HashMap<>();

            posMap.put("x", member.getLocation().getX());
            posMap.put("y", member.getLocation().getY());
            posMap.put("z", member.getLocation().getZ());

            playerMap.put(member.getUniqueId(), posMap);
        }

        Map<String, Double> posMap = new HashMap<>();

        posMap.put("x", player.getLocation().getX());
        posMap.put("y", player.getLocation().getY());
        posMap.put("z", player.getLocation().getZ());

        playerMap.put(player.getUniqueId(), posMap);

        os.write(writeVarInt(playerMap.size()));

        for (Map.Entry<UUID, Map<String, Double>> entry : playerMap.entrySet()) {
            os.write(getBytesFromUUID(entry.getKey()));
            os.write(writeVarInt(entry.getValue().size()));
            for (Map.Entry<String, Double> posEntry : entry.getValue().entrySet()) {
                os.write(writeString(posEntry.getKey()));
                os.write(writeDouble(posEntry.getValue()));
            }
        }

        os.close();

        player.sendPluginMessage(this, "Lunar-Client", os.toByteArray());
    }

    /**
     * Check if a player is playing on Lunar Client.
     *
     * @param player the player.
     * @return if they are on Lunar Client.
     */
    public boolean isAuthenticated(Player player) {
        User user = this.userManager.getPlayerData(player);

        if (user == null){
            return false;
        }

        return user.isLunarClient();
    }
}