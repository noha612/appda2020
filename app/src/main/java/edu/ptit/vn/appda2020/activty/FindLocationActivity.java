package edu.ptit.vn.appda2020.activty;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

import edu.ptit.vn.appda2020.R;
import edu.ptit.vn.appda2020.adapter.AutoSuggestAdapter;
import edu.ptit.vn.appda2020.model.dto.Place;
import edu.ptit.vn.appda2020.retrofit.APIService;
import edu.ptit.vn.appda2020.retrofit.ApiUtils;
import edu.ptit.vn.appda2020.util.CommonUtils;

public class FindLocationActivity extends AppCompatActivity {
    private static final int TRIGGER_AUTO_COMPLETE = 100;
    private static final long AUTO_COMPLETE_DELAY = 300;
    AutoCompleteTextView input;
    CardView tap;
    CardView gps;
    Button btnBack;
    Button btnClear;
    Place[] places;
    String[] stringList;
    String searchResult;
    Gson gson = new Gson();
    Set<Place> listHis;
    ListView listHisLV;
    String[] names;
    APIService mAPIService;
    private Handler handler;
    private AutoSuggestAdapter autoSuggestAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_location);
        CommonUtils.setTranslucentStatus(this, true);
        CommonUtils.MIUISetStatusBarLightMode(this, true);

        initHistory();
        mAPIService = ApiUtils.getAPIService(this);

        input = findViewById(R.id.input);
        autoSuggestAdapter = new AutoSuggestAdapter(this, R.layout.simple_dropdown_item_2line);
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
                intent.putExtra("location", places[i]);
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
                        btnClear.setVisibility(View.VISIBLE);
                    } else {
                        btnClear.setVisibility(View.GONE);
                    }
                }
                return false;
            }
        });

        tap = findViewById(R.id.tap);
        tap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int resultCode = getIntent().getIntExtra("requestCode", 0) == 1 ? 1 : 2;
                setResult(resultCode);
                finish();
            }
        });

        gps = findViewById(R.id.gps);
        gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int resultCode = getIntent().getIntExtra("requestCode", 0) == 1 ? 11 : 22;
                setResult(resultCode);
                finish();
            }
        });

        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnClear = findViewById(R.id.btnClear);
        btnClear.setVisibility(View.GONE);
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                input.setText("");
                btnClear.setVisibility(View.GONE);
            }
        });
    }

    private void initHistory() {
        SharedPreferences sharedPreferences = getSharedPreferences("share", MODE_PRIVATE);
        String his = sharedPreferences.getString("his", null);
        if (his != null) {
            Type type = new TypeToken<Set<Place>>() {
            }.getType();
            listHis = gson.fromJson(his, type);

            listHisLV = findViewById(R.id.listHis);
            names = new String[listHis.size()];
            int i = listHis.size() - 1;
            for (Place place : listHis) {
                names[i] = place.getName();
                i--;
            }
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, names);
            listHisLV.setAdapter(arrayAdapter);
            listHisLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    for (Place place : listHis) {
                        if (names[i].equals(place.getName())) {
                            Intent intent = new Intent();
                            intent.putExtra("location", place);
                            setResult(getIntent().getIntExtra("requestCode", 0), intent);
                            finish();
                        }
                    }

                }
            });
        }
    }

    public void getId(final boolean isSearchIconClick) {
        Log.i("api", "call /da2020/v1/places : " + input.getText().toString());
        mAPIService.getPlaces(input.getText().toString()).enqueue(new retrofit2.Callback<Place[]>() {
            @Override
            public void onResponse(retrofit2.Call<Place[]> call, retrofit2.Response<Place[]> response) {

                if (response.isSuccessful()) {
                    Log.i("TAG", "post submitted to API." + response.toString());

                    try {
                        places = response.body();
                        ArrayList<String> names = new ArrayList<>();
                        for (Place place : places) names.add(place.getName());
                        stringList = new String[names.size()];
                        stringList = names.toArray(stringList);
                    } catch (Exception e) {
                        ArrayList<String> names = new ArrayList<>();
                        stringList = new String[names.size()];
                        stringList = names.toArray(stringList);
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (isSearchIconClick) {
                                searchResult = stringList[0];
                                Intent intent = new Intent();
                                intent.putExtra("location", places[0]);
                                setResult(getIntent().getIntExtra("requestCode", 0), intent);
                                finish();
                            } else {
                                autoSuggestAdapter.setData(Arrays.asList(places));
                                autoSuggestAdapter.notifyDataSetChanged();
                            }
                        }
                    });
                }

            }

            @Override
            public void onFailure(retrofit2.Call<Place[]> call, Throwable t) {

            }
        });
    }

}