package com.trivago.casestudy;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jakewharton.rxbinding.support.v7.widget.RxSearchView;
import com.jakewharton.rxbinding.support.v7.widget.SearchViewQueryTextEvent;
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


    // Retain Activity State
    private Parcelable state;

    // api related fields
    public static final String BASE_URL = "https://api.trakt.tv/";
    public static final String TRAKT_API_KEY = "ad005b8c117cdeee58a1bdb7089ea31386cd489b21e14b19818c91511f12a086";
    public static final int LIMIT = 10;
    public static final String IMAGES = "full,images";


    // ui stufff
    private RecycleViewAdapter mAdapter;
    private Toolbar mToolbar;
    private SearchView search;


    // behaviour and helper fields
    private int scrollCounter = 1;
    private boolean searchMode;
    private String tempSearchTerm;


    // other
    private Subscription searchSubscription;
    private List<Movie> movies;


    // GSON RXJava & Retrofit 2
    Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
            .create();
    RxJavaCallAdapterFactory rxAdapter = RxJavaCallAdapterFactory.createWithScheduler(Schedulers.io());

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

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);




        // duplicate code: TODO refactor
        if (isTablet()) {

            StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(3, 1);
            RecyclerView mListview = (RecyclerView) findViewById(R.id.recyclerView);

            mAdapter = new RecycleViewAdapter(this);
            mListview.setLayoutManager(layoutManager);
            mListview.setAdapter(mAdapter);


            mListview.addOnScrollListener(new EndlessRecyclerViewScrollListener(layoutManager) {
                @Override
                public void onLoadMore(int page, int totalItemsCount) {


                    if (mAdapter != null) {
                        scrollCounter++;

                        if (searchMode) {
                            loadMovies(scrollCounter, tempSearchTerm);
                        } else {
                            loadMovies(scrollCounter);
                        }
                    }
                }
            });
        } else {

            // duplicate code: TODO refactor
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            RecyclerView mListview = (RecyclerView) findViewById(R.id.recyclerView);

            mAdapter = new RecycleViewAdapter(this);
            mListview.setLayoutManager(layoutManager);
            mListview.setAdapter(mAdapter);


            mListview.addOnScrollListener(new EndlessRecyclerViewScrollListener(layoutManager) {
                @Override
                public void onLoadMore(int page, int totalItemsCount) {


                    if (mAdapter != null) {
                        scrollCounter++;

                        if (searchMode) {
                            loadMovies(scrollCounter, tempSearchTerm);
                        } else {
                            loadMovies(scrollCounter);
                        }
                    }
                }
            });

        }


    }


    private void loadMovies(int position, String searchTerm) {

        movies = new ArrayList<>();
        api.searchForMovies(IMAGES, searchTerm, position, LIMIT).debounce(200, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<List<SearchResult>>() {
            @Override
            public void call(List<SearchResult> results) {

                for (int i = 0; i < results.size(); i++) {
                    movies.add(results.get(i).getMovie());
                }
                mAdapter.movies.addAll(movies);
                mAdapter.notifyItemRangeChanged(mAdapter.movies.size() - movies.size(), movies.size());
            }

        });


    }


    private void loadMovies(int position) {

        movies = new ArrayList<>();

        api.getPopularMovies(IMAGES, position, LIMIT).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<List<Movie>>() {
            @Override
            public void call(List<Movie> list) {
                movies = list;
                // workaround for initial start
                if (mAdapter.movies == null || mAdapter.movies.size() == 0) {
                    mAdapter.movies = movies;
                    mAdapter.notifyDataSetChanged();
                } else
                    mAdapter.movies.addAll(movies);
                mAdapter.notifyItemRangeChanged(mAdapter.movies.size() - movies.size(), movies.size());

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mToolbar.inflateMenu(R.menu.menu);
        MenuItem searchViewItem = menu.findItem(R.id.menu_search_item);
        search = (SearchView) MenuItemCompat.getActionView(searchViewItem);
        searchSubscription = RxSearchView.queryTextChangeEvents(search).debounce(100, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<SearchViewQueryTextEvent>() {
            @Override
            public void call(SearchViewQueryTextEvent searchViewQueryTextEvent) {
                tempSearchTerm = searchViewQueryTextEvent.queryText().toString();
                if (mAdapter.movies != null) {
                    if (tempSearchTerm.length() > 2) {

                        searchMode = true;
                        scrollCounter = 1;

                        mAdapter.movies.clear();
                        mAdapter.notifyDataSetChanged();

                        loadMovies(scrollCounter, tempSearchTerm);
                    } else {
                        searchMode = false;
                        scrollCounter = 1;

                        mAdapter.movies.clear();
                        mAdapter.notifyDataSetChanged();

                        loadMovies(scrollCounter);
                    }
                } else {
                    // if adapters movies null: get popular movies
                    loadMovies(scrollCounter);
                }

                // hide softkeyboard
                    if(searchViewQueryTextEvent.isSubmitted()){
                        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                        search.clearFocus();
                    }

            }
        });



        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onDestroy() {
        searchSubscription.unsubscribe();
        super.onDestroy();
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
//        state = lay.onSaveInstanceState();
//        state.putParcelable(LIST_STATE_KEY, mListState);

    }

    public interface TrakTvEndpointInterface {


        @Headers({"Content-Type: application/json", "trakt-api-key: "+TRAKT_API_KEY, "trakt-api-version: 2"})
        @GET("movies/popular")
        Observable<List<Movie>> getPopularMovies(@Query("extended") String extendedValues, @Query("page") int page, @Query("limit") int limit);


        @Headers({"Content-Type: application/json", "trakt-api-key: "+TRAKT_API_KEY, "trakt-api-version: 2"})
        @GET("search/movie")
        Observable<List<SearchResult>> searchForMovies(@Query("extended") String extendedValues, @Query("query") String query, @Query("page") int page, @Query("limit") int limit);


    }

    public boolean isTablet() {
        return (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }
}
