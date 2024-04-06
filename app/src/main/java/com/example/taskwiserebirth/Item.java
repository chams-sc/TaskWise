package com.example.taskwiserebirth;

public class Item {
    String ItemName;
    String Time;
    String Urgency;

    public Item(String itemName, String time, String urgency) {
        ItemName = itemName;
        Time = time;
        Urgency = urgency;
    }

    public String getItemName() {
        return ItemName;
    }

    public void setItemName(String itemName) {
        ItemName = itemName;
    }

    public String getTime() {
        return Time;
    }

    public void setTime(String time) {
        Time = time;
    }

    public String getUrgency() {
        return Urgency;
    }

    public void setUrgency(String urgency) {
        Urgency = urgency;
    }
}
