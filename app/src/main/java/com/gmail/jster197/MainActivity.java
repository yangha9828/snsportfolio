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

public class MainActivity extends AppCompatActivity {
    EditText id;
    EditText password;
    Button login;
    Button register;
    Button findreg;

    String result;

    @Override
    public void onResume(){
        super.onResume();
        id.setText("");
        password.setText("");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        id = (EditText)findViewById(R.id.id);
        password = (EditText)findViewById(R.id.password);

        login = (Button)findViewById(R.id.login);


        login.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View view){
                final String ids = id.getText().toString();
                if(ids.trim().length() < 1){
                    Toast.makeText(MainActivity.this, "아이디는 필수 입력입니다.", Toast.LENGTH_LONG).show();
                    return;
                }

                final String ps = password.getText().toString();
                if(ps.trim().length() < 1){
                    Toast.makeText(MainActivity.this, "비밀번호는 필수 입력입니다.", Toast.LENGTH_LONG).show();
                    return;
                }

                final Handler loginHandler = new Handler(){
                    @Override
                    public void handleMessage(Message message){
                        if(result.trim().equals("fail") == false){
                            Session.id = ids;
                            Session.name = result.trim();
                            Toast.makeText(MainActivity.this, Session.name + "님 로그인 성공", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(MainActivity.this, ArticleList.class);
                            startActivity(intent);
                        }else{
                            Toast.makeText(MainActivity.this, "없는 아이디이거나 잘못된 비밀번호입니다.", Toast.LENGTH_LONG).show();
                        }
                    }
                };

                Thread th = new Thread(){
                    public void run(){
                        StringBuilder output = new StringBuilder();
                        try {
                            String addr = Common.server + "login.jsp?id=" + ids;
                            addr = addr + "&pw=" + ps;

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
                                loginHandler.sendEmptyMessage(0);
                            }
                        }catch (Exception e){
                            Log.e("로그인 예외", e.getMessage());
                        }
                    }
                };
                th.start();


            }
        });


        register = (Button)findViewById(R.id.register);
        register.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View view){
                Intent intent = new Intent(MainActivity.this, MemberRegister.class);
                startActivity(intent);
            }
        });

        findreg = (Button)findViewById(R.id.findreg);
        findreg.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View view){
                Intent intent = new Intent(MainActivity.this, PasswordFind.class);
                startActivity(intent);
            }
        });
    }
}
