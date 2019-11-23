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
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class PasswordFind extends AppCompatActivity {
    EditText findid;
    Button select;
    Button back;

    String result;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passwordfind);

        findid = (EditText)findViewById(R.id.findid);

        back = (Button)findViewById(R.id.back);
        back.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View view){
               finish();
            }
        });

        select = (Button)findViewById(R.id.select);
        select.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View view){
                final String id = findid.getText().toString();
                if(id.trim().length() < 1){
                    Toast.makeText(PasswordFind.this, "아이디는 필수 입력입니다.", Toast.LENGTH_LONG).show();
                    return;
                }

                final Handler passwordSendHandler = new Handler(){
                    @Override
                    public void handleMessage(Message message){
                        if(result.trim().equals("fail") == true){
                            Toast.makeText(PasswordFind.this, "패스워드 생성에 실패했습니다. 잠시후에 다시 시도하세요!!", Toast.LENGTH_LONG).show();
                        }else if(result.trim().equals("success") == true){
                            Toast.makeText(PasswordFind.this, "패스워드 생성에 성공했습니다. 잠시후에 메일을 확인하세요!!", Toast.LENGTH_LONG).show();
                            finish();
                        }else{
                            Toast.makeText(PasswordFind.this, result.trim(), Toast.LENGTH_LONG).show();
                        }
                    }
                };

                Thread th = new Thread(){
                    public void run(){
                        StringBuilder output = new StringBuilder();
                        try {
                            String addr = Common.server + "pwsend.jsp?id=" + id;

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
                                passwordSendHandler.sendEmptyMessage(0);
                            }
                        }catch (Exception e){
                            Log.e("로그인 예외", e.getMessage());
                        }
                    }
                };
                th.start();
            }
        });
    }
}
