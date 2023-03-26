package net.lawaxi.util.sender;

import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpRequest;
import net.lawaxi.Shitboy;
import net.lawaxi.model.Pocket48Message;
import net.lawaxi.model.Pocket48MessageType;
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
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;

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

        for (int roomID : subscribe.getRoomIDs()) {
            for (Pocket48Message message : pocket.getNewMessages(roomID, endTime)) {
                try {
                    for(Message m: pharseMessage(message, group))
                        group.sendMessage(m);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public Message[] pharseMessage(Pocket48Message message, Group group) throws IOException {
        Pocket48Handler pocket = Shitboy.INSTANCE.getHandlerPocket48();
        String n = message.getNickName() + (message.getNickName().indexOf(message.getStarName()) != -1 ? "" : "(" + message.getStarName() + ")");
        String r = message.getRoom() + "(" + message.getOwnerName() + ")";
        String name = "【" + n + "@" + r + "】\n";

        switch (message.getType()) {
            case TEXT:
                return new Message[]{new PlainText(name + message.getBody())};
            case AUDIO:
                Audio audio = group.uploadAudio(ExternalResource.create(getRes(message.getResLoc())));
                return new Message[]{new PlainText(name + "发送了一条语音"),audio};
            case IMAGE:
            case EXPRESSIMAGE:
                Image image = group.uploadImage(ExternalResource.create(getRes(message.getResLoc())));
                return new Message[]{new PlainText(name + (message.getType() == Pocket48MessageType.VIDEO ? "发送了一部影片" : "")).plus(image)};
            case VIDEO:
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            group.getFiles().uploadNewFile("/" + r + "房间视频(" + DateUtil.format(new Date(message.getTime()), "yyyy-MM-dd HH:mm:ss") + ")",
                                    ExternalResource.create(getRes(message.getResLoc())));
                        } catch (IOException e) {
                            e.printStackTrace();
                            group.sendMessage(new PlainText("视频发送失败"));
                        }
                    }
                }.start();
                return new Message[]{new PlainText(name + "发送了一条视频")};
            case REPLY:
            case GIFTREPLY:
                return new Message[]{new PlainText(message.getReply().getNameTo() + "：" + message.getReply().getMsgTo() + "\n"
                        + name + message.getReply().getMsgFrom())};
            case LIVEPUSH:
                Image cover = group.uploadImage(ExternalResource.create(getRes(message.getLivePush().getCover())));
                return new Message[]{new PlainText("【" + getName() + "口袋48开播啦~】\n"
                        + message.getLivePush().getTitle()).plus(cover)};
            case FLIPCARD:
                return new Message[]{new PlainText("【" + getName() + "翻牌回复消息】\n"
                        + pocket.getAnswerNameTo(message.getAnswer().getAnswerID(), message.getAnswer().getQuestionID()) + "：" + message.getAnswer().getMsgTo()
                        + "------"
                        + message.getAnswer().getAnswer())};
            case FLIPCARD_AUDIO:
                Audio au = group.uploadAudio(ExternalResource.create(getRes(message.getResLoc())));
                return new Message[]{new PlainText("【" + getName() + "语音翻牌回复消息】\n"
                        + pocket.getAnswerNameTo(message.getAnswer().getAnswerID(), message.getAnswer().getQuestionID()) + "：" + message.getAnswer().getMsgTo()
                        + "------"),au};
            case FLIPCARD_VIDEO:
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            group.getFiles().uploadNewFile("/" + message.getOwnerName() + "公开视频翻牌(" + DateUtil.format(new Date(message.getTime()), "yyyy-MM-dd HH:mm:ss") + ")",
                                    ExternalResource.create(getRes(message.getResLoc())));
                        } catch (IOException e) {
                            e.printStackTrace();
                            group.sendMessage(new PlainText("翻牌回复视频发送失败"));
                        }
                    }
                }.start();
                return new Message[]{new PlainText("【" + message.getNickName() + "视频翻牌回复消息】\n"
                        + pocket.getAnswerNameTo(message.getAnswer().getAnswerID(), message.getAnswer().getQuestionID()) + "：" + message.getAnswer().getMsgTo()
                        + "------")};
            case PASSWORD_REDPACKAGE:
                return new Message[]{new PlainText(name + "红包信息")};
            case VOTE:
                return new Message[]{new PlainText(name + "投票信息")};
        }

        return new Message[]{new PlainText("不支持的消息")};
    }

}
