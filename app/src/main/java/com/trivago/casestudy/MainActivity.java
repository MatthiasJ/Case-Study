package com.trivago.casestudy;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ExpandableListView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.trivago.casestudy.models.Movie;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Headers;

public class MainActivity extends AppCompatActivity {


    // Trailing slash is needed
    public static final String BASE_URL = "https://api.trakt.tv/";
    private ListAdapter mAdapter;
    private  ExpandableListView mListview;


    Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
            .create();

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build();
    private Movie[] movies;


    public interface TrakTvEndpointInterface {

        // Request method and URL specified in the annotation
        // Callback for the parsed response is the last parameter


        @Headers({"Content-Type: application/json", "trakt-api-key: ad005b8c117cdeee58a1bdb7089ea31386cd489b21e14b19818c91511f12a086", "trakt-api-version: 2"})


        @GET("movies/popular?extended=images")
        Call<Movie[]> get10PopularMovies();


    }

    TrakTvEndpointInterface apiService =
            retrofit.create(TrakTvEndpointInterface.class);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mListview = (ExpandableListView) findViewById(R.id.listView);
        mAdapter = new ListAdapter(this);


        Call<Movie[]> movieCall = apiService.get10PopularMovies();
        movieCall.enqueue(new Callback<Movie[]>() {


            @Override
            public void onResponse(Call<Movie[]> call, Response<Movie[]> response) {
                movies = response.body();

                Log.d("MainActivity", movies[0].getTitle());
                mAdapter.setMovies(movies);
                mListview.setAdapter(mAdapter);
            }

            @Override
            public void onFailure(Call<Movie[]> call, Throwable t) {
                Log.d("MainActivity", "errorMessage: " + t.getMessage());
            }
        });



    }


}
