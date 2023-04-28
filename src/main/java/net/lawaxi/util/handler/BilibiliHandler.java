package net.lawaxi.util.handler;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import java.util.HashMap;

public class BilibiliHandler extends Handler {

    private static final String APILiveInfo = "https://api.live.bilibili.com/room/v1/Room/get_info?";
    private static final String APIUserInfo = "https://api.bilibili.com/x/space/acc/info?";

    public BilibiliHandler() {
        super();
    }


    public JSONObject shouldMention(int room_id, HashMap<Integer, Boolean> status) {
        //当前状态
        JSONObject data = getLiveData(room_id); //data
        if (data.getInt("code") == 1) {
            //不存在直播间直接返回
            return null;
        }
        JSONObject info = JSONUtil.parseObj(data.getObj("data")); //info
        boolean s = info.getInt("live_status") == 1;

        //缓存中状态
        boolean pre;
        if (status.containsKey(room_id)) pre = status.get(room_id);
        else pre = s;

        //更新并返回
        status.put(room_id, s);
        if (!pre && s) {
            return info;
        }
        return null;
    }

    public JSONObject getLiveData(int room_id) {
        String s = get(APILiveInfo + "room_id=" + room_id);
        return JSONUtil.parseObj(s);
    }

    public String getNameByMid(int uid) {
        String s = get(APIUserInfo + "mid=" + uid);
        JSONObject o = JSONUtil.parseObj(s);
        if (o.getInt("code") == 1) {
            //不存在用户
            return "null";
        }
        JSONObject data = JSONUtil.parseObj(o.getObj("data"));
        return data.getStr("name");
    }
}
