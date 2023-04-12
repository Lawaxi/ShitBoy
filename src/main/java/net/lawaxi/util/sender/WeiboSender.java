package net.lawaxi.util.sender;

import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import net.lawaxi.Shitboy;
import net.lawaxi.model.MessageWithTime;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.utils.ExternalResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WeiboSender extends Sender {

    private final HashMap<String, Long> endTime;

    public WeiboSender(Bot bot, long group, HashMap<String, Long> endTime) {
        super(bot, group);
        this.endTime = endTime;
    }

    @Override
    public void run() {
        List<MessageWithTime> ms = new ArrayList<>();

        try {
            //超话
            List<String> superChatSubscribe = Shitboy.INSTANCE.getProperties().weibo_superTopic_subscribe.get(group.getId());

            for (String id : superChatSubscribe) {
                String a = Shitboy.INSTANCE.getHandlerWeibo().getSuperTopicRes(id);
                if (a == null)
                    continue; //不存在超话

                a = a.substring(a.indexOf("onick']='") + "onick']='".length());
                String name = a.substring(0, a.indexOf("';"));
                Matcher b = Pattern.compile("(WB_info).*?(WB_detail)").matcher(a);

                if (!endTime.containsKey(id)) {
                    endTime.put(id, new Date().getTime());
                }

                long m = 0; //因为超话是按回复时间排序的 必须遍历完全部
                boolean find = false;

                while (b.find()) {
                    find = true;

                    String info = b.group().replace(" ", "");
                    String sender = info.substring(
                            info.indexOf("title=\\\"") + "title=\\\"".length()
                            , info.indexOf("\\\"suda-"));

                    //原博地址
                    info = info.substring(info.indexOf("WB_fromS_txt2"));
                    String link = info.substring(info.indexOf("https"), info.indexOf("\\\"title="))
                            .replace("\\/", "/");

                    //时间
                    info = info.substring(info.indexOf("date=\\\"") + "date=\\\"".length());
                    long time = Long.valueOf(info.substring(0, info.indexOf("\\\"")));
                    if (time <= endTime.get(id))//不播报
                        continue;

                    if (time > m)//决定最终更新的endTime
                        m = time;

                    //文字
                    String textInfo = "【" + name + "超话新内容：" + sender + "】\n";
                    if (info.indexOf("WB_textW_f14") != -1) { //有文字
                        info = info.substring(info.indexOf("feed_list_content\\\">") + "feed_list_content\\\">".length());
                        textInfo += handleWeiboText(info.substring(0, info.indexOf("<\\/div>"))) + "\n";
                        info = info.substring(info.indexOf("<\\/div>") + "<\\/div>".length());
                    }

                    Message o = new PlainText(textInfo);

                    //影像
                    if (info.indexOf("WB_media_wrap") != -1) {
                        //会有一个前置div

                        info = info.substring(info.indexOf("WB_media_a") + "WB_media_a".length());

                        if (info.contains("WB_video")) {//视频获取链接(原始地址无权限访问)
                            o = o.plus("https://video.weibo.com/show?fid=" + info.substring(info.indexOf("objectid=") + "objectid=".length(),
                                    info.indexOf("keys=")));
                        } else {//图片时获取原始地址
                            if (info.indexOf("&thumb_picSrc=") == -1) { //单张图
                                String src = "https:" + info.substring(
                                        info.indexOf("clear_picSrc=") + "clear_picSrc=".length(),
                                        info.indexOf("jpg")
                                ).replace("%2F", "/") + "jpg";
                                o = o.plus(group.uploadImage(ExternalResource.create(getRes(src))));
                            } else { //多张图
                                for (String src : info.substring(
                                        info.indexOf("clear_picSrc=") + "clear_picSrc=".length(),
                                        info.indexOf("&thumb_picSrc=")).replace("%2F", "/").split(",")) {
                                    System.out.println("https:" + src);
                                    o = o.plus(group.uploadImage(ExternalResource.create(getRes("https:" + src))));
                                }
                            }
                        }

                    }

                    ms.add(new MessageWithTime(o.plus("\nlink: " + link), time));

                }

                if (!find)
                    Shitboy.INSTANCE.getLogger().info(name + "超话找不到任何东西");
                if (m > endTime.get(id))
                    endTime.put(id, m);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            //个人
            List<Long> userSubscribe = Shitboy.INSTANCE.getProperties().weibo_user_subscribe.get(group.getId());
            for (long id : userSubscribe) {
                if (!endTime.containsKey(String.valueOf(id))) {
                    endTime.put(String.valueOf(id), new Date().getTime());
                }

                String name = Shitboy.INSTANCE.getHandlerWeibo().getUserName(id);
                Object[] as = Shitboy.INSTANCE.getHandlerWeibo().getUserBlog(id);
                if (as == null) {
                    Shitboy.INSTANCE.getLogger().info(name + "主页找不到任何东西");
                    continue;
                }

                long m = 0;
                for (Object a : as) {
                    JSONObject b = JSONUtil.parseObj(a);
                    if (b.containsKey("title")) { //赞过的，转发的微博(暂不处理——时间无法获取 排序也不对)
                        /*
                        o = new PlainText("【"+ b.getJSONObject("title").getStr("text")
                                .replaceAll("\\(\\d*\\)","")
                                .replace("他", name)+"】\n");
                        link += b.getJSONObject("user").getInt("id");*/
                        continue;

                    }

                    long time = DateUtil.parse(b.getStr("created_at")).getTime();
                    if (time <= endTime.get(String.valueOf(id)))
                        continue;
                    if (time > m)
                        m = time;

                    ms.add(new MessageWithTime(new PlainText("【" + name + "微博更新】\n")
                            .plus(parseUserBlog(b))
                            .plus("\nlink: " + "https://weibo.com/" + id + "/" + b.getStr("mblogid")), time));
                }
                if (m > endTime.get(String.valueOf(id)))
                    endTime.put(String.valueOf(id), m);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        ms.sort((a, b) -> a.getTime() - b.getTime() > 0 ? 1 : 0); //由小到大排序
        for (MessageWithTime m : ms) {
            group.sendMessage(m.getMessage());
        }
    }

    private Message parseUserBlog(JSONObject b) throws IOException {
        String oriText = b.getStr("text");
        String text = handleWeiboText(oriText); //另有文本格式的text_raw(草稿中样式)，但视频提示不友好，我毅然决然选择text+处理
        Message o = new PlainText( text + "\n");

        //图片
        if (b.containsKey("pic_infos")) {
            JSONObject pic_infos = b.getJSONObject("pic_infos");
            for (String pic_id : b.getJSONArray("pic_ids").toArray(new String[0])) {
                JSONObject pic_info = pic_infos.getJSONObject(pic_id);
                String src = pic_info.getJSONObject("mw2000")
                        .getStr("url");
                o = o.plus(group.uploadImage(ExternalResource.create(getRes(src))));
            }
        }

        //视频
        if (oriText.indexOf("video.weibo.com") != -1) {
            oriText = oriText.substring(oriText.indexOf("video.weibo.com") + "video.weibo.com".length());
            String src = "https://video.weibo.com" + oriText.substring(0, oriText.indexOf("\">"));
            o = o.plus(src);
        }

        //转发
        if(b.containsKey("retweeted_status")){
            o = o.plus("\n").plus(parseUserBlog(b.getJSONObject("retweeted_status")));//叠呗
        }

        return o;
    }

    @Override
    public InputStream getRes(String resLoc) {
        return HttpRequest.get(resLoc).header("Referer", "https://weibo.com/").execute().bodyStream();
    }

    private String handleWeiboText(String oriText) {
        return oriText.replaceAll("&#xe627;", "▼")
                .replaceAll("<br>", "\n")
                .replaceAll("<.*?>", "");
    }
}
