package Global;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.exceptions.UnirestException;

public class Unirest {
    public static String get(String url) {
        HttpResponse<String> response = null;

        try {
            response = com.mashape.unirest.http.Unirest.get(url)
                    .header("Cache-Control", "no-cache")
                    .asString();
        } catch (UnirestException e) {
            e.printStackTrace();
        }

        return response.getBody();
    }

    public static int post(String url, String body) {
        HttpResponse<String> response = null;

        try {
            response = com.mashape.unirest.http.Unirest.post(url)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Cache-Control", "no-cache")
                    .body(body).asString();
        } catch (UnirestException e) {
            e.printStackTrace();
        }

        return response.getStatus();
    }
}
