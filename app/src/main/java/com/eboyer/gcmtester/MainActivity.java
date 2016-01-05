package com.eboyer.gcmtester;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private final String GCM_URL="https://gcm-http.googleapis.com/gcm/send";

    private final String SETTING_API_KEY="SETTING_API_KEY";
    private final String SETTING_TOKEN="SETTING_TOKEN";
    private final String SETTING_MESSAGE="SETTING_MESSAGE";

    EditText mEditTextAPIKey;
    EditText mEditTextToken;
    EditText mEditTextJSON;

    Button mSendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        mEditTextAPIKey=(EditText)findViewById(R.id.editTextAPIKey);
        mEditTextAPIKey.setText(sharedPreferences.getString(SETTING_API_KEY,""));

        mEditTextToken=(EditText)findViewById(R.id.editTextToken);
        mEditTextToken.setText(sharedPreferences.getString(SETTING_TOKEN,""));

        mEditTextJSON=(EditText)findViewById(R.id.editTextJSON);
        mEditTextJSON.setText(sharedPreferences.getString(SETTING_MESSAGE,getString(R.string.message_default)));

        mSendButton=(Button)findViewById(R.id.buttonSend);
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendGCM();
            }
        });
    }

    public void sendGCM() {

        final TextView textViewResponse=(TextView)findViewById(R.id.textViewResponse);
        textViewResponse.setText("");

        final String apiKey = ((EditText)findViewById(R.id.editTextAPIKey)).getText().toString();
        final String token = ((EditText)findViewById(R.id.editTextToken)).getText().toString();
        final String jsonMessage = ((EditText)findViewById(R.id.editTextJSON)).getText().toString();

        if (apiKey==null || token==null || jsonMessage==null || apiKey.length()==0 || token.length()==0 || jsonMessage.length()==0 ) {
            Toast.makeText(MainActivity.this, "All fields requires", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject gcmJSON=createBody(token, jsonMessage);
        if (gcmJSON==null) {
            Toast.makeText(MainActivity.this, "Invalid JSON", Toast.LENGTH_SHORT).show();
        }

        JsonObjectRequest JsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                GCM_URL, gcmJSON,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        mSendButton.setEnabled(true);
                        textViewResponse.setText(response.toString());
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mSendButton.setEnabled(true);
                textViewResponse.setText(error.toString());
            }
        }) {
            @Override
            public Map<String, String> getHeaders()  {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");
                headers.put( "Authorization","key="+apiKey);
                return headers;
            }
        };
        mSendButton.setEnabled(false);
        VolleySingleton.getInstance(this).addToRequestQueue(JsonObjectRequest);
    }

    private JSONObject createBody(String token, String jsonMessage) {
        JSONObject gcmJSON=null;
        try {
            String gcmBody =
                    "{" +
                            "\"registration_ids\" : [\""+token+"\"]," +
                            "\"data\" : {" +
                            jsonMessage +
                            "}" +
                            "}";
            gcmJSON = new JSONObject(gcmBody);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return gcmJSON;
    }

    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String apiKey=mEditTextAPIKey.getText().toString();
        if (apiKey==null || apiKey.length()==0) {
            sharedPreferences.edit().remove(SETTING_API_KEY).apply();
        } else {
            sharedPreferences.edit().putString(SETTING_API_KEY, apiKey).apply();
        }

        String token=mEditTextToken.getText().toString();
        if (token==null || token.length()==0) {
            sharedPreferences.edit().remove(SETTING_TOKEN).apply();
        } else {
            sharedPreferences.edit().putString(SETTING_TOKEN, token).apply();
        }

        String message=mEditTextJSON.getText().toString();
        if (message==null || message.length()==0) {
            sharedPreferences.edit().remove(SETTING_MESSAGE).apply();
        } else {
            sharedPreferences.edit().putString(SETTING_MESSAGE, message).apply();
        }
        sharedPreferences.edit().commit();
    }
}
