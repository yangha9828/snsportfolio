package com.gmail.jster197;

import java.util.Date;

public class Article implements Comparable<Article>{
    public int num;
    public String id;
    public String content;
    public String regdate;

    public int good;
    public int dislike;

    public int compareTo(Article other){
        if(other.regdate.equals(this.regdate)){
            return other.num - this.num;
        }
        else {
            return other.regdate.compareTo(this.regdate);
        }
    }

}
