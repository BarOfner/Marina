package com.example.user.marina;


import android.text.format.Time;

public class Execute {

    public String ShowTime(Entity e)
    {
        Time today = new Time(Time.getCurrentTimezone());
        return today.hour + ":" + today.minute;
    }

}
