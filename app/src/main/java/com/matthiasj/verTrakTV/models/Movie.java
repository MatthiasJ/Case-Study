package com.matthiasj.verTrakTV.models;

/**
 * Created by Matthias on 16.08.16 at 13:01.
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class Movie {

    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("year")
    @Expose
    private String year;
    @SerializedName("ids")
    @Expose
    private Ids ids;
    @SerializedName("images")
    @Expose
    private Images images;
    @SerializedName("overview")
    @Expose
    private String overview;

    /**
     * @return The Overview
     */
    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    /**
     * @return The title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title The title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return The year
     */
    public String getYear() {
        return year;
    }

    /**
     * @param year The year
     */
    public void setYear(String year) {
        this.year = year;
    }

    /**
     * @return The ids
     */
    public Ids getIds() {
        return ids;
    }

    /**
     * @param ids The ids
     */
    public void setIds(Ids ids) {
        this.ids = ids;
    }

    /**
     * @return The images
     */
    public Images getImages() {
        return images;
    }

    /**
     * @param images The images
     */
    public void setImages(Images images) {
        this.images = images;
    }

}