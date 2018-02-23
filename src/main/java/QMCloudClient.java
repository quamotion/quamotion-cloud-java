import com.google.gson.Gson;
import models.*;
import okhttp3.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents the
 */
public class QMCloudClient
{
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public static final MediaType APPLICATION = MediaType.parse("application/octet-stream");


    private OkHttpClient client = new OkHttpClient();
    private Gson gson = new Gson();

    private String host = "https://cloud.quamotion.mobi";
    private String accessToken = null;


    public QMCloudClient(String host)
    {
        this.host = host;
    }

    public QMCloudClient()
    {
    }

    public void login(String apiKey) throws IOException {
        HashMap<String, String> loginData = new HashMap<String, String>();
        loginData.put("ApiKey", apiKey);
        String response = this.postFormRequest("/api/login", loginData);
        LoginResponse loginResponse = gson.fromJson(response, LoginResponse.class);
        this.accessToken = loginResponse.accessToken;
    }

    public List<Tenant> getProjects() throws IOException {
        String response = this.getRequest("/api/project");
        return this.gson.fromJson(response, ProjectResponse.class);
    }

    public List<TestPackage> publishTestPackage(Tenant tenant, String filePath) throws IOException {
        String response = this.sendFile("/project/"+ tenant.name + "/api/testPackage", filePath);
        return this.gson.fromJson(response, TestPackageResponse.class);
    }

    public List<Application> publishApplication(Tenant tenant, String filePath) throws IOException {
        String response = this.sendFile("/project/"+ tenant.name + "/api/app", filePath);
        return this.gson.fromJson(response, ApplicationResponse.class);
    }

    public List<Application> getApplications(Tenant tenant) throws IOException {
        String response = this.getRequest("/project/"+ tenant.name +"/api/app");
        return this.gson.fromJson(response, ApplicationResponse.class);
    }

    public List<TestPackage> getTestPackages(Tenant tenant) throws IOException {
        String response = this.getRequest("/project/"+ tenant.name +"/api/testPackage");
        return this.gson.fromJson(response, TestPackageResponse.class);
    }

    public List<DeviceGroup> getDeviceGroups(Tenant tenant) throws IOException {
        String response = this.getRequest("/project/"+ tenant.name +"/api/deviceGroup");
        return this.gson.fromJson(response, DeviceGroupResponse.class);
    }

    public DeviceGroup getDeviceGroup(Tenant tenant, String deviceGroupId) throws IOException {
        List<DeviceGroup> deviceGroups = this.getDeviceGroups(tenant);
        DeviceGroup result = null;
        for (DeviceGroup deviceGroup : deviceGroups)
        {
            if(deviceGroup.deviceGroupId.toString().equals(deviceGroupId))
            {
                result = deviceGroup;
            }
        }

        return result;
    }

    public Application getApplication(Tenant tenant, String applicationId, String applicationVersion, String operatingSystem) throws IOException {
        List<Application> applications = this.getApplications(tenant);
        Application result = null;
        for (Application application : applications)
        {
            if(application.appId.equals(applicationId) && application.version.equals(applicationVersion) && application.operatingSystem.equals(operatingSystem))
            {
                result = application;
            }
        }

        return result;
    }

    public TestPackage getTestPackage(Tenant tenant, String name, String version) throws IOException {
        List<TestPackage> testPackages = this.getTestPackages(tenant);
        TestPackage result = null;
        for (TestPackage testPackage : testPackages)
        {
            if(testPackage.name.equals(name) && testPackage.version.equals(version))
            {
                result = testPackage;
            }
        }

        return result;
    }

    public TestRun scheduleTestRun(Tenant tenant, TestPackage testPackage, Application application, DeviceGroup deviceGroup) throws IOException {
        CreateTestRunRequest createTestRunRequest = new CreateTestRunRequest();
        createTestRunRequest.app = application;
        createTestRunRequest.deviceGroupId = deviceGroup.deviceGroupId.toString();
        createTestRunRequest.schedule ="";
        createTestRunRequest.testPackage = testPackage;
        createTestRunRequest.testScriptEnvironmentVariables = new HashMap<String, String>();
        createTestRunRequest.testScriptParameters = "";

        String response = this.postJsonRequest("/project/"+ tenant.name +"/api/testRun", createTestRunRequest);

        return this.gson.fromJson(response, TestRun.class);
    }

    public ArrayList<TestJob> getTestJobs(Tenant tenant, TestRun testRun)throws IOException {
        String response = this.getRequest("/project/"+ tenant.name +"/api/testRun/"+ testRun.testRunId +"/jobs");
        return this.gson.fromJson(response, TestJobResponse.class);
    }

    public String getJobLogLive(Tenant tenant, TestJob testJob)throws IOException {
        return this.getRequest("/project/"+ tenant.name +"/api/job/"+ testJob.id +"/log/live");
    }

    private void ensureLogin()
    {
        if(this.accessToken == null)
        {
            throw new IllegalStateException("You are not logged in. Please log in and try again");
        }
    }

    private void ensureSuccess(Response response) throws IOException {
        if (!response.isSuccessful()) throw new IOException(response.body().string());
    }

    private  String sendFile(String relativeUrl, String filePath) throws IOException {
        this.ensureLogin();

        String url = this.host+relativeUrl;
        File file = new File(filePath);

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("files", file.getName(),
                        RequestBody.create(APPLICATION, file))
                .build();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization","Bearer " + this.accessToken)
                .addHeader("Accept", "application/json")
                .post(requestBody)
                .build();

        Response response = client.newCall(request).execute();
        this.ensureSuccess(response);

        return response.body().string();
    }

    private String getRequest(String relativeUrl) throws IOException {
        this.ensureLogin();

        String url = this.host+relativeUrl;
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization","Bearer " + this.accessToken)
                .build();

        Response response = client.newCall(request).execute();
        this.ensureSuccess(response);

        return response.body().string();
    }

    private String postFormRequest(String relativeUrl, HashMap<String,String> form) throws IOException {
        String url = this.host+relativeUrl;

        FormBody.Builder formBodyBuilder = new FormBody.Builder();
        for(Map.Entry<String, String> entry: form.entrySet())
        {
            formBodyBuilder.add(entry.getKey(), entry.getValue());
        }

        FormBody formBody = formBodyBuilder.build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();
        Response response = client.newCall(request).execute();
        this.ensureSuccess(response);

        return response.body().string();
    }

    private String postJsonRequest(String relativeUrl, Object object) throws IOException {
        this.ensureLogin();

        String url = this.host+relativeUrl;
        String json = this.gson.toJson(object);
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Authorization","Bearer " + this.accessToken)
                .build();
        Response response = client.newCall(request).execute();
        this.ensureSuccess(response);

        return response.body().string();
    }
}
