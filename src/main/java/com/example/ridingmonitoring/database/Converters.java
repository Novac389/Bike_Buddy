package com.example.ridingmonitoring.database;

import androidx.room.TypeConverter;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;

public class Converters {
    //converter per la data
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }
    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }

    @TypeConverter
    public static String latLngLIstToString(List<LatLng> route){
        if (route == null) {
            return (null);
        }
        Gson gson = new Gson();
        Type type = new TypeToken<List<LatLng>>() {}.getType();
        String json = gson.toJson(route, type);
        return json;
    }

    @TypeConverter
    public static List<LatLng> toLatLngList(String value){
        if (value == null) {
            return (null);
        }
        Gson gson = new Gson();
        Type type = new TypeToken<List<LatLng>>() {}.getType();
        List<LatLng> countryLangList = gson.fromJson(value, type);
        return countryLangList;
    }
}
