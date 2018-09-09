package com.example.olskr.myapplication;


import android.os.AsyncTask;
import android.util.Log;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;


// Создатель запросов (класс, умеющий запрашивать страницы)
public class RequestMaker {

    // APP ID вашего аккаунта на сайте погоды: бесплатен
    // Но приложение запускается и без ключа, но и без данных
    private static final String OPEN_WEATHER_MAP_API = "http://api.openweathermap.org/data/2.5/weather?q=%s&units=metric";
    private static final String OPEN_WEATHER_MAP_APP_ID = "ac8e27ae2dc7a85df994b9c9b6293ea8";
    private static final String KEY = "x-api-key";
    private static final String RESPONSE = "cod";
    private static final String NEW_LINE = "\n";
    private static final int ERROR_FROM_SERVER = 200;

    // Слушатель, при помощи него отправим обратный вызов о готовности страницы
    private final OnRequestListener listener;

    // В конструкторе примем слушателя, в дальнейшем его передадим асинхронной задаче
    public RequestMaker(OnRequestListener onRequestListener){
        listener = onRequestListener;
    }

    // Сделать запрос
    public void make(String city) {
        // создаем объект асинхронной задачи (передаем ей слушателя)
        Requester requester = new Requester(listener);
        // Запускаем асинхронную задачу
        requester.execute(city);
    }

    // Интерфейс слушателя с методами обратного вызова
    public interface OnRequestListener {
        void onStatusProgress(String updateProgress);   // Вызов для обновления прогресса
        void onComplete(JSONObject json);                 // Вызов при завершении обработки
    }

    // AsyncTask - это обертка для выполнения потока в фоне.
    // Начальные и конечные методы работают в потоке UI, а основной метод расчета работает в фоне
    private static class Requester extends AsyncTask<String, String, JSONObject> {

        private final OnRequestListener listener;

        Requester(OnRequestListener listener) {
            this.listener = listener;
        }

        // Обновление прогресса, работает в основном потоке UI
        @Override
        protected void onProgressUpdate(String... strings) {
            listener.onStatusProgress(strings[0]);
        }

        @Override
        protected JSONObject doInBackground(String... strings) {
            return getJSONData(strings[0]);
        }

        // Выдать результат, работает в основном потоке UI
        @Override
        protected void onPostExecute(JSONObject json) {
            listener.onComplete(json);
        }

        //метод класса, который делает запрос на сервер и получает от него данные
        //возвращает объект JSON или null
        public JSONObject getJSONData(String city) {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(String.format(OPEN_WEATHER_MAP_API, city));
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET"); // установка метода получения данных -GET
                connection.setReadTimeout(10000);   // установка таймаута - 10 000 миллисекунд
                publishProgress("Подготовка данных"); // обновим прогресс

                connection.addRequestProperty(KEY, OPEN_WEATHER_MAP_APP_ID);
                publishProgress("Соединение"); // обновим прогресс

                //принимаем поток данных
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder rawData = new StringBuilder(1024);
                publishProgress("Получение данных"); // обновим прогресс

                String tempVariable;
                int numLine = 0; // эта переменная нужна лишь для показа прогресса
                //пока идет поток. сохраняем данные
                while ((tempVariable = reader.readLine()) != null) {
                    numLine++;
                    publishProgress(String.format("Строка %d", numLine));// обновим прогресс
                    rawData.append(tempVariable).append(NEW_LINE);
                }
                reader.close(); //закрываем поток
                //создаем объект json
                JSONObject jsonObject = new JSONObject(rawData.toString());
                if (jsonObject.getInt(RESPONSE) != ERROR_FROM_SERVER) {
                    publishProgress(); // обновим прогресс
                    return null;
                }
                publishProgress("Данные загружены"); // обновим прогресс
                return jsonObject;
            } catch (Exception e) {
                publishProgress("Ошибка"); // обновим прогресс
            }finally {
                if (connection != null) connection.disconnect(); // разъединиться
            }return null;
        }
    }
}
