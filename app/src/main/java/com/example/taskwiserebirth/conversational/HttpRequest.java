package com.example.taskwiserebirth.conversational;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.taskwiserebirth.task.TaskModel;
import com.example.taskwiserebirth.utils.CalendarUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpRequest {

    private static final String SERVER_ADDRESS = "https://taskwise.michacaldaira.com/";  // add task link
    private static final String VICUNA_RESPONSE = "vicuna";
    private static final String ALL_INTENT = "all_intent";
    private static final String PRIMARY_SECONDARY_INTENT = "primary_secondary_intent";
    private static final String EDIT_TASK = "edit_intent";
    private static final String DELETE_TASK = "delete_intent";
    private static final String MARK_TASK = "mark_intent";
    private static final String GET_TASK_DETAIL = "task_detail";
    private static final String PROCESS_TASK_DETAIL = "process_task_detail";
    private static final String GET_TASK_NAME = "get_task_name";
    private static final String SECONDARY_INTENT = "secondary_intent";
    private static final String TAG_HTTP = "HttpRequest";


    public static void handleVicunaResponse(String aiName, TaskModel task, String systemAction, final HttpRequestCallback callback) {
        JSONObject requestBodyJson = new JSONObject();
        try {
            requestBodyJson.put("ai_name", aiName);
            requestBodyJson.put("task_name", task.getTaskName());
            requestBodyJson.put("system_action", systemAction);
        } catch (JSONException e) {
            Log.e(TAG_HTTP, e.getMessage());
        }

        String url = SERVER_ADDRESS + VICUNA_RESPONSE;
        sendHttpRequest(url, requestBodyJson, callback);
    }

    public static void handleAllIntent(String userMessage, String aiName, String userId, final HttpRequestCallback callback) {
        JSONObject requestBodyJson = new JSONObject();
        try {
            requestBodyJson.put("user_prompt", userMessage);
            requestBodyJson.put("ai_name", aiName);
            requestBodyJson.put("user_id", userId);
        } catch (JSONException e) {
            Log.e(TAG_HTTP, e.getMessage());
        }

        String url = SERVER_ADDRESS + ALL_INTENT;
        sendHttpRequest(url, requestBodyJson, callback);
    }

    public static void primarySecondaryRequest(String userMessage, String aiName, String userId, final HttpRequestCallback callback) {
        JSONObject requestBodyJson = new JSONObject();
        try {
            requestBodyJson.put("user_prompt", userMessage);
            requestBodyJson.put("ai_name", aiName);
            requestBodyJson.put("user_id", userId);
        } catch (JSONException e) {
            Log.e(TAG_HTTP, e.getMessage());
        }

        String url = SERVER_ADDRESS + PRIMARY_SECONDARY_INTENT;
        sendHttpRequest(url, requestBodyJson, callback);
    }

    public static void addRequest(String userMessage, String aiName, String userId, final HttpRequestCallback callback) {
        JSONObject requestBodyJson = new JSONObject();
        try {
            requestBodyJson.put("user_prompt", userMessage);
            requestBodyJson.put("ai_name", aiName);
            requestBodyJson.put("user_id", userId);
        } catch (JSONException e) {
            Log.e(TAG_HTTP, e.getMessage());
        }

        String url = SERVER_ADDRESS;
        sendHttpRequest(url, requestBodyJson, callback);
    }

    public static void editRequest(String userMessage, String aiName, String userId, final HttpRequestCallback callback) {
        JSONObject requestBodyJson = new JSONObject();
        try {
            requestBodyJson.put("user_prompt", userMessage);
            requestBodyJson.put("ai_name", aiName);
            requestBodyJson.put("user_id", userId);
        } catch (JSONException e) {
            Log.e(TAG_HTTP, e.getMessage());
        }

        String url = SERVER_ADDRESS + EDIT_TASK;
        sendHttpRequest(url, requestBodyJson, callback);
    }

    public static void deleteRequest(String userMessage, String aiName, String userId, final HttpRequestCallback callback) {
        JSONObject requestBodyJson = new JSONObject();
        try {
            requestBodyJson.put("user_prompt", userMessage);
            requestBodyJson.put("ai_name", aiName);
            requestBodyJson.put("user_id", userId);
        } catch (JSONException e) {
            Log.e(TAG_HTTP, e.getMessage());
        }

        String url = SERVER_ADDRESS + DELETE_TASK;
        sendHttpRequest(url, requestBodyJson, callback);
    }

    public static void markRequest(String userMessage, String aiName, String userId, final HttpRequestCallback callback) {
        JSONObject requestBodyJson = new JSONObject();
        try {
            requestBodyJson.put("user_prompt", userMessage);
            requestBodyJson.put("ai_name", aiName);
            requestBodyJson.put("user_id", userId);
        } catch (JSONException e) {
            Log.e(TAG_HTTP, e.getMessage());
        }

        String url = SERVER_ADDRESS + MARK_TASK;
        sendHttpRequest(url, requestBodyJson, callback);
    }

    public static void secondaryIntentReq(String userMessage, String aiName, String userId, final HttpRequestCallback callback) {
        JSONObject requestBodyJson = new JSONObject();
        try {
            requestBodyJson.put("user_prompt", userMessage);
            requestBodyJson.put("ai_name", aiName);
            requestBodyJson.put("user_id", userId);
        } catch (JSONException e) {
            Log.e(TAG_HTTP, e.getMessage());
        }

        String url = SERVER_ADDRESS + SECONDARY_INTENT;
        sendHttpRequest(url, requestBodyJson, callback);
    }

    public static void editTaskRequest(String userMessage, String aiName, String userId, final HttpRequestCallback callback) {
        JSONObject requestBodyJson = new JSONObject();
        try {
            requestBodyJson.put("user_prompt", userMessage);
            requestBodyJson.put("ai_name", aiName);
            requestBodyJson.put("user_id", userId);
        } catch (JSONException e) {
            Log.e(TAG_HTTP, e.getMessage());
        }

        String url = SERVER_ADDRESS + GET_TASK_DETAIL;
        sendHttpRequest(url, requestBodyJson, callback);
    }

    public static void requestTaskName(String userMessage, final HttpRequestCallback callback) {
        JSONObject requestBodyJson = new JSONObject();
        try {
            requestBodyJson.put("user_prompt", userMessage);
        } catch (JSONException e) {
            Log.e(TAG_HTTP, e.getMessage());
        }

        String url = SERVER_ADDRESS + GET_TASK_NAME;
        sendHttpRequest(url, requestBodyJson, callback);
    }

    public static void taskDetailRequest(TaskModel task, String userMessage, String aiName, final HttpRequestCallback callback) {
        JSONObject requestBodyJson = new JSONObject();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy | hh:mm a");
            String formattedCreationDate = sdf.format(task.getCreationDate());
            String formattedDateFinished = "null";
            String recurrence = task.getRecurrence();

            if (task.getDateFinished() != null) {
                formattedDateFinished  = sdf.format(task.getDateFinished());
            }
            if (!task.getRecurrence().equals("None") && !task.getRecurrence().equals("Daily")) {
                recurrence = CalendarUtils.convertRecurrenceToFullDayNames(task.getRecurrence());
            }

            requestBodyJson.put("task_name", task.getTaskName());
            requestBodyJson.put("importance_level", task.getImportanceLevel());
            requestBodyJson.put("urgency_level", task.getUrgencyLevel());
            requestBodyJson.put("deadline", task.getDeadline());
            requestBodyJson.put("schedule", task.getSchedule());
            requestBodyJson.put("recurrence", recurrence);
            requestBodyJson.put("reminder", task.isReminder());
            requestBodyJson.put("notes", task.getNotes());
            requestBodyJson.put("status", task.getStatus());
            requestBodyJson.put("date_finished", formattedDateFinished );
            requestBodyJson.put("creation_date", formattedCreationDate);

            requestBodyJson.put("user_prompt", userMessage);
            requestBodyJson.put("ai_name", aiName);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String url = SERVER_ADDRESS + PROCESS_TASK_DETAIL;
        sendHttpRequest(url, requestBodyJson, callback);
    }

    private static void sendHttpRequest(String url, JSONObject requestBodyJson, final HttpRequestCallback callback) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(requestBodyJson.toString(), JSON);

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                String errorMessage;
                try {
                    throw e;
                } catch (SocketTimeoutException timeoutException) {
                    errorMessage = "Request timed out";
                } catch (UnknownHostException unknownHostException) {
                    errorMessage = "Host not found";
                } catch (IOException ioException) {
                    errorMessage = "Request failed: " + e.getMessage();
                }

                Log.e("HttpRequest", errorMessage);
                callback.onFailure(errorMessage);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                try {
                    if (!response.isSuccessful()) {
                        if (response.code() == 404) {
                            callback.onFailure("Page not found");
                        } else if (response.code() == 502) {
                            callback.onFailure("Sorry, server is not available at the moment");
                        } else if (response.code() == 500) {
                            callback.onFailure("We apologize, server is still loading. Please try again after a few moments.");
                        } else {
                            callback.onFailure("Unexpected code: " + response.code());
                        }

                        Log.e("HttpRequest", "Unexpected code: " + response);
                        response.close();

                        return;
                    }

                    // Get the response body as a string
                    String responseBody = response.body().string();
                    response.close();

                    JSONObject jsonResponse = new JSONObject(responseBody);
                    String responseText = jsonResponse.getString("response");
                    String intent = jsonResponse.getString("intent");

                    callback.onSuccess(intent, responseText);

                } catch (IOException e) {
                    Log.e("HttpRequest", "Error reading response body", e);
                    callback.onFailure("Error reading response body" + e.getMessage());
                } catch (JSONException e) {
                    Log.e("HttpRequest", "JSON error: ", e);
                    callback.onFailure("JSON error: " + e.getMessage());
                }
            }
        });
    }

    public interface HttpRequestCallback {
        void onSuccess(String intent, String responseText);
        void onFailure(String errorMessage);
    }
}
