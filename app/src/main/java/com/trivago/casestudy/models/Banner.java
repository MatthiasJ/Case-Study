package com.trivago.casestudy.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class Banner {

    @SerializedName("full")
    @Expose
    private String full;

    /**
     * @return The full
     */
    public String getFull() {
        return full;
    }

    /**
     * @param full The full
     */
    public void setFull(String full) {
        this.full = full;
    }

}