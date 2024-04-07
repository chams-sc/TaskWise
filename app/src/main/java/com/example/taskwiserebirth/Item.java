package com.example.taskwiserebirth;

public class Item {

    String title;
    String time;
    String prio;
    int menu;

    public Item(String title, String time, String prio) {
        this.title = title;
        this.time = time;
        this.prio = prio;
        this.menu = menu;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getPrio() {
        return prio;
    }

    public void setPrio(String prio) {
        this.prio = prio;
    }

    public int getMenu() {
        return menu;
    }

    public void setMenu(int menu) {
        this.menu = menu;
    }
}