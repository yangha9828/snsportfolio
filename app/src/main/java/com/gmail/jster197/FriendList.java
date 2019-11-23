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

public class FriendList extends AppCompatActivity {
    Button back;
    Button findfrd;
    Button delete;
    ListView listview;

    ArrayList<String> list;
    ArrayAdapter<String>adapter;
    String friendlist;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friendlist);

        findfrd = (Button)findViewById(R.id.findfrd);
        findfrd.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View view){
                Intent intent = new Intent(FriendList.this, FriendFind.class);
                startActivity(intent);
            }
        });
        delete = (Button)findViewById(R.id.delete);
        delete.setOnClickListener(new Button.OnClickListener(){
            final Handler deleteHandler = new Handler(){
                @Override
                public void handleMessage(Message message){
                    adapter.notifyDataSetChanged();
                    Toast.makeText(FriendList.this,"삭제 성공", Toast.LENGTH_LONG).show();
                }
            };

            public void onClick(View view){
                Thread th = new Thread(){
                    public void run(){
                        StringBuilder output = new StringBuilder();
                        try {

                            SparseBooleanArray sb = listview.getCheckedItemPositions();
                            friendlist = "";
                            if (sb.size() != 0) {
                                for (int i = listview.getCount() - 1; i >= 0 ; i--) {
                                    if (sb.get(i)) {
                                        friendlist = friendlist + list.get(i) + ",";
                                        list.remove(i);
                                    }
                                }
                            }

                            String addr = Common.server+ "frienddelete.jsp?memberid=" + Session.id;
                            friendlist = friendlist.substring(0, friendlist.length() - 1);
                            addr = addr + "&friendid=" + friendlist;
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
                                deleteHandler.sendEmptyMessage(0);
                            }
                        }catch (Exception e){
                            Log.e("파싱 예외", e.getMessage());
                        }
                    }
                };
                th.start();
            }
        });

        back = (Button)findViewById(R.id.back);
        back.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View view){
                finish();
            }
        });

        listview = (ListView)findViewById(R.id.listview);
    }

    @Override
    public void onResume(){
        super.onResume();

        list = new ArrayList<>();

        final Handler listHandler = new Handler(){
            @Override
            public void handleMessage(Message message){


                    //Toast.makeText(List.this, "" + list.size(), Toast.LENGTH_LONG).show();
                    //데이터를 ListView에 출력할 수 있도록 Adapter에 주입
                    adapter = new ArrayAdapter<String>(FriendList.this, android.R.layout.
                            simple_list_item_multiple_choice, list);
                    listview.setAdapter(adapter);
                    listview.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            }
        };


        Thread th = new Thread(){
            public void run(){
                StringBuilder output = new StringBuilder();
                try {
                    String addr = Common.server + "friendlist.jsp?id=" + Session.id;

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
                                Article article = new Article();
                                JSONObject jObject = jarray.getJSONObject(i);

                                String id = jObject.optString("friendid");
                                list.add(id);
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
