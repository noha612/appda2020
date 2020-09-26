package edu.ptit.vn.appda2020;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FindLocationActivity extends AppCompatActivity {
    private static final int TRIGGER_AUTO_COMPLETE = 100;
    private static final long AUTO_COMPLETE_DELAY = 300;
    private Handler handler;
    private AutoSuggestAdapter autoSuggestAdapter;
    AutoCompleteTextView input;
    String[] stringList;
    OkHttpClient client = new OkHttpClient();
    String searchResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_location);

        input = findViewById(R.id.input);
        autoSuggestAdapter = new AutoSuggestAdapter(this,
                android.R.layout.simple_dropdown_item_1line);
        input.setThreshold(2);
        input.setAdapter(autoSuggestAdapter);

        //typing listener
        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    input.clearFocus();
                    InputMethodManager in = (InputMethodManager) FindLocationActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                    in.hideSoftInputFromWindow(input.getWindowToken(), 0);
                    if (TextUtils.isEmpty(input.getText())) {
                        Toast.makeText(FindLocationActivity.this, "Bạn chưa nhập gì!", Toast.LENGTH_SHORT).show();
                    } else {
                        getId(true);
                    }
                    return true;
                }
                return false;
            }
        });

        //item click listener
        input.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                searchResult = stringList[i];
                Intent intent = new Intent();
                intent.putExtra("s", searchResult);
                setResult(getIntent().getIntExtra("requestCode", 0), intent);
                finish();
            }
        });

        //handler
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int
                    count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                handler.removeMessages(TRIGGER_AUTO_COMPLETE);
                handler.sendEmptyMessageDelayed(TRIGGER_AUTO_COMPLETE,
                        AUTO_COMPLETE_DELAY);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == TRIGGER_AUTO_COMPLETE) {
                    if (!TextUtils.isEmpty(input.getText())) {
                        getId(false);
                    }
                }
                return false;
            }
        });
    }

    //call api get locations
    private void getId(final boolean isSearchIconClick) {
        HttpUrl.Builder httpBuilder = HttpUrl.parse(getString(R.string.server_uri) + getString(R.string.api_locations)).newBuilder();
        httpBuilder.addQueryParameter("name", input.getText().toString());
        Request request = new Request.Builder().get()
                .url(httpBuilder.build())
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("Error", "Network Error" + e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String json = response.body().string();
                final Location[] locations = new ObjectMapper().readValue(json, Location[].class);
                ArrayList<String> names = new ArrayList<>();
                for (Location location : locations) names.add(location.getName());
                stringList = new String[names.size()];
                stringList = names.toArray(stringList);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (isSearchIconClick) {
                            searchResult = stringList[0];
                            input.setText(searchResult);
                        } else {
                            autoSuggestAdapter.setData(Arrays.asList(stringList));
                            autoSuggestAdapter.notifyDataSetChanged();
                        }
                    }
                });
            }
        });
    }
}