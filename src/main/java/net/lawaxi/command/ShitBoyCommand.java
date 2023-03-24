package net.lawaxi.command;

import net.lawaxi.Shitboy;
import net.lawaxi.util.Pocket48Handler;
import net.mamoe.mirai.console.command.CommandOwner;
import net.mamoe.mirai.console.command.CommandSender;
import net.mamoe.mirai.console.command.java.JCompositeCommand;
import org.jetbrains.annotations.NotNull;

public class ShitBoyCommand extends JCompositeCommand {

    public ShitBoyCommand() {
        this(Shitboy.INSTANCE,"shitboy","开关");
    }
    public ShitBoyCommand(@NotNull CommandOwner owner, @NotNull String primaryName, @NotNull String... secondaryNames) {
        super(owner, primaryName, secondaryNames);
    }

    @SubCommand({"on"})
    public void on(CommandSender sender) {
        Shitboy.INSTANCE.getConfig().swch(true);
    }

    @SubCommand({"off"})
    public void off(CommandSender sender) {
        Shitboy.INSTANCE.getConfig().swch(false);
    }

    @SubCommand({"getRoomID"})
    public void getRoomID(CommandSender sender, int starID) {
        Pocket48Handler handler = Shitboy.INSTANCE.getHandlerPocket48();
        for(int id : handler.getChannelIDBySeverID(handler.getServerIDByStarID(starID)))
            sender.sendMessage(String.valueOf(id));
    }
}
