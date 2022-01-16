package rip.api.lunarclient.module.border;

import lombok.Getter;
import lombok.Setter;
import rip.api.lunarclient.LunarClientAPI;
import rip.api.lunarclient.listener.BorderListener;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class BorderManager {
    /* borders */
    private List<Border> borderList;

    /**
     * setup border manager
     */
    public BorderManager(){
        this.borderList = new ArrayList<>();
        LunarClientAPI.getInstance().getServer().getPluginManager().registerEvents(new BorderListener(), LunarClientAPI.getInstance());
    }

    /**
     * create border object.
     *
     * @param name the name of the border.
     * @param world the world of the border.
     * @param cancelsExit setting for the border.
     * @param canShrinkExpand setting for the border.
     * @param red the red color in the border.
     * @param green the green color in the border.
     * @param blue the blue color in the border.
     * @param minLocation the minimum location of the border.
     * @param maxLocation the maximum location of th border.
     */
    public void createBorder(String name, World world, boolean cancelsExit, boolean canShrinkExpand, int red, int green, int blue, Location minLocation, Location maxLocation){
        if (this.getBorder(name) != null){
            return;
        }
        this.borderList.add(new Border(name, world, cancelsExit, canShrinkExpand, red, green, blue, minLocation, maxLocation));
    }

    /**
     * delete a border object.
     *
     * @param name the name of the border to delete.
     */
    public void deleteBorder(String name){
        if (this.getBorder(name) == null){
            return;
        }

        Border border = this.getBorder(name);

        for (Player player : Bukkit.getOnlinePlayers()){
            try {
                border.disable(player);
            } catch (IOException e) {
                //ignore
            }
        }

        this.borderList.remove(border);
    }

    /**
     * get the border object.
     * @param name the name of the object.
     * @return the border object.
     */
    public Border getBorder(String name){
        for (Border border : this.borderList){
            if (border.getName().equalsIgnoreCase(name)){
                return border;
            }
        }
        return null;
    }
}
