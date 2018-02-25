package mobi.quamotion.cloud.models;

import com.google.gson.annotations.SerializedName;

import java.util.UUID;

/**
 * Created by BartSaintGermain on 2/23/2018.
 */
public class TestRun
{
    //,"scheduleId":null,"cronSchedule":null,"jobs":[]}

    @SerializedName("testRunId")
    public UUID testRunId;

    @SerializedName("deviceGroup")
    public UUID deviceGroupId;

    @SerializedName("operatingSystem")
    public int operatingSystem;

    @SerializedName("appId")
    public String applicationId;

    @SerializedName("appVersion")
    public String applicationVersion;

    @SerializedName("testPackageName")
    public String testPackageName;

    @SerializedName("testPackageVersion")
    public String testPackageVersion;

    @SerializedName("scheduled")
    public String scheduled;

    @SerializedName("commitId")
    public String commitId;

    @SerializedName("branch")
    public String branch;

    @SerializedName("scheduleId")
    public Integer scheduleId;

    @SerializedName("cronSchedule")
    public String cronSchedule;

    @SerializedName("jobs")
    public TestJob[] jobs;
}
