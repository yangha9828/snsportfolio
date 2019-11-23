package com.gmail.jster197;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import java.io.*;

public class MemberRegister extends AppCompatActivity {
    EditText id;
    EditText password;
    EditText name;

    Spinner year;
    ArrayList<Integer> yearlist ;
    ArrayAdapter<Integer> yearadapter;

    Spinner month;
    ArrayList<Integer> monthlist ;
    ArrayAdapter<Integer> monthadapter;

    Spinner day;
    ArrayList<Integer> daylist ;
    ArrayAdapter<Integer> dayadapter;

    RadioButton radio1;
    RadioButton radio2;
    EditText mail;
    EditText number;
    Button register;
    Button back;
    RadioGroup group;
    boolean idcheck = false;
    String result;

    Handler idcheckHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        id = (EditText)findViewById(R.id.id);
        id.setOnFocusChangeListener(new View.OnFocusChangeListener(){
            public void onFocusChange(View v, boolean b){
                if(b == false){
                    Thread th = new Thread(){
                        public void run(){
                            StringBuilder output = new StringBuilder();
                            try {
                                String ids = id.getText().toString().trim();
                                URL url = new URL(Common.server + "idcheck.jsp?id=" + ids);
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
                                    idcheckHandler.sendEmptyMessage(0);
                                }
                            }catch (Exception e){
                                Log.e("아이디 체크 예외", e.getMessage());
                            }
                        }
                    };
                    th.start();
                }
            }
        });

        idcheckHandler = new Handler(){
            @Override
            public void handleMessage(Message message){
                if(result.trim().length() < 1){
                    Toast.makeText(MemberRegister.this, "아이디는 필수 입력입니다.", Toast.LENGTH_LONG).show();
                    idcheck = false;
                    return;
                }
                if(result.trim().equals("success")){
                    Toast.makeText(MemberRegister.this, "사용 가능한 아이디 입니다.", Toast.LENGTH_LONG).show();
                    idcheck = true;
                }else{
                    Toast.makeText(MemberRegister.this, "이미 사용 중인 아이디 입니다.", Toast.LENGTH_LONG).show();
                    idcheck = false;
                }
            }
        };

        password = (EditText)findViewById(R.id.password);
        name = (EditText)findViewById(R.id.name);

        year = (Spinner)findViewById(R.id.year);
        yearlist = new ArrayList<>();
        for(int i=2019; i>1900; i=i-1){
            yearlist.add(i);
        }
        yearadapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, yearlist);
        year.setAdapter(yearadapter);

        month = (Spinner)findViewById(R.id.month);
        monthlist = new ArrayList<>();
        for(int i=1; i<=12; i=i+1){
            monthlist.add(i);
        }
        monthadapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, monthlist);
        month.setAdapter(monthadapter);

        day = (Spinner)findViewById(R.id.day);
        daylist = new ArrayList<>();
        for(int i=1; i<=31; i=i+1){
            daylist.add(i);
        }
        dayadapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, daylist);
        day.setAdapter(dayadapter);

        radio1 = (RadioButton)findViewById(R.id.radio1);
        radio2 = (RadioButton)findViewById(R.id.radio2);
        mail = (EditText)findViewById(R.id.mail);

        number = (EditText)findViewById(R.id.number);

        group = (RadioGroup)findViewById(R.id.radiogroup);

        back = (Button)findViewById(R.id.back);
        back.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View view){
                finish();
            }
        });

        register = (Button)findViewById(R.id.register);
        register.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View view){
                String ids = id.getText().toString();
                if(ids.trim().length() < 1){
                    Toast.makeText(MemberRegister.this, "아이디는 필수 입력입니다.", Toast.LENGTH_LONG).show();
                    return;
                }

                final String ps = password.getText().toString();
                if(ps.trim().length() < 1){
                    Toast.makeText(MemberRegister.this, "비밀번호는 필수 입력입니다.", Toast.LENGTH_LONG).show();
                    return;
                }

                final String n = name.getText().toString();
                if(n.trim().length() < 1){
                    Toast.makeText(MemberRegister.this, "이름은 필수 입력입니다.", Toast.LENGTH_LONG).show();
                    return;
                }

                String y = year.getSelectedItem().toString();
                String m = month.getSelectedItem().toString();
                String d = day.getSelectedItem().toString();

                Date date = new Date(Integer.parseInt(y)-1900, Integer.parseInt(m)-1, Integer.parseInt(d));
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                final String birth = sdf.format(date);



                final String ma = mail.getText().toString();
                if(ma.trim().length() < 1){
                    Toast.makeText(MemberRegister.this, "이메일은 필수 입력입니다.", Toast.LENGTH_LONG).show();
                    return;
                }

                final String num = number.getText().toString();
                if(num.trim().length() < 1){
                    Toast.makeText(MemberRegister.this, "전화번호는 필수 입력입니다.", Toast.LENGTH_LONG).show();
                    return;
                }

                final Handler registerHandler = new Handler(){
                    @Override
                    public void handleMessage(Message message){
                        if(result.trim().equals("success") && idcheck==true){
                            Toast.makeText(MemberRegister.this, "회원가입 성공", Toast.LENGTH_LONG).show();
                            finish();
                        }else{
                            Toast.makeText(MemberRegister.this, "회원가입 실패", Toast.LENGTH_LONG).show();
                            idcheck = false;
                        }
                    }
                };




                Thread th = new Thread(){
                    public void run(){
                        StringBuilder output = new StringBuilder();
                        try {
                            String ids = id.getText().toString().trim();
                            String addr = Common.server + "register.jsp?id=" + ids;
                            addr = addr + "&password=" + ps;
                            addr = addr + "&name=" + n;
                            String gender= "남자";
                            if(group.getCheckedRadioButtonId() == R.id.radio2){
                                gender="여자";
                            }
                            addr = addr + "&gender=" + gender;
                            addr = addr + "&birth=" + birth;
                            addr = addr + "&mail=" + ma;
                            addr = addr + "&mobile=" + num;

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
}
