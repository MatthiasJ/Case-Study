package com.trivago.casestudy;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.EditText;

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
    private RecycleViewAdapter mAdapter;


    // behaviour and helper fields
    private int scrollCounter = 1;
    private boolean searchMode;
    private boolean searchedBefore;
    private String tempSearchTerm;


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


        // handle editText changes


//        filter(new Func1<CharSequence, Boolean>() {
//                   @Override
//                   public Boolean call(CharSequence charSequence) {
//                       return charSequence.length()>2;
//                   }
//               }

        editTextSub = RxTextView.textChanges(editText).debounce(100, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<CharSequence>() {

                    @Override
                    public void call(CharSequence value) {

                        if (mAdapter.movies != null) {
                            tempSearchTerm = value.toString();
                            if (tempSearchTerm.length() > 2) {

                                searchMode = true;

                                mAdapter.movies.clear();
                                mAdapter.notifyDataSetChanged();

                                // possible bug, might be handled differently: possibly by filtering with rxjava
                                scrollCounter = 1;
                                loadMovies(scrollCounter, tempSearchTerm);
                            } else {
                                searchMode = false;
                                // bug: loads new list of images, even scrollCounter stays the same


                                mAdapter.movies.clear();
                                scrollCounter=1;
                                mAdapter.notifyDataSetChanged();
                                loadMovies(scrollCounter);
                            }
                        }else{
                            // if adapters movies null: get 10 popular items
                            loadMovies(scrollCounter);
                        }
                    }
                });


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
        Observable<List<Movie>> getPopularMovies(@Query("extended") String extendedValues, @Query("page") int page, @Query("limit") int limit);


        @Headers({"Content-Type: application/json", "trakt-api-key: ad005b8c117cdeee58a1bdb7089ea31386cd489b21e14b19818c91511f12a086", "trakt-api-version: 2"})
        @GET("search/movie")
        Observable<List<SearchResult>> searchForMovies(@Query("extended") String extendedValues, @Query("query") String query, @Query("page") int page, @Query("limit") int limit);


    }
}
