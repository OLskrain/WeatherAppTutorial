package com.example.olskr.myapplication;


import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONObject;

import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

/** Фрагмент, в нашем случае - основной UI приложения */

public class WeatherFragment extends Fragment {

    //Классовые переменные
    private static final String LOG_TAG = "WeatherFragment";
    private static final String FONT_FILENAME = "fonts/weather.ttf";
    private final Handler handler = new Handler();

    //Реализация иконок погоды через шрифт (но можно и через setImageDrawable)
    private Typeface weatherFont;
    private TextView cityTextView;
    private TextView updatedTextView;
    private TextView detailsTextView;
    private TextView currentTemperatureTextView;
    private TextView weatherIcon;


    //Callback при создании класса (Жизненный цикл Фрагмента)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WeatherActivity activity = (WeatherActivity) getActivity();
        weatherFont = Typeface.createFromAsset(getActivity().getAssets(), FONT_FILENAME);//находим наш шрифт
        updateWeatherData(new CityPreference(activity).getCity());
    }

    //Callback при создании класса (Жизненный цикл Фрагмента)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_weather, container, false);
        cityTextView = (TextView) rootView.findViewById(R.id.city_field);
        updatedTextView = (TextView) rootView.findViewById(R.id.updated_field);
        detailsTextView = (TextView) rootView.findViewById(R.id.details_field);
        currentTemperatureTextView = (TextView) rootView.findViewById(R.id.current_temperature_field);
        weatherIcon = (TextView) rootView.findViewById(R.id.weather_icon);

        weatherIcon.setTypeface(weatherFont);
        return rootView;
    }

    //Обновление/загрузка погодных данных
    //тут рекомендуеться отображать процесс загрузки
    private void updateWeatherData(final String city) {
        new Thread() {
            public void run() {
                final JSONObject json = WeatherDataLoader.getJSONData(getActivity(), city);
                if (json == null) {
                    //работаем через handler, чтобы не получить ошибку
                    handler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(getActivity(), getActivity().getString(R.string.place_not_found),
                                    Toast.LENGTH_LONG).show();
                            Log.d(LOG_TAG, "ERROOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOR");
                        }
                    });
                } else {
                    handler.post(new Runnable() {
                        public void run() {
                            renderWeather(json);
                        }
                    });
                }
            }
        }.start();
    }

    //Обработка загруженных данных
    private void renderWeather(JSONObject json) {
        try {
            cityTextView.setText(json.getString("name").toUpperCase(Locale.US) + ", "
                    + json.getJSONObject("sys").getString("country"));

            JSONObject details = json.getJSONArray("weather").getJSONObject(0);
            JSONObject main = json.getJSONObject("main");
            detailsTextView.setText(details.getString("description").toUpperCase(Locale.US) + "\n" + "Humidity: "
                    + main.getString("humidity") + "%" + "\n" + "Pressure: " + main.getString("pressure") + " hPa");

            currentTemperatureTextView.setText(String.format("%.2f", main.getDouble("temp")) + " ℃");

            DateFormat df = DateFormat.getDateTimeInstance();
            String updatedOn = df.format(new Date(json.getLong("dt") * 1000));
            updatedTextView.setText("Last update: " + updatedOn);

            //получение иконки
            setWeatherIcon(details.getInt("id"), json.getJSONObject("sys").getLong("sunrise") * 1000,
                    json.getJSONObject("sys").getLong("sunset") * 1000);

        } catch (Exception e) {
            Log.e("SimpleWeather", "One or more fields not found in the JSON data");
        }
    }

    //Подстановка нужной иконки
    private void setWeatherIcon(int actualId, long sunrise, long sunset) {
        int id = actualId / 100;
        String icon = "";
        if (actualId == 800) {
            long currentTime = new Date().getTime();
            if (currentTime >= sunrise && currentTime < sunset) {
                icon = getActivity().getString(R.string.weather_sunny);
            } else {
                icon = getActivity().getString(R.string.weather_clear_night);
            }
        } else {
            Log.d("SimpleWeather", "id " + id);
            switch (id) {
                case 2:
                    icon = getActivity().getString(R.string.weather_thunder);
                    break;
                case 3:
                    icon = getActivity().getString(R.string.weather_drizzle);
                    break;
                case 5:
                    icon = getActivity().getString(R.string.weather_rainy);
                    break;
                case 6:
                    icon = getActivity().getString(R.string.weather_snowy);
                    break;
                case 7:
                    icon = getActivity().getString(R.string.weather_foggy);
                    break;
                case 8:
                    icon = getActivity().getString(R.string.weather_cloudy);
                    break;
            }
        }
        weatherIcon.setText(icon);
    }

    //Метод для доступа кнопки меню к данным
    public void changeCity(String city) {
        updateWeatherData(city);
    }

}
