package net.lawaxi.handler;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import net.lawaxi.Shitboy;

import java.util.HashMap;

public class WeiboHandler extends Handler {

    //微博监测采取游客登陆的方式，但也时刻要注意cookie失效

    private static final String APITID = "https://passport.weibo.com/visitor/genvisitor?cb=gen_callback&fp={\"os\":\"1\",\"browser\":\"Chrome70,0,3538,25\",\"fonts\":\"undefined\",\"screenInfo\":\"1920*1080*24\",\"plugins\":\"\"}";
    private static final String APISUB = "https://passport.weibo.com/visitor/visitor?a=incarnate&t=%s&w=%d&c&cb=restore_back&from=weibo";
    private static final String URLsuperTopic = "https://weibo.com/p/%s/super_index";
    private static final String APIUserProfile = "https://weibo.com/ajax/profile/info?uid=%d";
    private static final String APIUserBlog = "https://weibo.com/ajax/statuses/mymblog?uid=%d&page=1&feature=0";
    private static String cookie = "";

    public WeiboHandler() {
        super();
    }

    public String getSuperTopicRes(String id) {

        if (!cookie.equals("")) {
            String res = get(String.format(URLsuperTopic, id));
            if (!res.equals("")) //否则时效
                return res.equals("{\"code\":\"100006\",\"msg\":\"\",\"data\":\"https:\\/\\/weibo.com\\/sorry?pagenotfound&\"}") ? null : res;
        }

        //更新cookie
        try {
            updateLoginToSuccess();
            Shitboy.INSTANCE.getLogger().info("微博Cookie更新成功");

        } catch (Exception e) {
            Shitboy.INSTANCE.getLogger().info("微博Cookie更新失败");
        }
        return null;

    }

    private final HashMap<Long, String> name = new HashMap<>();

    public String getUserName(long id) {
        if (name.containsKey(id))
            return name.get(id);

        JSONObject a = getUserInfo(id);
        if (a == null)
            return "未知用户";

        String name = a.getJSONObject("user").getStr("screen_name");
        this.name.put(id, name);
        return name;
    }

    public JSONObject getUserInfo(long id) {
        if (!cookie.equals("")) {
            String ret = get(String.format(APIUserProfile, id));
            if (!ret.equals("")) {
                JSONObject o = JSONUtil.parseObj(ret);
                if (o.getInt("ok") == 1) {
                    return o.getJSONObject("data");
                } else
                    return null;
            }
        }

        try {
            updateLoginToSuccess();
            Shitboy.INSTANCE.getLogger().info("微博Cookie更新成功");

        } catch (Exception e) {
            Shitboy.INSTANCE.getLogger().info("微博Cookie更新失败");
        }
        return null;
    }

    public Object[] getUserBlog(long id) {
        if (!cookie.equals("")) {
            String ret = get(String.format(APIUserBlog, id));
            if (!ret.equals("")) {
                JSONObject o = JSONUtil.parseObj(ret);
                if (o.getInt("ok") == 1) {
                    return o.getJSONObject("data").getJSONArray("list").toArray(new Object[0]);
                } else
                    return null;
            }
        }

        try {
            updateLoginToSuccess();
            Shitboy.INSTANCE.getLogger().info("微博Cookie更新成功");

        } catch (Exception e) {
            Shitboy.INSTANCE.getLogger().info("微博Cookie更新失败");
        }
        return null;
    }

    public void updateLoginToSuccess() {
        try {
            updateLogin();
        } catch (Exception e) {
            updateLoginToSuccess();
        }
    }

    public void updateLogin() throws Exception {
        String a = getWithDefaultHeader(APITID);

        String tid = a.substring(a.indexOf("\"tid\":\"") + "\"tid\":\"".length(), a.indexOf("\",\"new_tid\""));
        boolean isNew = Boolean.parseBoolean(a.substring(a.indexOf("\"new_tid\":") + "\"new_tid\":".length(), a.indexOf("}})")));

        String b = getWithDefaultHeader(String.format(APISUB, tid, isNew ? 3 : 2));

        if (b.contains("\"msg\":\"succ\"") && !b.contains("null")) { //tid不合法时需要重新申请
            String sub = b.substring(b.indexOf("\"sub\":\"") + "\"sub\":\"".length(), b.indexOf("\",\"subp\":\""));
            String subp = b.substring(b.indexOf("\",\"subp\":\"") + "\",\"subp\":\"".length(), b.indexOf("\"}})"));
            cookie = "SUB=" + sub + "; SUBP=" + subp;

        } else {
            throw new Exception();
        }
    }

    public HttpRequest setDefaultHeader(HttpRequest request) {
        return request.header("authority", "weibo.com").header(
                "sec-ch-ua", "\"Chromium\";v=\"94\", \"Google Chrome\";v=\"94\", \";Not A Brand\";v=\"99\"").header(
                "content-type", "application/x-www-form-urlencoded").header(
                "x-requested-with", "XMLHttpRequest").header(
                "sec-ch-ua-mobile", "?0").header(
                "user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML).header( like Gecko) Chrome/94.0.4606.71 Safari/537.36").header(
                "sec-ch-ua-platform", "\"Windows\"").header(
                "accept", "*/*").header(
                "sec-fetch-site", "same-origin").header(
                "sec-fetch-mode", "cors").header(
                "sec-fetch-dest", "empty");
    }

    @Override
    protected HttpRequest setHeader(HttpRequest request) {
        return setDefaultHeader(request).header("cookie", cookie);
    }

    protected String getWithDefaultHeader(String url) {
        return setDefaultHeader(HttpRequest.get(url))
                .execute().body();
    }
}
