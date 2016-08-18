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
import java.util.concurrent.TimeUnit;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;


public class MainActivity extends AppCompatActivity {


    // api related fields
    public static final String BASE_URL = "https://api.trakt.tv/";
    public static final int LIMIT = 10;
    public static final String IMAGES = "images";


    // ui stufff
    private ListAdapter mAdapter;
    private ExpandableListView mListview;


    // behaviour and helper fields
    private int scrollCounter = 0;
    private int mLastFirstVisibleItem;
    private boolean mIsScrollingUp;


    // other
    private Subscription editTextSub;
    private List<Movie> movies;


    RxJavaCallAdapterFactory rxAdapter = RxJavaCallAdapterFactory.createWithScheduler(Schedulers.io());

    Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
            .create();

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(rxAdapter)
            .build();

    TrakTvEndpointInterface api = retrofit.create(TrakTvEndpointInterface.class);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final EditText editText = (EditText) findViewById(R.id.editText);


        mListview = (ExpandableListView) findViewById(R.id.listView);
        mAdapter = new ListAdapter(this);
        mListview.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                Log.d("MainActivity", "first visible item: " + firstVisibleItem);
                Log.d("MainActivity", "last visible position: " + mListview.getLastVisiblePosition());


                // determine scroll direction
                if (view.getId() == mListview.getId()) {
                    final int currentFirstVisibleItem = mListview.getFirstVisiblePosition();

                    if (currentFirstVisibleItem > mLastFirstVisibleItem) {
                        mIsScrollingUp = false;
                    } else if (currentFirstVisibleItem < mLastFirstVisibleItem) {
                        mIsScrollingUp = true;
                    }

                    mLastFirstVisibleItem = currentFirstVisibleItem;
                }


                //load data only when scrolling downwards
                if (mListview.getLastVisiblePosition() % 9 == 0 && firstVisibleItem != 0 && !mIsScrollingUp) {

                    if (mLastFirstVisibleItem != mListview.getLastVisiblePosition()) {
                        scrollCounter++;

                        if (editText.getText().toString().equals("")) {
                            loadMovies(scrollCounter);
                        } else {
                            loadMovies(scrollCounter, editText.getText().toString());

                        }
                        mLastFirstVisibleItem = mListview.getLastVisiblePosition();
                    }
                }
            }
        });



            // handle editText changes
        editTextSub = RxTextView.textChanges(editText)
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<CharSequence>() {
                    @Override
                    public void call(CharSequence value) {
                        if (value.length() > 3) {
                            loadMovies(1, value.toString());
                        }
                    }
                });


        loadMovies(scrollCounter);
    }


    private void loadMovies(int position, String searchTerm) {

        movies = new ArrayList<>();
        api.getSearchedMovies(IMAGES, searchTerm, position, LIMIT).debounce(100, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<List<SearchResult>>() {
            @Override
            public void call(List<SearchResult> results) {
                if (mListview.getAdapter() == null) {
                    mListview.setAdapter(mAdapter);
                }
                for (int i = 0; i < results.size(); i++) {
                    movies.add(results.get(i).getMovie());
                }
                mAdapter.setMovies(movies);
                mAdapter.notifyDataSetChanged();
            }
        });


    }


    private void loadMovies(int position) {

        api.get10PopularMovies(IMAGES, position, LIMIT).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<List<Movie>>() {
            @Override
            public void call(List<Movie> movies) {
                if (mListview.getAdapter() == null) {
                    mListview.setAdapter(mAdapter);
                }
                mAdapter.getMovies().addAll(movies);
                mAdapter.notifyDataSetChanged();
            }
        });


    }


    @Override
    protected void onDestroy() {
        editTextSub.unsubscribe();
        super.onDestroy();
    }

    public interface TrakTvEndpointInterface {


        @Headers({"Content-Type: application/json", "trakt-api-key: ad005b8c117cdeee58a1bdb7089ea31386cd489b21e14b19818c91511f12a086", "trakt-api-version: 2"})
        @GET("movies/popular")
        Observable<List<Movie>> get10PopularMovies(@Query("extended") String extendedValues, @Query("page") int page, @Query("limit") int limit);


        @Headers({"Content-Type: application/json", "trakt-api-key: ad005b8c117cdeee58a1bdb7089ea31386cd489b21e14b19818c91511f12a086", "trakt-api-version: 2"})
        @GET("search/movie")
        Observable<List<SearchResult>> getSearchedMovies(@Query("extended") String extendedValues, @Query("query") String query, @Query("page") int page, @Query("limit") int limit);


    }
}
