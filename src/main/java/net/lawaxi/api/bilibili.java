package net.lawaxi.api;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

public class bilibili {

    private static String url = "https://api.live.bilibili.com/room/v1/Room/get_info?";
    private static String roomUrl = "https://live.bilibili.com/";

    public static boolean getLiveStatus(String room_id) {
        String s = HttpUtil.get(url + "room_id=" + room_id);
        JSONObject object = JSONUtil.parseObj(s);
        if (object.getInt("code") == 1) {
            //不存在
            return false;
        }
        JSONObject info = JSONUtil.parseObj(object.getObj("data"));
        //是否在播
        return info.getInt("live_status") == 1;
    }

    public static JSONObject getLiveData(String room_id) {
        String s = HttpUtil.get(url + "room_id=" + room_id);
        JSONObject object = JSONUtil.parseObj(s);
        return JSONUtil.parseObj(object.getObj("data"));
        /*
        String uid = info.getStr("uid");
        String roomId = info.getStr("room_id");
        Long attention = info.getLong("attention");
        Long online = info.getLong("online");
        String description = info.getStr("description");
        String areaName = info.getStr("area_name");
        String backgroundUrl = info.getStr("background");
        String title = info.getStr("title");
        String userCoverUrl = info.getStr("user_cover");
        String keyFrameUrl = info.getStr("keyframe");
        String liveTime = info.getStr("live_time");
        String tags = info.getStr("tags");
        return info.getInt("live_status") == 1;*/
    }
}
