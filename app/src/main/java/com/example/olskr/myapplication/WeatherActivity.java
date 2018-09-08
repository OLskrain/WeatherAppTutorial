package com.example.olskr.myapplication;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

//наша основная активити
public class WeatherActivity extends AppCompatActivity {

    private static final String POSITIVE_BUTTON_TEXT = "Go";
    private static final String WEATHER_FRAGMENT_TAG = "43ddDcdd-c9e0-4794-B7e6-cf05af49fbf0";

    private CityPreference cityPreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        cityPreference = new CityPreference(this);//теперь мы можем сохранять данные и получать их из нашего класса
        //если мы приложение только открыли
        if (savedInstanceState == null) {
            //создали фрагмент и поместили его в контейнер, чтобы потом найти
            getSupportFragmentManager().beginTransaction().add(R.id.container_for_fragment, new WeatherFragment(), WEATHER_FRAGMENT_TAG)
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.weather, menu);
        return true;
    }

    //обработка нажатия кнопок меню
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.change_city) {
            showInputDialog();
            return true;
        }
        return false;
    }

    //отображение диалогового окна
    private void showInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(getResources().getString(R.string.change_city_dialog));
        //добавляем едит текст
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT); //указываем что будет только текст,а не номер напрмер(тип данных)
        builder.setView(input);

        //добавляем кнопку
        builder.setPositiveButton(POSITIVE_BUTTON_TEXT, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                changeCity(input.getText().toString());
            }
        });
        builder.show();
    }

    //обновляем вид, сохраняем выбранный город
    public void changeCity(String city) {
        //находим наш фрагмент по тегу
        WeatherFragment weatherFragment = (WeatherFragment) getSupportFragmentManager().findFragmentByTag(WEATHER_FRAGMENT_TAG);
        weatherFragment.changeCity(city);
        cityPreference.setCity(city);//обновляем данные в нашем хранилише
    }

}
