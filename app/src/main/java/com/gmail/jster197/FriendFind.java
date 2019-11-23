package com.gmail.jster197;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;

public class FriendFind extends AppCompatActivity {
    Button back;
    Button find;
    Button add;
    EditText search;
    ListView listView;
    ArrayList<String> list;
    ArrayAdapter<String> adapter;

    String friendlist = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend);

        back = (Button)findViewById(R.id.back);
        back.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View view){
                finish();
            }
        });

        listView = (ListView)findViewById(R.id.listview);
        search = (EditText)findViewById(R.id.search);
        add = (Button)findViewById(R.id.add);

        final Handler addHandler = new Handler(){
          public void handleMessage(Message msg){
              Toast.makeText(FriendFind.this, "친구 추가 성공", Toast.LENGTH_LONG).show();
              finish();
          }
        };
        add.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                Thread th = new Thread(){
                    public void run(){
                        StringBuilder output = new StringBuilder();
                        try {

                            SparseBooleanArray sb = listView.getCheckedItemPositions();
                            friendlist = "";
                            if (sb.size() != 0) {
                                for (int i = listView.getCount() - 1; i >= 0 ; i--) {
                                    if (sb.get(i)) {
                                        friendlist = friendlist + list.get(i) + ",";
                                        list.remove(i);
                                    }
                                }
                            }

                            String addr = Common.server+ "friendinsert.jsp?id=" + Session.id;
                            friendlist = friendlist.substring(0, friendlist.length() - 1);
                            addr = addr + "&friend=" + friendlist;
                            URL url = new URL(addr);
                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                            if (conn != null) {
                                conn.setConnectTimeout(10000);
                                conn.setUseCaches(false);

                                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                                    BufferedReader br = new BufferedReader(
                                            new InputStreamReader(conn.getInputStream()));
                                    while (true) {
                                        String line = br.readLine();
                                        if (line == null) break;
                                        output.append(line + '\n');
                                    }
                                    br.close();
                                }
                                conn.disconnect();
                                addHandler.sendEmptyMessage(0);
                            }
                        }catch (Exception e){
                            Log.e("파싱 예외", e.getMessage());
                        }
                    }
                };
                th.start();
            }
        });

        find = (Button)findViewById(R.id.find);

        find.setOnClickListener(new View.OnClickListener(){

            public void onClick(View v){
                final Handler listHandler = new Handler(){
                    @Override
                    public void handleMessage(Message message){

                        adapter = new ArrayAdapter<String>(FriendFind.this, android.R.layout.
                                simple_list_item_multiple_choice, list);
                        listView.setAdapter(adapter);
                        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                    }
                };



                Thread th = new Thread(){
                    public void run(){
                        StringBuilder output = new StringBuilder();
                        try {
                            String addr = Common.server+ "findfriend.jsp?id=" + search.getText().toString().trim();

                            URL url = new URL(addr);
                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                            if (conn != null) {
                                conn.setConnectTimeout(10000);
                                conn.setUseCaches(false);

                                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                                    BufferedReader br = new BufferedReader(
                                            new InputStreamReader(conn.getInputStream()));
                                    while (true) {
                                        String line = br.readLine();
                                        if (line == null) break;
                                        output.append(line + '\n');
                                    }
                                    br.close();

                                    list = new ArrayList<>();
                                    JSONArray jarray = new JSONArray(output.toString().trim());
                                    for (int i = 0; i < jarray.length(); i++) {
                                        list.add(jarray.getString(i));
                                    }
                                    Collections.sort(list);

                                }
                                conn.disconnect();
                                listHandler.sendEmptyMessage(0);
                            }
                        }catch (Exception e){
                            Log.e("파싱 예외", e.getMessage());
                        }
                    }
                };
                th.start();
            }
        });

    }

    @Override
    public void onResume(){
        super.onResume();

        list = new ArrayList<>();

        final Handler listHandler = new Handler(){
            @Override
            public void handleMessage(Message message){

                adapter = new ArrayAdapter<String>(FriendFind.this, android.R.layout.
                        simple_list_item_multiple_choice, list);
                listView.setAdapter(adapter);
                listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                }
            };



        Thread th = new Thread(){
            public void run(){
                StringBuilder output = new StringBuilder();
                try {
                    String addr = Common.server + "findlist.jsp?id=" + Session.id;

                    URL url = new URL(addr);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    if (conn != null) {
                        conn.setConnectTimeout(10000);
                        conn.setUseCaches(false);

                        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                            BufferedReader br = new BufferedReader(
                                    new InputStreamReader(conn.getInputStream()));
                            while (true) {
                                String line = br.readLine();
                                if (line == null) break;
                                output.append(line + '\n');
                            }
                            br.close();


                            JSONArray jarray = new JSONArray(output.toString().trim());
                            for (int i = 0; i < jarray.length(); i++) {
                                JSONObject item = jarray.getJSONObject(i);
                                list.add(item.getString("id"));
                            }
                            Collections.sort(list);

                        }
                        conn.disconnect();
                        listHandler.sendEmptyMessage(0);
                    }
                }catch (Exception e){
                    Log.e("파싱 예외", e.getMessage());
                }
            }
        };
        th.start();

    }
}
