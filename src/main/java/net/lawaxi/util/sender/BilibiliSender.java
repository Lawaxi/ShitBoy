package net.lawaxi.util.sender;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import net.lawaxi.Shitboy;
import net.lawaxi.handler.BilibiliHandler;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.utils.ExternalResource;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.lawaxi.model.EndTime.newTime;

public class BilibiliSender extends Sender {
    private static final String roomUrl = "https://live.bilibili.com/";

    private final HashMap<Integer, Long> endTime;
    private final HashMap<Integer, Boolean> status;

    public BilibiliSender(Bot bot, long group, HashMap<Integer, Long> endTime, HashMap<Integer, Boolean> status) {
        super(bot, group);
        this.endTime = endTime;
        this.status = status;
    }

    @Override
    public void run() {
        BilibiliHandler bili = Shitboy.INSTANCE.getHandlerBilibili();
        List<Integer> bili_subs = Shitboy.INSTANCE.getProperties().bilibili_subscribe.get(group_id);
        List<Integer> live_subs = Shitboy.INSTANCE.getProperties().bililive_subscribe.get(group_id);

        for (Integer uid : bili_subs) {
            if (!endTime.containsKey(uid)) {
                endTime.put(uid, newTime());
            }

            List<JSONObject> a = bili.getOriSpaceData(uid, endTime);
            if (a == null) {
                Shitboy.INSTANCE.getLogger().info("获取uid为" + uid + "b站主页错误");
                continue;
            }
            if (a.size() > 0) {
                String name = bili.getNameByMid(uid);
                for (int i = a.size() - 1; i >= 0; i--) {
                    JSONObject a0 = a.get(i);
                    JSONObject card = JSONUtil.parseObj(a0.getStr("card")
                            .replace("\\\\/", "/")
                            .replace("\\", ""));

                    //动态
                    if (card.containsKey("item")) {
                        JSONObject item = card.getJSONObject("item");

                        //直播推送或捞视频
                        JSONObject push;
                        try {
                            push = JSONUtil.parseObj(a0.getJSONObject("display").getJSONArray("add_on_card_info")
                                    .get(0));
                        } catch (Exception e) {
                            push = null;
                        }

                        if (item.containsKey("content")) {
                            //纯文字动态 & 直播推送
                            group.sendMessage(push == null ?
                                    new PlainText("【" + name + " 发布了B站动态】\n").plus(pharseBilibiliContent(item.getStr("content"), a0)) :
                                    new PlainText("【" + name + " 发布了B站直播预约】\n").plus(pharseBilibiliContent(item.getStr("content"), a0)).plus("\n---------\n" + push.getStr("title")));


                        } else if (item.containsKey("description")) {
                            //带图动态
                            Message content = pharseBilibiliContent(item.getStr("description"), a0);

                            //图片
                            if (item.containsKey("pictures")) {
                                for (Object pic : item.getJSONArray("pictures").stream().toArray()) {
                                    JSONObject pic1 = JSONUtil.parseObj(pic);
                                    try {
                                        content = content.plus(group.uploadImage(ExternalResource.create(
                                                getRes(pic1.getStr("img_src"))
                                        )));
                                    } catch (Exception e) {

                                    }
                                }
                            }

                            //捞视频
                            if (push != null) {
                                try {
                                    JSONObject attach = push.getJSONObject("ugc_attach_card");
                                    content = content.plus("\n---------\n"
                                            + attach.getStr("title")
                                            + attach.getStr("play_url"));
                                } catch (Exception e) {

                                }
                            }

                            group.sendMessage(new PlainText("【" + name + " 发布了B站动态】\n").plus(content));
                        }


                    }
                    //视频投稿
                    else if (card.containsKey("desc")) {
                        Message content = pharseBilibiliContent(card.getStr("desc"), a0);

                        try {//封面
                            //
                            // card.getStr("first_frame");

                            content = content.plus(group.uploadImage(ExternalResource.create(
                                    getRes(card.getStr("pic"))
                            )));
                        } catch (Exception e) {

                        }

                        JSONObject desc = a0.getJSONObject("desc");
                        content = content.plus("https://b23.tv/" + desc.getStr("bvid"));

                        group.sendMessage(new PlainText("【" + name + " 发布了B站" + (card.containsKey("copyright") ? "" : "动态") + "视频】\n").plus(content));
                    } else {
                        group.sendMessage(new PlainText("【" + name + " B站更新】\n未知内容"));
                        Shitboy.INSTANCE.getLogger().warning(a0.toString());
                    }

                }
            }
        }

        for (Integer room : live_subs) {
            JSONObject info = bili.shouldMention(room, status);
            if (info != null) {
                String title = info.getStr("title");
                String description = info.getStr("description");
                String cover = info.getStr("user_cover");
                String name = bili.getNameByMid(info.getInt("uid"));

                try {
                    group.sendMessage(toNotification(new PlainText("【" + name + "开播啦~】\n" + title)
                            .plus(group.uploadImage(ExternalResource.create(getRes(cover))))
                            .plus(new PlainText(roomUrl + room))));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public Message pharseBilibiliContent(String body, JSONObject a0) {
        if (a0.getJSONObject("display").containsKey("emoji_info"))
            return pharseBilibiliContentWithEmoji(body,
                    a0.getJSONObject("display")
                            .getJSONObject("emoji_info")
                            .getJSONArray("emoji_details"));
        else
            return new PlainText(body);
    }

    public Message pharseBilibiliContentWithEmoji(String body, JSONArray emoji_details) {
        HashMap<String, String> emojis = new HashMap<>();
        for (Object emoji : emoji_details.stream().toArray()) {
            JSONObject emoji1 = JSONUtil.parseObj(emoji);
            emojis.put(emoji1.getStr("emoji_name"), emoji1.getStr("url"));
        }

        String[] a = body.split("\\[.*?\\]", -1);//其余部分，-1使其产生空字符串
        if (a.length < 2)
            return new PlainText(body);

        Message out = new PlainText(a[0]);
        int count = 1;//从第1个表情后a[1]开始
        Matcher b = Pattern.compile("\\[.*?\\]").matcher(body);
        while (b.find()) {
            String e = b.group();
            if (emojis.containsKey(e)) {
                try {
                    out = out.plus(group.uploadImage(ExternalResource.create(getRes(emojis.get(e)))));
                } catch (Exception exception) {

                }
            } else {
                out = out.plus(e);
            }
            out = out.plus(a[count]);
            count++;
        }

        return out;
    }
}
