package com.thomas.simple;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.thomas.rxpreferences.PersistenceParser;
import com.thomas.rxpreferences.RxField;
import com.thomas.rxpreferences.RxPersistence;

import java.lang.reflect.Type;

import io.reactivex.functions.Consumer;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RxPersistence.init(this, new PersistenceParser() {
            @Override
            public Object deserialize(Type clazz, String text) {
                return new Gson().fromJson(text,clazz);
            }

            @Override
            public String serialize(Object object) {
                return new Gson().toJson(object);
            }
        });
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserTest2DiskCache.get().setName(((EditText)findViewById(R.id.editText)).getText().toString());
                UserTest2DiskCache.get().setNameRx(((EditText)findViewById(R.id.editText)).getText().toString())
                        .subscribe();
            }
        });
        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                UserTest2DiskCache.get().getNameRx().subscribe(new Consumer<RxField<String>>() {
                    @Override
                    public void accept(RxField<String> stringRxField) throws Exception {
                        ((TextView)findViewById(R.id.textview)).setText(stringRxField.get());
                    }
                });
            }
        });
        findViewById(R.id.button3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserTest2DiskCache.get().clear();
            }
        });

        findViewById(R.id.button4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((TextView)findViewById(R.id.textview)).setText(UserTest2DiskCache.get().getCacheCount()+"");

            }
        });
        findViewById(R.id.button5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((TextView)findViewById(R.id.textview)).setText( UserTest2DiskCache.get().getCacheSize()+"");

            }
        });
    }
}
