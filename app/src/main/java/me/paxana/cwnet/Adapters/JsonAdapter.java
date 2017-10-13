package me.paxana.cwnet.Adapters;

import org.json.JSONException;

import java.io.IOException;

import me.paxana.cwnet.Model.Movie;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by paxie on 10/8/17.
 */

public class JsonAdapter {

    public static String jsonData;

    public static String getRawResults(String searchParam, Callback callback) throws JSONException {
        String apiKey = "face7189";
        String theURL = "http://www.omdbapi.com/?s=" + searchParam + "&apikey=" + apiKey;

        final OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(theURL).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                jsonData = response.body().string();
            }

        });
        return jsonData;
    }

}
