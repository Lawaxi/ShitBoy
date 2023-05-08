package net.lawaxi.util;

import net.lawaxi.Shitboy;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.ListeningStatus;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.MemberJoinEvent;
import net.mamoe.mirai.event.events.UserMessageEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.Message;

public class Listener extends SimpleListenerHost {

    public CommandOperator operator = new CommandOperator();

    @EventHandler()
    public ListeningStatus onGroupMessage(GroupMessageEvent event) {
        Member sender = event.getSender();
        Group group = event.getGroup();
        String message = event.getMessage().contentToString();


        if (message.startsWith("/")) {
            Message m = operator.executePublic(message.split(" "), group, sender.getId());
            if (m != null)
                group.sendMessage(new At(sender.getId()).plus(m));
        }

        return ListeningStatus.LISTENING;
    }

    @EventHandler()
    public ListeningStatus onUserMessage(UserMessageEvent event) {
        User sender = event.getSender();
        String message = event.getMessage().contentToString();

        if (message.startsWith("/")) {
            Message m = operator.executePrivate(message, event);
            if (m != null)
                sender.sendMessage(m);
        }

        return ListeningStatus.LISTENING;
    }

    @EventHandler()
    public ListeningStatus onMemberJoin(MemberJoinEvent event) {
        if (Shitboy.INSTANCE.getProperties().welcome.containsKey(event.getGroupId())) {
            Member member = event.getMember();
            String welcome = Shitboy.INSTANCE.getProperties().welcome.get(event.getGroupId());
            event.getGroup().sendMessage(new At(member.getId()).plus(welcome));
        }

        return ListeningStatus.LISTENING;
    }
}
