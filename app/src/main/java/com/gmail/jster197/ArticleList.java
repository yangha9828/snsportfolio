package com.gmail.jster197;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
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

public class ArticleList extends AppCompatActivity {
    Button logout;
    Button write;
    Button friendlist;

    ListView listview;

    ArrayList<Article> list;
    ArrayAdapter<Article> adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        setTitle(Session.name);

        write = (Button)findViewById(R.id.write);
        write.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View view){
                Intent intent = new Intent(ArticleList.this, ArticleInsert.class);
                startActivity(intent);
            }
        });

        friendlist = (Button)findViewById(R.id.findlist);
        friendlist.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View view){
                Intent intent = new Intent(ArticleList.this, FriendList.class);
                startActivity(intent);
            }
        });

        logout = (Button)findViewById(R.id.logout);
        logout.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View view){
                Session.id = null;
                Session.name = null;
                finish();
            }
        });

        listview = (ListView)findViewById(R.id.listview);
    }

    public void onResume(){
        super.onResume();

        list = new ArrayList<>();

        final Handler listHandler = new Handler(){
            @Override
            public void handleMessage(Message message){
                if(list.size() == 0){
                   Toast.makeText(ArticleList.this, "출력할 게시글이 없습니다.", Toast.LENGTH_LONG).show();

                }else{
                    //Toast.makeText(List.this, "" + list.size(), Toast.LENGTH_LONG).show();
                    //데이터를 ListView에 출력할 수 있도록 Adapter에 주입
                    MyAdapter adapter = new MyAdapter(
                            ArticleList.this, list, R.layout.content, "게시글");
                    listview.setAdapter(adapter);
                }
            }
        };

        Thread th = new Thread(){
            public void run(){
                StringBuilder output = new StringBuilder();
                try {
                    String addr = Common.server + "articlelist.jsp?id=" + Session.id;

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
                            Log.e("받아온 데이터", output.toString().trim());

                            JSONArray jarray = new JSONArray(output.toString().trim());
                            for (int i = 0; i < jarray.length(); i++) {
                                Article article = new Article();
                                JSONObject jObject = jarray.getJSONObject(i);

                                article.id = jObject.optString("id");
                                article.num = Integer.parseInt(jObject.optString("num"));
                                article.regdate = jObject.optString("regdate");
                                article.content = jObject.optString("content");
                                article.dislike = Integer.parseInt(jObject.optString("dislike"));
                                article.good = Integer.parseInt(jObject.optString("good"));
                                Log.e("내용",article.content);
                                list.add(article);
                            }
                            Collections.sort(list);

                        }
                        conn.disconnect();
                        listHandler.sendEmptyMessage(0);
                    }
                }catch (Exception e){
                    Log.e("로그인 예외", e.getMessage());
                }
            }
        };
        th.start();
    }
}
