package com.trivago.casestudy;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ExpandableListView;

import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String[] uris = new String[]{};

        // Use OkHttpClient singleton
        OkHttpClient client = new OkHttpClient();



        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://api.trakt.tv/movies/popular?extended=full,images").newBuilder();
        String url = urlBuilder.build().toString();

        Request request = new Request.Builder()
                .url(url).addHeader("Content-Type","application/json").addHeader("trakt-api-key","ad005b8c117cdeee58a1bdb7089ea31386cd489b21e14b19818c91511f12a086").addHeader("trakt-api-version","2")
                .build();



        // Get a handler that can be used to post to the main thread
        client.newCall(request).enqueue(new Callback() {
                                            @Override
                                            public void onFailure(Call call, IOException e) {
                                                e.printStackTrace();
                                            }

                                            @Override
                                            public void onResponse(Call call, final Response response) throws IOException {
                                                if (!response.isSuccessful()) {
                                                    throw new IOException("Unexpected code " + response);
                                                }
                                            }
                                        });

        // Create new gson object
        final Gson gson = new Gson();
// Get a handler that can be used to post to the main thread
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            // Parse response using gson deserializer
                                            @Override
                                            public void onResponse(Call call, final Response response) throws IOException {
                                                // Process the data on the worker thread
                                                Item item = gson.fromJson(response.body().charStream(), Item.class);
                                                // Access deserialized user object here
                                            }
                                        });



        ExpandableListView mListview = (ExpandableListView) findViewById(R.id.listView);
        ListAdapter mAdapter = new ListAdapter(this);
        mListview.setAdapter(mAdapter);
    }





}
