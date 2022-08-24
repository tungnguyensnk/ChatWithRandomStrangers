package tung.com.botfb.controller;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import tung.com.botfb.BotFbApplication;
import tung.com.botfb.model.InfoRepository;
import tung.com.botfb.model.RoomRepository;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@RestController
@EnableScheduling
public class RestAPI {

    static Map<String, String> hangDoi = new HashMap<>();
    static ArrayList<String> dangDangKy = new ArrayList<>();
    static ArrayList<String> dangTimKiem = new ArrayList<>();

    @GetMapping("/")
    public ResponseEntity<String> sub(@RequestParam("hub.mode") String mode, @RequestParam("hub.verify_token") String token, @RequestParam("hub.challenge") String challenge) {
        String verifyToken = "tung";
        if (mode.equals("subscribe") && token.equals(verifyToken)) {
            System.out.println("Kết nối webhook");
            return new ResponseEntity<>(challenge, HttpStatus.OK);
        }
        System.out.println("xin chào");
        return new ResponseEntity<>("wrong verify token", HttpStatus.OK);
    }

    public static String toUTF8(String s) {
        ByteBuffer buffer = StandardCharsets.UTF_8.encode(s);
        return StandardCharsets.UTF_8.decode(buffer).toString();
    }

    @PostMapping("/")
    public void handleMess(@RequestBody(required = false) String body) {
        System.out.println(body);
        JSONObject object = new JSONObject(body);
        if (object.getString("object").equals("page")) {
            JSONObject messaging = (JSONObject) ((JSONObject) object.getJSONArray("entry").get(0))
                    .getJSONArray("messaging").get(0);
            String id = messaging.getJSONObject("sender").getString("id");

            //trong room
            if (BotFbApplication.context.getBean(RoomRepository.class).checkInRoom(id) != null &&
                    BotFbApplication.context.getBean(InfoRepository.class).check(id) != null) {
                String idNguoiKia = BotFbApplication.context.getBean(RoomRepository.class).idNguoiKia(id);
                if (messaging.getJSONObject("message").has("text")) {
                    String rawString = messaging.getJSONObject("message").getString("text");
                    String text = toUTF8(rawString);
                    if (text.toLowerCase(Locale.ROOT).equals("end")) {
                        sendMess(id, "Bạn đã kết thúc cuộc trò chuyện.");
                        sendMess(idNguoiKia, "Cuộc trò chuyện đã kết thúc");
                        BotFbApplication.context.getBean(RoomRepository.class).xoa(id);
                        return;
                    }
                }

                if (messaging.getJSONObject("message").has("text")) {
                    String rawString = messaging.getJSONObject("message").getString("text");
                    String text = toUTF8(rawString);
                    sendMess(idNguoiKia, text);
                    return;
                }

                if (messaging.getJSONObject("message").has("attachments")) {
                    JSONArray list = messaging.getJSONObject("message").getJSONArray("attachments");
                    for (int i = 0; i < list.length(); i++) {
                        JSONObject obj = (JSONObject) list.get(i);
                        sendImage(idNguoiKia, obj.getString("type"), obj.getJSONObject("payload").getString("url"));
                    }
                    return;
                }
            }

            //không trong room
            if (messaging.getJSONObject("message").has("text")) {
                String rawString = messaging.getJSONObject("message").getString("text");
                String text = toUTF8(rawString);
                if (text.toLowerCase(Locale.ROOT).equals("đăng ký") && BotFbApplication.context.getBean(InfoRepository.class).check(id) == null) {
                    dangDangKy.add(id);
                    sendMessQuick(id, "Giới tính của bạn?", "Nam", "Nữ");
                    return;
                }

                if ((text.toLowerCase(Locale.ROOT).equals("nam") || text.toLowerCase(Locale.ROOT).equals("nữ")) &&
                        dangDangKy.contains(id)) {
                    dangDangKy.remove(id);
                    BotFbApplication.context.getBean(InfoRepository.class).them(id, text.toLowerCase(Locale.ROOT));
                    sendMessQuick(id, "Đăng ký thành công. Tìm kiếm người trò chuyện ngay?", "Tìm kiếm");
                    return;
                }

                if (text.toLowerCase(Locale.ROOT).equals("tìm kiếm") && BotFbApplication.context.getBean(InfoRepository.class).check(id) != null) {
                    dangTimKiem.add(id);
                    sendMessQuick(id, "Bạn tìm nam hay nữ?", "Nam", "Nữ");
                    return;
                }

                if ((text.toLowerCase(Locale.ROOT).equals("nam") || text.toLowerCase(Locale.ROOT).equals("nữ") && BotFbApplication.context.getBean(InfoRepository.class).check(id) != null) &&
                        dangTimKiem.contains(id)) {
                    dangTimKiem.remove(id);
                    hangDoi.put(id, text.toLowerCase(Locale.ROOT));
                    sendMess(id, "Đang tìm kiếm...");
                    return;
                }
            }

            //chưa đăng ký
            if (BotFbApplication.context.getBean(InfoRepository.class).check(id) == null) {
                sendMessQuick(id, "Bạn phải đăng ký trước.", "Đăng ký");
            }

        }
    }

    public static void sendMess(String id, String text) {
        try {
            String accessToken = "EAASu8PogxBUBACXuDnEQC8hy4Fk7gIaJlORl9iHwz5XwsUXcDQgGliTmTL6aMEeiQjz8boXkIGIOWr5D3y4k4f" +
                    "PX7KvCbYAVAxOR7VYWlCfMp60vq9IRNxdsf7DQXgxdf6Tkn36k9RIZAN5tLbay1BCTwh4a9ZBWCYfX5Han7qKiaYan4nO12Oa5S0l6iBY5sO2yU8rQZDZD";
            HttpResponse<String> response = Unirest.post("https://graph.facebook.com/v2.6/me/messages?access_token="
                    + accessToken).header("Content-Type", "application/json").body("{\"messaging_type\": \"UPDATE\",\"recipient\": {\"id\": \""
                    + id + "\"},\"message\": {\"text\": '" + text + "'}}").asString();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void sendMessQuick(String id, String text, String... reply) {
        try {
            String accessToken = "EAASu8PogxBUBACXuDnEQC8hy4Fk7gIaJlORl9iHwz5XwsUXcDQgGliTmTL6aMEeiQjz8boXkIGIOWr5D3y4k4f" +
                    "PX7KvCbYAVAxOR7VYWlCfMp60vq9IRNxdsf7DQXgxdf6Tkn36k9RIZAN5tLbay1BCTwh4a9ZBWCYfX5Han7qKiaYan4nO12Oa5S0l6iBY5sO2yU8rQZDZD";
            StringBuilder post = new StringBuilder();
            for (String s : reply) {
                post.append(",{\"content_type\": \"text\",\"title\": \"").append(s).append("\",\"payload\": \"<POSTBACK_PAYLOAD>\"}");
            }
            HttpResponse<String> response = Unirest.post("https://graph.facebook.com/v2.6/me/messages?access_token=" + accessToken)
                    .header("Content-Type", "application/json")
                    .body("{\"recipient\": {\"id\": \"" + id + "\"},\"messaging_type\": \"RESPONSE\",\"message\": {\"text\": \"" + text + "\",\"quick_replies\": [" + post.substring(1) + "]}}")
                    .asString();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void sendImage(String id, String type, File file) {
        try {
            String accessToken = "EAASu8PogxBUBACXuDnEQC8hy4Fk7gIaJlORl9iHwz5XwsUXcDQgGliTmTL6aMEeiQjz8boXkIGIOWr5D3y4k4f" +
                    "PX7KvCbYAVAxOR7VYWlCfMp60vq9IRNxdsf7DQXgxdf6Tkn36k9RIZAN5tLbay1BCTwh4a9ZBWCYfX5Han7qKiaYan4nO12Oa5S0l6iBY5sO2yU8rQZDZD";
            HttpResponse<String> response = Unirest.post("https://graph.facebook.com/v2.6/me/messages?access_token="
                    + accessToken).field("messaging_type", "UPDATE").field("recipient", "{\"id\": \""
                    + id + "\"}", "application/json").field("message", "{\"attachment\":{\"type\":\""
                    + type + "\", \"payload\":{\"is_reusable\":true}}}", "application/json").field("file", file, "image/jpeg").asString();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendImage(String id, String type, String url) {
        try {
            String accessToken = "EAASu8PogxBUBACXuDnEQC8hy4Fk7gIaJlORl9iHwz5XwsUXcDQgGliTmTL6aMEeiQjz8boXkIGIOWr5D3y4k4" +
                    "fPX7KvCbYAVAxOR7VYWlCfMp60vq9IRNxdsf7DQXgxdf6Tkn36k9RIZAN5tLbay1BCTwh4a9ZBWCYfX5Han7qKiaYan4nO12Oa5S0l6iBY5sO2yU8rQZDZD";
            HttpResponse<String> response = Unirest.post("https://graph.facebook.com/v2.6/me/messages?access_token="
                    + accessToken).field("messaging_type", "UPDATE").field("recipient", "{\"id\": \""
                    + id + "\"}", "application/json").field("message", "{\"attachment\":{\"type\":\""
                    + type + "\", \"payload\":{\"url\":\"" + url + "\",\"is_reusable\":true}}}", "application/json").asString();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Scheduled(fixedRate = 1000)
    public static void check() {
        hangDoi.forEach((s, s2) -> hangDoi.forEach((s1, s21) -> {
            if (!s.equals(s1)) {
                if (BotFbApplication.context.getBean(InfoRepository.class).checkGioiTinh(s).equals(s21) &&
                        BotFbApplication.context.getBean(InfoRepository.class).checkGioiTinh(s1).equals(s2)) {
                    sendMessQuick(s, "Tìm kiếm thành công.\\n Gõ End để kết thúc.", "Xin chào");
                    sendMessQuick(s1, "Tìm kiếm thành công.\\n Gõ End để kết thúc.", "Xin chào");
                    hangDoi.remove(s);
                    hangDoi.remove(s1);
                    BotFbApplication.context.getBean(RoomRepository.class).them(s, s1);
                }
            }
        }));
    }
}
