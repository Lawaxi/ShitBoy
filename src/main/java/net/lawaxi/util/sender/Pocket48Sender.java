package net.lawaxi.util.sender;

import cn.hutool.core.date.DateUtil;
import net.lawaxi.Shitboy;
import net.lawaxi.model.Pocket48Message;
import net.lawaxi.model.Pocket48Subscribe;
import net.lawaxi.util.handler.Pocket48Handler;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.data.Audio;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.utils.ExternalResource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class Pocket48Sender extends Sender {
    private final HashMap<Integer, Long> endTime;

    public Pocket48Sender(Bot bot, long group, HashMap<Integer, Long> endTime) {
        super(bot, group);
        this.endTime = endTime;
    }

    @Override
    public void run() {
        Pocket48Subscribe subscribe = Shitboy.INSTANCE.getProperties().pocket48_subscribe.get(group.getId());
        Pocket48Handler pocket = Shitboy.INSTANCE.getHandlerPocket48();

        List<Pocket48Message[]> totalMessages = new ArrayList<>();

        for (int roomID : subscribe.getRoomIDs()) {
            Pocket48Message[] a = pocket.getNewMessages(roomID, endTime); //endTime是一个关于roomID的HashMap

            if (a.length > 0) {
                totalMessages.add(a);
            }
        }

        if(totalMessages.size()>0) {
            if(Shitboy.INSTANCE.getProperties().pocket48_subscribe.get(group.getId()).showAtOne()){

                List<Pocket48SenderMessage> pharsedMessage = new ArrayList<>();
                Message roomJoint = null;

                //需要累加的消息
                for(Pocket48Message[] roomMessage : totalMessages) {
                    Message joint = null;
                    for (int i=roomMessage.length-1;i>=0;i--) { //倒序输出
                        try {
                            Pocket48SenderMessage message1 = pharseMessage(roomMessage[i], group);
                            pharsedMessage.add(message1);

                            if (message1.canJoin()) {
                                if (joint == null)
                                    joint = message1.getTitle();
                                joint = joint.plus("· ").plus(message1.getMessage()[0]).plus("\n");
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    if(roomJoint == null) {
                        roomJoint = joint;
                    }else {
                        roomJoint = roomJoint.plus(joint);
                    }
                }

                if(roomJoint!=null)
                    group.sendMessage(roomJoint);

                //不需要累加的消息
                for(Pocket48SenderMessage message : pharsedMessage){
                    if(message.canJoin()){
                        for(int i = 1;i<message.getMessage().length;i++)
                            group.sendMessage(message.getMessage()[i]);
                    }else{
                        for(Message m : message.getMessage())
                            group.sendMessage(m);
                    }
                }


            }else{
                for(Pocket48Message[] roomMessage : totalMessages) {
                    for (int i=roomMessage.length-1;i>=0;i--) { //倒序输出
                        try {
                            for (Message m : pharseMessage(roomMessage[i], group).getUnjointMessage()) {
                                group.sendMessage(m);
                            }
                        }catch (Exception e)
                        {e.printStackTrace();}
                    }
                }
            }
        }
    }

    public Pocket48SenderMessage pharseMessage(Pocket48Message message, Group group) throws IOException {
        Pocket48Handler pocket = Shitboy.INSTANCE.getHandlerPocket48();
        String n = message.getNickName() + (message.getNickName().indexOf(message.getStarName()) != -1 ? "" : "(" + message.getStarName() + ")");
        String r = message.getRoom() + "(" + message.getOwnerName() + ")";
        String name = "【" + n + "@" + r + "】\n";

        switch (message.getType()) {
            case TEXT:
                return new Pocket48SenderMessage(true, new PlainText(name),
                        new Message[]{new PlainText(message.getBody())});
            case AUDIO: {
                Audio audio = group.uploadAudio(ExternalResource.create(getRes(message.getResLoc())));
                return new Pocket48SenderMessage(true, new PlainText(name),
                        new Message[]{new PlainText("发送了一条语音"), audio});
            }
            case IMAGE:
            case EXPRESSIMAGE: {
                Image image = group.uploadImage(ExternalResource.create(getRes(message.getResLoc())));
                return new Pocket48SenderMessage(true, new PlainText(name),
                        new Message[]{image});
            }
            case VIDEO:
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            group.getFiles().uploadNewFile("/" + r + "房间视频(" + DateUtil.format(new Date(message.getTime()), "yyyy-MM-dd HH-mm-ss") + ")",
                                    ExternalResource.create(getRes(message.getResLoc())));
                        } catch (IOException e) {
                            e.printStackTrace();
                            group.sendMessage(new PlainText("视频发送失败"));
                        }
                    }
                }.start();
                return new Pocket48SenderMessage(true, new PlainText(name),
                        new Message[]{new PlainText("发送了一条视频")});
            case REPLY:
            case GIFTREPLY:
                return new Pocket48SenderMessage(false,null,
                        new Message[]{new PlainText(message.getReply().getNameTo() + "：" + message.getReply().getMsgTo() + "\n"
                        + name + message.getReply().getMsgFrom())});
            case LIVEPUSH:
                Image cover = group.uploadImage(ExternalResource.create(getRes(message.getLivePush().getCover())));
                return new Pocket48SenderMessage(false,null,
                        new Message[]{new PlainText("【" + getName() + "口袋48开播啦~】\n"
                        + message.getLivePush().getTitle()).plus(cover)});
            case FLIPCARD:
                return new Pocket48SenderMessage(false,null, new Message[]{new PlainText("【" + getName() + "翻牌回复消息】\n"
                        + pocket.getAnswerNameTo(message.getAnswer().getAnswerID(), message.getAnswer().getQuestionID()) + "：" + message.getAnswer().getMsgTo()
                        + "------"
                        + message.getAnswer().getAnswer())});
            case FLIPCARD_AUDIO:
                Audio audio = group.uploadAudio(ExternalResource.create(getRes(message.getResLoc())));
                return new Pocket48SenderMessage(false,null,
                        new Message[]{new PlainText("【" + getName() + "语音翻牌回复消息】\n"
                        + pocket.getAnswerNameTo(message.getAnswer().getAnswerID(), message.getAnswer().getQuestionID()) + "：" + message.getAnswer().getMsgTo()
                        + "------"), audio});
            case FLIPCARD_VIDEO:
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            group.getFiles().uploadNewFile("/" + message.getOwnerName() + "公开视频翻牌(" + DateUtil.format(new Date(message.getTime()), "yyyy-MM-dd HH-mm-ss") + ")",
                                    ExternalResource.create(getRes(message.getResLoc())));
                        } catch (IOException e) {
                            e.printStackTrace();
                            group.sendMessage(new PlainText("翻牌回复视频发送失败"));
                        }
                    }
                }.start();
                return new Pocket48SenderMessage(false,null,
                        new Message[]{new PlainText("【" + message.getNickName() + "视频翻牌回复消息】\n"
                        + pocket.getAnswerNameTo(message.getAnswer().getAnswerID(), message.getAnswer().getQuestionID()) + "：" + message.getAnswer().getMsgTo()
                        + "------")});
            case PASSWORD_REDPACKAGE:
                return new Pocket48SenderMessage(true, new PlainText(name),
                        new Message[]{new PlainText(name + "红包信息")});
            case VOTE:
                return new Pocket48SenderMessage(true, new PlainText(name),
                        new Message[]{new PlainText(name + "投票信息")});
        }

        return new Pocket48SenderMessage(true, new PlainText(name),
                new Message[]{new PlainText("不支持的消息")});
    }

}
