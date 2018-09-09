package com.example.olskr.myapplication;


import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;

import android.content.Context;
import android.util.Log;


//Класс для обрашения к серверу и скачивания информации
public class WeatherDataLoader {
// APP ID вашего аккаунта на сайте погоды: бесплатен
//Но приложение запускается и без ключа, но и без данных
    private static final String OPEN_WEATHER_MAP_API = "http://api.openweathermap.org/data/2.5/weather?q=%s&units=metric";
    private static final String OPEN_WEATHER_MAP_APP_ID = "ac8e27ae2dc7a85df994b9c9b6293ea8";
    private static final String KEY = "x-api-key";
    private static final String RESPONSE = "cod";
    private static final String NEW_LINE = "\n";
    private static final int ERROR_FROM_SERVER = 200;

    //метод класса, который делает запрос на сервер и получает от него данные
    //возвращает объект JSON или null
    public static JSONObject getJSONData(String city) {
        try {
            URL url = new URL(String.format(OPEN_WEATHER_MAP_API, city));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.addRequestProperty(KEY, OPEN_WEATHER_MAP_APP_ID);

            //принимаем поток данных
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder rawData = new StringBuilder(1024);
            String tempVariable;
            //пока идет поток. сохраняем данные
            while ((tempVariable = reader.readLine()) != null) {
                rawData.append(tempVariable).append(NEW_LINE);
            }
            reader.close(); //закрываем поток
            //создаем объект json
            JSONObject jsonObject = new JSONObject(rawData.toString());
            if (jsonObject.getInt(RESPONSE) != ERROR_FROM_SERVER) {
                return null;
            }
            return jsonObject;
        } catch (Exception e) {
            return null; //здесь надо дописать обработку ощибки
        }
    }
}
