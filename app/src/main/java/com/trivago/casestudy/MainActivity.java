package com.trivago.casestudy;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ExpandableListView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.trivago.casestudy.models.Movie;

import java.util.List;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class MainActivity extends AppCompatActivity {


    public static final String BASE_URL = "https://api.trakt.tv/";
    public static final String LIMIT = "10";
    public static final String IMAGES = "images";


    private ListAdapter mAdapter;
    private ExpandableListView mListview;
    private int scrollCounter = 1;

    private Subscription movieCallSubscription;
    private Observable<List<Movie>> movieCall;

    private EditText editText;


    RxJavaCallAdapterFactory rxAdapter = RxJavaCallAdapterFactory.createWithScheduler(Schedulers.io());

    Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
            .create();

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(rxAdapter)
            .build();


    TrakTvEndpointInterface apiService =
            retrofit.create(TrakTvEndpointInterface.class);








    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText = (EditText) findViewById(R.id.editText);

        loadMoviesPopMovies(scrollCounter);

        movieCallSubscription = movieCall.subscribe();


        mListview = (ExpandableListView) findViewById(R.id.listView);
        mAdapter = new ListAdapter(this);






        mListview.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                    // load moare
                    Log.d("MainActivity","first visible item: "+firstVisibleItem);

                    if (firstVisibleItem%9==0 && firstVisibleItem!=0) {
                        //copy old movies to new array

                    Log.d("MainActivity","here we are");
                        scrollCounter++;
                        loadMoviesPopMovies(scrollCounter);
                    }


            }
        });



    }



    private void loadMoviesPopMovies(int position) {

        String pos = Integer.toString(position);

        movieCall = apiService.get10PopularMovies(IMAGES, pos, LIMIT);
        movieCallSubscription = movieCall.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<List<Movie>>() {
            @Override
            public void onCompleted() {

                Log.d("MainActivity", "completed");
            }

            @Override
            public void onError(Throwable e) {
                Log.d("MainActivity", "error: " + e.getMessage());
            }

            @Override
            public void onNext(List<Movie> movies) {

                if (mListview.getAdapter() == null) {
                    mListview.setAdapter(mAdapter);
                }

                mAdapter.getMovies().addAll(movies);
                mAdapter.notifyDataSetChanged();
                Log.d("MainActivity", "onNExt");

            }
        });


    }


    @Override
    protected void onDestroy() {
        this.movieCallSubscription.unsubscribe();
        super.onDestroy();
    }

    public interface TrakTvEndpointInterface {

        // Request method and URL specified in the annotation
        // Callback for the parsed response is the last parameter

        @Headers({"Content-Type: application/json", "trakt-api-key: ad005b8c117cdeee58a1bdb7089ea31386cd489b21e14b19818c91511f12a086", "trakt-api-version: 2"})

        @GET("movies/popular?extended=images")
        Observable<List<Movie>> get10PopularMovies(@Query("extended") String images, @Query("page") String page, @Query("limit") String limit);


        //TODO change parameters
        @GET("movies/popular?extended=images")
        Observable<List<Movie>> getSpecificMovies(@Query("extended") String images, @Query("page") String page, @Query("limit") String limit);


    }
}
