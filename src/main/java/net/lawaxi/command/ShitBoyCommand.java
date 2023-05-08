package net.lawaxi.command;

import net.lawaxi.Shitboy;
import net.lawaxi.util.handler.Pocket48Handler;
import net.mamoe.mirai.console.command.CommandSender;
import net.mamoe.mirai.console.command.java.JCompositeCommand;

public class ShitBoyCommand extends JCompositeCommand {

    public ShitBoyCommand() {
        super(Shitboy.INSTANCE, "shitboy", "开关");
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
        for (Integer id : handler.getChannelIDBySeverID(handler.getServerIDByStarID(starID)))
            sender.sendMessage(id == null ? "无服务器" : String.valueOf(id));
    }
}
