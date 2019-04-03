package com.example.toonmoa;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements Button.OnClickListener{
    private AlertService alertService;
    private boolean isBind;

    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override   // 서비스가 실행될 때 호출
        public void onServiceConnected(ComponentName name, IBinder service) {
            AlertService.AlertBinder binder = (AlertService.AlertBinder) service;
            alertService = binder.getService();

            isBind = true;
            System.out.println("onServiceConnected()");
        }

        @Override   // 서비스가 종료될 때 호출
        public void onServiceDisconnected(ComponentName name) {
            alertService = null;
            isBind = false;
            System.out.println("onServiceDisconnected()");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.naver).setOnClickListener(this);
        findViewById(R.id.daum).setOnClickListener(this);
        findViewById(R.id.ktoon).setOnClickListener(this);
        findViewById(R.id.foxtoon).setOnClickListener(this);

        DBOpenHelper dbOpenHelper = new DBOpenHelper(this);
        dbOpenHelper.open();
        dbOpenHelper.create();
        dbOpenHelper.close();

        startService(new Intent(MainActivity.this, AlertService.class));
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()){
            case R.id.naver:
                intent = new Intent(this, NaverActivity.class);
                startActivity(intent);
                break;
            case R.id.daum:
                intent = new Intent(this, DaumActivity.class);
                startActivity(intent);
                break;
            case R.id.ktoon:
                intent = new Intent(this, KtoonActivity.class);
                startActivity(intent);
                break;
            case R.id.foxtoon:
                intent = new Intent(this, FoxtoonActivity.class);
                startActivity(intent);
                break;
        }
    }

    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.search){
            Intent intent = new Intent(this, SearchActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
}
