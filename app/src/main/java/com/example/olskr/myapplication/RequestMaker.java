package com.example.olskr.myapplication;


import android.os.AsyncTask;
import android.util.Log;
import org.json.JSONObject;


// Создатель запросов (класс, умеющий запрашивать страницы)
public class RequestMaker {

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
            return  WeatherDataLoader.getJSONData(strings[0]);
        }

        // Выдать результат, работает в основном потоке UI
        @Override
        protected void onPostExecute(JSONObject json) {
            listener.onComplete(json);
        }
    }
}
