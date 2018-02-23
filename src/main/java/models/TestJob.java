package models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by BartSaintGermain on 2/23/2018.
 */
public class TestJob
{
    @SerializedName("id")
    public Integer id;

    @SerializedName("name")
    public String name;

    @SerializedName("status")
    public String status;

    @SerializedName("duration")
    public double duration;

    @SerializedName("started")
    public String started;

    @SerializedName("commitId")
    public String commitId;

    @SerializedName("hasArtifacts")
    public Boolean hasArtifacts;
}
