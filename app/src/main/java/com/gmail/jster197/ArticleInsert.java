package com.gmail.jster197;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ArticleInsert extends AppCompatActivity {
    Button write;
    Button back;
    EditText sentence;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);

        sentence = (EditText)findViewById(R.id.sentence);

        write = (Button)findViewById(R.id.write);
        write.setOnClickListener(new Button.OnClickListener(){
            String result = "";
            public void onClick(View view){

                final String msg = sentence.getText().toString();
                if(msg.trim().length() < 1){
                    Toast.makeText(ArticleInsert.this, "내용은 필수 입력입니다.", Toast.LENGTH_LONG).show();
                    return;
                }

                final Handler insertHandler = new Handler(){
                    @Override
                    public void handleMessage(Message message){
                        if(result.trim().equals("success")){
                            Toast.makeText(ArticleInsert.this, Session.name + "님 게시글 작성 성공", Toast.LENGTH_LONG).show();
                            finish();
                        }else{
                            Toast.makeText(ArticleInsert.this, Session.name + "님 게시글 작성 실패", Toast.LENGTH_LONG).show();
                        }
                    }
                };

                Thread th = new Thread(){
                    public void run(){
                        StringBuilder output = new StringBuilder();
                        try {
                            String addr = Common.server + "articleinsert.jsp?";
                            addr = addr + "id=" + Session.id;
                            addr = addr + "&text=" + msg;

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
                                insertHandler.sendEmptyMessage(0);
                            }
                        }catch (Exception e){
                            Log.e("게시글 예외", e.getMessage());
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
    }
}
