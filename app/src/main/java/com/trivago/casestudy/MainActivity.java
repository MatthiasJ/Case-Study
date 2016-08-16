package com.trivago.casestudy;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ExpandableListView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.trivago.casestudy.models.Movie;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;
import rx.Subscription;
import rx.functions.Action1;


public class MainActivity extends AppCompatActivity {


    public static final String BASE_URL = "https://api.trakt.tv/";
    public static final String LIMIT = "10";
    public static final String IMAGES = "images";


    private ListAdapter mAdapter;
    private ExpandableListView mListview;
    private int scrollCounter = 1;
    private Movie[] movies;


    private EditText editText;

    Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
            .create();

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build();


    public interface TrakTvEndpointInterface {

        // Request method and URL specified in the annotation
        // Callback for the parsed response is the last parameter

        @Headers({"Content-Type: application/json", "trakt-api-key: ad005b8c117cdeee58a1bdb7089ea31386cd489b21e14b19818c91511f12a086", "trakt-api-version: 2"})

        @GET("movies/popular?extended=images")
        Call<Movie[]> get10PopularMovies(@Query("extended") String images, @Query("page") String page, @Query("limit") String limit);


    }

    TrakTvEndpointInterface apiService =
            retrofit.create(TrakTvEndpointInterface.class);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText = (EditText) findViewById(R.id.editText);

        mListview = (ExpandableListView) findViewById(R.id.listView);
        mAdapter = new ListAdapter(this);

        mListview.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem % 10 == 0) {

                    // load moare

                    if (movies != null) {
                        //copy old movies to new array

                        loadMovies(scrollCounter + 1);
                    }

                }
            }
        });

        loadMovies(scrollCounter);

        Subscription editTextSub = RxTextView.textChanges(editText).subscribe(new Action1<String>() {
            @Override
            public void call(String value) {
                // do some work with new text
            }
        });



// make sure to unsubscribe the subscription.


    }




    private void loadMovies(int position) {

        String pos = Integer.toString(position);

        Call<Movie[]> movieCall = apiService.get10PopularMovies(IMAGES, pos, LIMIT);
        movieCall.enqueue(new Callback<Movie[]>() {


            @Override
            public void onResponse(Call<Movie[]> call, Response<Movie[]> response) {
                movies = response.body();
                mAdapter.setMovies(movies);

                if (mListview.getAdapter() == null) {
                    mListview.setAdapter(mAdapter);

                }
            }

            @Override
            public void onFailure(Call<Movie[]> call, Throwable t) {
                Log.d("MainActivity", "errorMessage: " + t.getMessage());
            }
        });
    }


}
