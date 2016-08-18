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
import com.trivago.casestudy.models.SearchResult;

import java.util.ArrayList;
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
import rx.functions.Action1;
import rx.schedulers.Schedulers;


public class MainActivity extends AppCompatActivity {


    public static final String BASE_URL = "https://api.trakt.tv/";
    public static final String LIMIT = "10";
    public static final String IMAGES = "images";


    private ListAdapter mAdapter;
    private ExpandableListView mListview;
    private int scrollCounter = 0;


    private int mLastFirstVisibleItem;
    private boolean mIsScrollingUp;


    private Subscription movieCallSubscription;
    private Subscription searchSubscription;
    private Subscription editTextSub;
    private Observable<List<Movie>> movieCall;
    private Observable<List<SearchResult>> movieCall2;

    private List<Movie> movies;

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

//        movieCallSubscription = movieCall.subscribe();


        mListview = (ExpandableListView) findViewById(R.id.listView);
        mAdapter = new ListAdapter(this);

        mListview.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                // load moare
                Log.d("MainActivity", "first visible item: " + firstVisibleItem);
                Log.d("MainActivity", "last visible position: " + mListview.getLastVisiblePosition());


                if (view.getId() == mListview.getId()) {
                    final int currentFirstVisibleItem = mListview.getFirstVisiblePosition();

                    if (currentFirstVisibleItem > mLastFirstVisibleItem) {
                        mIsScrollingUp = false;
                    } else if (currentFirstVisibleItem < mLastFirstVisibleItem) {
                        mIsScrollingUp = true;
                    }

                    mLastFirstVisibleItem = currentFirstVisibleItem;
                }


                //load data only when scrolling down
                if (mListview.getLastVisiblePosition() % 9 == 0 && firstVisibleItem != 0 && !mIsScrollingUp) {
                    //copy old movies to new array


                    if (mLastFirstVisibleItem != mListview.getLastVisiblePosition()) {
                        scrollCounter++;
                        loadMoviesPopMovies(scrollCounter);
                        mLastFirstVisibleItem = mListview.getLastVisiblePosition();
                    }


                }


            }
        });


//

        editTextSub = RxTextView.textChanges(editText)
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<CharSequence>() {
                    @Override
                    public void call(CharSequence value) {
                        // do some work with new text

                        Log.d("MainActivity", "subscription: " + value);
                        if (value.length() > 3) {

                            loadSearchedMovies(1, value.toString());
                        }
                    }
                });


    }

    private void loadSearchedMovies(int position, String squence) {

        String pos = Integer.toString(position);
        movies = new ArrayList<>();

        movieCall2 = apiService.getSearchedMovies(IMAGES, squence);
        movieCallSubscription = movieCall2.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<List<SearchResult>>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                Log.d("MainActivity", "error: " + e.getMessage());
            }


            @Override
            public void onNext(List<SearchResult> results) {

                if (mListview.getAdapter() == null) {
                    mListview.setAdapter(mAdapter);
                }

                Log.d("MainActivity", "movie size: " + results.size());


                for (int i = 0; i < results.size(); i++) {
                    movies.add(results.get(i).getMovie());
                }
                mAdapter.setMovies(movies);
                mAdapter.notifyDataSetChanged();
                Log.d("MainActivity", "searchMovies");

            }
        });


    }

    private void loadMoviesPopMovies(int position) {

        String pos = Integer.toString(position);

        movieCall = apiService.get10PopularMovies(IMAGES, pos, LIMIT);
        searchSubscription = movieCall.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<List<Movie>>() {
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
                Log.d("MainActivity", "onNExt movies size " + mAdapter.getMovies().size());

            }
        });


    }


    @Override
    protected void onDestroy() {
        movieCallSubscription.unsubscribe();
        editTextSub.unsubscribe();
        super.onDestroy();
    }

    public interface TrakTvEndpointInterface {


        @Headers({"Content-Type: application/json", "trakt-api-key: ad005b8c117cdeee58a1bdb7089ea31386cd489b21e14b19818c91511f12a086", "trakt-api-version: 2"})

        @GET("movies/popular")
        Observable<List<Movie>> get10PopularMovies(@Query("extended") String extendedValues, @Query("page") String page, @Query("limit") String limit);


        @Headers({"Content-Type: application/json", "trakt-api-key: ad005b8c117cdeee58a1bdb7089ea31386cd489b21e14b19818c91511f12a086", "trakt-api-version: 2"})
        @GET("search/movie")
        Observable<List<SearchResult>> getSearchedMovies(
                @Query("extended") String extendedValues,
                @Query("query") String query);


    }
}
