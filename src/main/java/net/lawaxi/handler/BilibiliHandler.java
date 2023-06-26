package net.lawaxi.handler;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BilibiliHandler extends WebHandler {

    private static final String APILiveInfo = "https://api.live.bilibili.com/room/v1/Room/get_info?";
    private static final String APIUserInfo = "https://api.bilibili.com/x/space/acc/info?";
    private static final String APIUserSpace = "https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/space_history?host_uid=";

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

    private static final HashMap<Integer, String> name_data = new HashMap<>();

    public String getNameByMid(int uid) {
        String s = get(APIUserInfo + "mid=" + uid);
        JSONObject o = JSONUtil.parseObj(s);
        if (o.getInt("code") == 0) {
            JSONObject data = JSONUtil.parseObj(o.getObj("data"));
            String n = data.getStr("name");
            name_data.put(uid, n);
            return n;
        } else if (o.getInt("code") == -799) { //访问频繁时被拒
            return name_data.containsKey(uid) ? name_data.get(uid) : getNameByMid(uid);
        }
        return "null";
    }

    //注意endTime中无key=uid时此方法返回null
    //采用13位时间戳
    public List<JSONObject> getOriSpaceData(int uid, HashMap<Integer, Long> endTime) {
        if (!endTime.containsKey(uid))
            return null;

        JSONArray a = getOriSpaceData(uid);
        if (a != null) {
            List<JSONObject> b = new ArrayList<>();
            long latest = 0;
            for (Object o : a.stream().toArray()) {
                JSONObject o1 = JSONUtil.parseObj(o);
                JSONObject desc = o1.getJSONObject("desc");
                long t = desc.getLong("timestamp") * 1000;

                if (endTime.get(uid) >= t) {
                    break; //api有时间次序
                }

                if (latest < t) {
                    latest = t;
                }

                b.add(o1);
            }
            if (latest != 0) {
                endTime.put(uid, latest);
            }
            return b;
        }
        return null;
    }

    public JSONArray getOriSpaceData(int uid) {
        String s = get(APIUserSpace + uid);
        JSONObject o = JSONUtil.parseObj(s);
        if (o.getInt("code") == 0) {
            return o.getJSONObject("data").getJSONArray("cards");
        }
        return null;
    }
}
