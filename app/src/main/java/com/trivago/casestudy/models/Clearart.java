package com.trivago.casestudy.models;

/**
 * Created by Matthias on 16.08.16 at 12:57.
 */
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class Clearart {

    @SerializedName("full")
    @Expose
    private String full;

    /**
     *
     * @return
     * The full
     */
    public String getFull() {
        return full;
    }

    /**
     *
     * @param full
     * The full
     */
    public void setFull(String full) {
        this.full = full;
    }

}
