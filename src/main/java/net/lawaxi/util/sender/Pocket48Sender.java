package net.lawaxi.util.sender;

import cn.hutool.core.date.DateUtil;
import net.lawaxi.Shitboy;
import net.lawaxi.handler.Pocket48Handler;
import net.lawaxi.model.*;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.data.*;
import net.mamoe.mirai.utils.ExternalResource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Pocket48Sender extends Sender {

    //endTime是一个关于roomID的HashMap
    private final HashMap<Long, Long> endTime;
    private final HashMap<Long, List<Long>> voiceStatus;
    private final HashMap<Long, Pocket48SenderCache> cache;

    public Pocket48Sender(Bot bot, long group, HashMap<Long, Long> endTime, HashMap<Long, List<Long>> voiceStatus, HashMap<Long, Pocket48SenderCache> cache) {
        super(bot, group);
        this.endTime = endTime;
        this.voiceStatus = voiceStatus;
        this.cache = cache;
    }

    @Override
    public void run() {
        try {
            Pocket48Subscribe subscribe = Shitboy.INSTANCE.getProperties().pocket48_subscribe.get(group.getId());
            Pocket48Handler pocket = Shitboy.INSTANCE.getHandlerPocket48();

            //房间消息获取
            for (long roomID : subscribe.getRoomIDs()) {
                if (!cache.containsKey(roomID)) {
                    cache.put(roomID, Pocket48SenderCache.create(roomID, endTime));
                }
            }

            List<Pocket48Message[]> totalMessages = new ArrayList<>();

            for (long roomID : subscribe.getRoomIDs()) {
                if (cache.get(roomID) == null)
                    continue;

                Pocket48RoomInfo roomInfo = cache.get(roomID).roomInfo;

                //房间消息预处理
                Pocket48Message[] a = cache.get(roomID).messages;
                if (a.length > 0) {
                    totalMessages.add(a);
                }

                //房间语音
                List<Long> n = cache.get(roomID).voiceList;
                if (voiceStatus.containsKey(roomID)) {
                    String[] r = handleVoiceList(voiceStatus.get(roomID), n);
                    if (r[0] != null || r[1] != null) {
                        String ownerName = pocket.getOwnerOrTeamName(roomInfo);
                        boolean private_ = ownerName.equals(roomInfo.getOwnerName());
                        String message = "【" + roomInfo.getRoomName() + "(" + ownerName + ")房间语音】\n";

                        if (r[0] != null) {
                            message += private_ ?
                                    "上麦啦~" //成员房间
                                    : "★ 上麦：\n" + r[0] + "\n"; //队伍房间
                        }
                        if (r[1] != null) {
                            message += private_ ?
                                    "下麦了捏~"
                                    : "☆ 下麦：\n" + r[1];
                        }
                        Message m = new PlainText(message);
                        group.sendMessage(r[0] != null ? toNotification(m) : m); //有人上麦时才@all
                    }
                }
                voiceStatus.put(roomID, n);
            }

            //房间消息
            if (totalMessages.size() > 0) {
                if (Shitboy.INSTANCE.getProperties().pocket48_subscribe.get(group.getId()).showAtOne()) {

                    for (Pocket48Message[] roomMessage : totalMessages) {
                        Message joint = null;
                        Message title = null;
                        for (int i = roomMessage.length - 1; i >= 0; i--) {
                            try {
                                Pocket48SenderMessage message1 = pharseMessage(roomMessage[i], group, subscribe.getRoomIDs().size() == 1);
                                if (message1 == null)
                                    continue;

                                if (message1.canJoin() || message1.isSpecific()) {
                                    if (joint == null) {
                                        title = joint = message1.getTitle();
                                    } else if (!title.contentEquals(message1.getTitle(), false)) {
                                        joint = joint.plus(message1.getTitle());
                                        title = message1.getTitle();
                                    }
                                    joint = joint.plus(message1.getMessage()[0]).plus("\n");
                                }

                                if (!message1.canJoin()) {
                                    //遇到不可组合的消息先发送以前的可组合消息
                                    if (joint != null) {
                                        group.sendMessage(joint);
                                        joint = null;
                                        title = null;
                                    }

                                    //不可组合消息的发送需要通过for循环完成
                                    for (int j = (message1.isSpecific() ? 1 : 0); j < message1.getMessage().length; j++) {
                                        group.sendMessage(message1.getMessage()[j]);
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        if (joint != null) {
                            group.sendMessage(joint);
                            joint = null;
                            title = null;
                        }
                    }


                } else {
                    for (Pocket48Message[] roomMessage : totalMessages) {
                        for (int i = roomMessage.length - 1; i >= 0; i--) { //倒序输出
                            try {
                                for (Message m : pharseMessage(roomMessage[i], group, subscribe.getRoomIDs().size() == 1).getUnjointMessage()) {
                                    group.sendMessage(m);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String[] handleVoiceList(List<Long> a, List<Long> b) {
        String zengjia = "";
        String jianshao = "";
        for (Long b0 : b) {
            if (!a.contains(b0))
                zengjia += "，" + b0;
        }
        for (Long a0 : a) {
            if (!b.contains(a0))
                jianshao += "，" + a0;
        }
        return new String[]{(zengjia.length() > 0 ? zengjia.substring(1) : null),
                (jianshao.length() > 0 ? jianshao.substring(1) : null)};
    }

    public Pocket48SenderMessage pharseMessage(Pocket48Message message, Group group, boolean single_subscribe) throws IOException {
        Pocket48Handler pocket = Shitboy.INSTANCE.getHandlerPocket48();
        String n = message.getNickName() + (message.getNickName().indexOf(message.getStarName()) != -1 ? "" : "(" + message.getStarName() + ")");
        String r = message.getRoom() + "(" + message.getOwnerName() + ")";
        String name = "【" + n + "@" + r + "】\n";

        switch (message.getType()) {
            case TEXT:
            case GIFT_TEXT:
                return new Pocket48SenderMessage(true, new PlainText(name),
                        new Message[]{pharsePocketTextWithFace(message.getText())});
            case AUDIO: {
                Audio audio = group.uploadAudio(ExternalResource.create(getRes(message.getResLoc())));
                return single_subscribe ? new Pocket48SenderMessage(false, null,
                        new Message[]{audio}) : new Pocket48SenderMessage(false, new PlainText(name),
                        new Message[]{new PlainText("发送了一条语音"), audio}).setSpecific();
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
                            group.getFiles().uploadNewFile("/" + r + "房间视频(" + DateUtil.format(new Date(message.getTime()), "yyyy-MM-dd HH-mm-ss") + ").mp4",
                                    ExternalResource.create(getRes(message.getResLoc())));
                        } catch (IOException e) {
                            e.printStackTrace();
                            group.sendMessage(new PlainText("视频发送失败"));
                        }
                    }
                }.start();
                return single_subscribe ? null : new Pocket48SenderMessage(false, new PlainText(name),
                        new Message[]{new PlainText("发送了一条视频")}).setSpecific();
            case REPLY:
            case GIFTREPLY:
                return new Pocket48SenderMessage(false, null,
                        new Message[]{new PlainText(message.getReply().getNameTo() + "：").plus(pharsePocketTextWithFace(message.getReply().getMsgTo())).plus("\n"
                                + name).plus(pharsePocketTextWithFace(message.getReply().getMsgFrom()))});
            case LIVEPUSH:
                Image cover = group.uploadImage(ExternalResource.create(getRes(message.getLivePush().getCover())));
                return new Pocket48SenderMessage(false, null,
                        new Message[]{toNotification(new PlainText("【" + (single_subscribe ? "" : message.getOwnerName()) + "口袋48开播啦~】\n"
                                + message.getLivePush().getTitle()).plus(cover))});
            case FLIPCARD:
                return new Pocket48SenderMessage(false, null, new Message[]{new PlainText("【" + (single_subscribe ? "" : message.getOwnerName()) + "翻牌回复消息】\n"
                        + pocket.getAnswerNameTo(message.getAnswer().getAnswerID(), message.getAnswer().getQuestionID()) + "：" + message.getAnswer().getMsgTo()
                        + "\n------\n"
                        + message.getAnswer().getAnswer())});
            case FLIPCARD_AUDIO:
                Audio audio = group.uploadAudio(ExternalResource.create(getRes(message.getAnswer().getResInfo())));
                return new Pocket48SenderMessage(false, null,
                        new Message[]{new PlainText("【" + (single_subscribe ? "" : message.getOwnerName()) + "语音翻牌回复消息】\n"
                                + pocket.getAnswerNameTo(message.getAnswer().getAnswerID(), message.getAnswer().getQuestionID()) + "：" + message.getAnswer().getMsgTo()
                                + "\n------\n"), audio});
            case FLIPCARD_VIDEO:
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            group.getFiles().uploadNewFile("/" + (single_subscribe ? "" : message.getOwnerName()) + "公开视频翻牌(" + DateUtil.format(new Date(message.getTime()), "yyyy-MM-dd HH-mm-ss") + ").mp4",
                                    ExternalResource.create(getRes(message.getAnswer().getResInfo())));
                        } catch (IOException e) {
                            e.printStackTrace();
                            group.sendMessage(new PlainText("翻牌回复视频发送失败"));
                        }
                    }
                }.start();
                return new Pocket48SenderMessage(false, null,
                        new Message[]{new PlainText("【" + (single_subscribe ? "" : message.getOwnerName()) + "视频翻牌回复消息】\n"
                                + pocket.getAnswerNameTo(message.getAnswer().getAnswerID(), message.getAnswer().getQuestionID()) + "：" + message.getAnswer().getMsgTo()
                                + "------")});
            case PASSWORD_REDPACKAGE:
                return new Pocket48SenderMessage(true, new PlainText(name),
                        new Message[]{new PlainText("红包信息")});
            case VOTE:
                return new Pocket48SenderMessage(true, new PlainText(name),
                        new Message[]{new PlainText("投票信息")});
        }

        return new Pocket48SenderMessage(true, new PlainText(name),
                new Message[]{new PlainText("不支持的消息")});
    }

    public Message pharsePocketTextWithFace(String body) {
        String[] a = body.split("\\[.*?\\]", -1);//其余部分，-1使其产生空字符串
        if (a.length < 2)
            return new PlainText(body);

        Message out = new PlainText(a[0]);
        int count = 1;//从第1个表情后a[1]开始
        Matcher b = Pattern.compile("\\[.*?\\]").matcher(body);
        while (b.find()) {
            out = out.plus(pharsePocketFace(b.group()));
            out = out.plus(a[count]);
            count++;
        }

        return out;
    }

    public Message pharsePocketFace(String face) {
        if (face.equals("[亲亲]"))
            face = "[左亲亲]";

        for (int i = 0; i < Face.names.length; i++) {
            if (Face.names[i].equals(face))
                return new Face(i);
        }
        return new PlainText(face);
    }

}
