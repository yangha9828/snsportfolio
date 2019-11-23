package com.gmail.jster197;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MyAdapter extends BaseAdapter {
    //뷰를 출력할 때 필요한 Context(문맥-어떤 작업을 하기 위해 필요한 정보를 저장한 객체) 변수
    Context context;
    //리스트 뷰에 출력할 데이터
    ArrayList<Article> data;
    //항목 뷰에 해당하는 레이아웃의 아이디를 저장할 변수
    int layout;
    //xml로 만들어진 레이아웃을 뷰로 변환하기 위한 클래스의 변수
    LayoutInflater inflater;

    String temp;
    public MyAdapter(Context context, ArrayList<Article> data, int layout, String temp) {
        super();
        this.context = context;
        this.data = data;
        this.layout = layout;
        this.temp = temp;
        inflater = (LayoutInflater)context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    //출력할 데이터의 개수를 설정하는 메소드
    public int getCount() {
        return data.size();
    }

    @Override
    //항목 뷰에 보여질 문자열을 설정하는 메소드
    //position은 반복문이 수행될 때의 인덱스
    public Object getItem(int position) {
        return data.get(position).id;
    }

    @Override
    //각 항목뷰의 아이디를 설정하는 메소드
    public long getItemId(int position) {
        return position;
    }


    @Override
    //리스트 뷰에 출력될 실제 뷰의 모양을 설정하는 메소드
    //convertView는 화면에 보여질 뷰인데 처음에는 null이 넘어오고 두번째 부터는
    //이전에 출력된 뷰가 넘어옵니다.
    //인덱스마다 다른 뷰를 출력하고자 하면 convertView를 새로 만들지만
    //모든 항목뷰의 모양이 같다면 처음 한번만 만들면 됩니다.
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
        //convertView 생성
        if(convertView == null){
            //layout에 정의된 뷰를 parent에 넣을 수 있도록 View로 생성
            convertView = inflater.inflate(layout, parent, false);
        }
        //텍스트 출력
        TextView txtId = (TextView)convertView.findViewById(R.id.txtId);
        txtId.setText(data.get(pos).id);

        TextView txtRegdate = (TextView)convertView.findViewById(R.id.txtRegdate);
        txtRegdate.setText(data.get(pos).regdate);

        Button commentlist = (Button) convertView.findViewById(R.id.commentlist);
        if(temp.equals("게시글")) {
            commentlist.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, CommentList.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("articleid", data.get(pos).num);
                    context.startActivity(intent);
                }
            });
        }else{
            commentlist.setVisibility(View.INVISIBLE);
        }


        //버튼의 이벤트 처리
        Button btnDelete = (Button)convertView.findViewById(R.id.btnDelete);
        if(!data.get(pos).id.equals(Session.id)){
            btnDelete.setVisibility(View.INVISIBLE);
        }
        if(temp.equals("게시글")) {
            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    final Handler deleteHandler = new Handler() {
                        @Override
                        public void handleMessage(Message message) {
                            data.remove(data.get(pos));
                            notifyDataSetChanged();
                            Toast.makeText(context, "게시글 삭제", Toast.LENGTH_LONG).show();
                        }
                    };


                    Thread th = new Thread() {
                        public void run() {
                            StringBuilder output = new StringBuilder();
                            try {
                                String addr = Common.server + "articledelete.jsp?num=" + data.get(pos).num;

                                URL url = new URL(addr);
                                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                                String result = "";
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
                                    deleteHandler.sendEmptyMessage(0);
                                }
                            } catch (Exception e) {
                                Log.e("게시글 삭제 예외", e.getMessage());
                            }
                        }
                    };
                    th.start();
                }
            });
        }
        else{
            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    final Handler deleteHandler = new Handler() {
                        @Override
                        public void handleMessage(Message message) {
                            data.remove(data.get(pos));
                            notifyDataSetChanged();
                            Toast.makeText(context, "댓글 삭제", Toast.LENGTH_LONG).show();
                        }
                    };

                    Thread th = new Thread() {
                        public void run() {
                            StringBuilder output = new StringBuilder();
                            try {
                                String addr = Common.server + "commentdelete.jsp?num=" + data.get(pos).num;

                                URL url = new URL(addr);
                                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                                String result = "";
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
                                    deleteHandler.sendEmptyMessage(0);
                                }
                            } catch (Exception e) {
                                Log.e("게시글 삭제 예외", e.getMessage());
                            }
                        }
                    };
                    th.start();
                }
            });
        }
        TextView content = (TextView)convertView.findViewById(R.id.content);
        content.setText(data.get(pos).content);

        TextView likecnt = (TextView)convertView.findViewById(R.id.likecnt);
        likecnt.setText(data.get(pos).good + "");

        TextView dislikecnt = (TextView)convertView.findViewById(R.id.dislikecnt);
        dislikecnt.setText(data.get(pos).dislike + "");

        ImageView good = (ImageView)convertView.findViewById(R.id.like);
        ImageView dislike = (ImageView)convertView.findViewById(R.id.dislike);

        good.setOnClickListener(new View.OnClickListener() {
            final Handler likeHandler = new Handler(){
                @Override
                public void handleMessage(Message message){
                    Article article = data.get(pos);
                    article.good = article.good + 1;
                    notifyDataSetChanged();

                }
            };

            @Override
            public void onClick(View view) {
                Thread th = new Thread(){
                    public void run(){
                        StringBuilder output = new StringBuilder();
                        try {
                            String addr ="";
                            if(temp.equals("게시글")) {
                                addr = Common.server + "articlelike.jsp?num=" + data.get(pos).num;
                            }else{
                                addr = Common.server + "commentlike.jsp?num=" + data.get(pos).num;
                            }

                            Log.e("addr", addr);
                            URL url = new URL(addr);
                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                            String result = "";
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
                                likeHandler.sendEmptyMessage(0);
                            }
                        }catch (Exception e){
                            Log.e("좋아요 예외", e.getMessage());
                        }
                    }
                };
                th.start();
            }
        });

        dislike.setOnClickListener(new View.OnClickListener() {
            final Handler dislikeHandler = new Handler(){
                @Override
                public void handleMessage(Message message){
                    Article article = data.get(pos);
                    article.dislike = article.dislike + 1;
                    notifyDataSetChanged();

                }
            };

            @Override
            public void onClick(View view) {
                Thread th = new Thread(){
                    public void run(){
                        StringBuilder output = new StringBuilder();
                        try {
                            String addr = "";
                            if(temp.equals("게시글")) {
                                addr = Common.server + "articledislike.jsp?num=" + data.get(pos).num;
                            }else{
                                addr = Common.server + "commentdislike.jsp?num=" + data.get(pos).num;
                            }

                            URL url = new URL(addr);
                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                            String result = "";
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
                                dislikeHandler.sendEmptyMessage(0);
                            }
                        }catch (Exception e){
                            Log.e("싫어요 예외", e.getMessage());
                        }
                    }
                };
                th.start();
            }
        });
        return convertView;
    }
}

