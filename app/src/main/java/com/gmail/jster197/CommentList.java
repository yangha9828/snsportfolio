package com.gmail.jster197;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class CommentList extends AppCompatActivity {
    Button write;
    EditText sentence;
    Button back;

    int articleid;
    String result;

    ListView listview;
    ArrayList <Article> list;
    MyAdapter adapter;
    Article article;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commentlist);

        Intent intent = getIntent();
        articleid = intent.getIntExtra("articleid", 0);

        back = (Button)findViewById(R.id.back);
        sentence = (EditText)findViewById(R.id.sentence);
        listview = (ListView)findViewById(R.id.listview);

        back.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View view){
                String comment = sentence.getText().toString();

                finish();
            }
        });

        write = (Button)findViewById(R.id.write);
        write.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View view){
                String comment = sentence.getText().toString().trim();
                if(comment.trim().length() <= 0){
                    Toast.makeText(CommentList.this, "댓글은 필수 입력입니다.", Toast.LENGTH_LONG).show();
                    return;
                }

                final Handler registerHandler = new Handler(){
                    @Override
                    public void handleMessage(Message message){
                        if(result.trim().equals("success")){
                            Toast.makeText(CommentList.this, "댓글작성 성공", Toast.LENGTH_LONG).show();
                            list.add(0, article);
                            adapter.notifyDataSetChanged();
                            sentence.setText("");
                        }else{
                            Toast.makeText(CommentList.this, "댓글 실패", Toast.LENGTH_LONG).show();
                        }
                    }
                };

                Thread th = new Thread(){
                    public void run(){
                        StringBuilder output = new StringBuilder();
                        try {
                            String addr = Common.server + "commentinsert.jsp?";
                            addr = addr + "id=" + Session.id;
                            String comment = sentence.getText().toString();
                            addr = addr + "&comment=" + comment.trim();
                            addr = addr + "&artnum=" + articleid;

                            article = new Article();
                            article.dislike=0;
                            article.good = 0;
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd");
                            Date date = new Date();

                            article.regdate = sdf.format(date);
                            article.id = Session.id;
                            article.content = comment.trim();


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
                                    result = output.toString();
                                }
                                conn.disconnect();
                                registerHandler.sendEmptyMessage(0);
                            }
                        }catch (Exception e){
                            Log.e("회원 가입 예외", e.getMessage());
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
                Log.e("list", list.toString());
                if(list.size() == 0){
                    Toast.makeText(CommentList.this, "출력할 댓글이 없습니다.", Toast.LENGTH_LONG).show();
                    adapter = new MyAdapter(CommentList.this, list, R.layout.content,"댓글");
                    listview.setAdapter(adapter);

                }else{
                    //Toast.makeText(List.this, "" + list.size(), Toast.LENGTH_LONG).show();
                    //데이터를 ListView에 출력할 수 있도록 Adapter에 주입
                    adapter = new MyAdapter(CommentList.this, list, R.layout.content,"댓글");
                    listview.setAdapter(adapter);
                }
            }
        };


        Thread th = new Thread(){
            public void run(){
                StringBuilder output = new StringBuilder();
                try {
                    String addr = Common.server + "commentlist.jsp?artnum=" + articleid;

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
                                article.content = jObject.optString("comment");
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
                    Log.e("파싱 예외", e.getMessage());
                }
            }
        };
        th.start();
    }
}
