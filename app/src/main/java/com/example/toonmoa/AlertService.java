package com.example.toonmoa;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class AlertService extends Service {
    private IBinder iBinder = new AlertBinder();
    private Thread thread;
    public static String base_url = NaverActivity.base_url;
    private String url = base_url + "webtoonList.php";
    private GettingPHP gPHP;

    private ArrayList<Integer> id = new ArrayList<>();
    private ArrayList<String> title = new ArrayList<>();
    private ArrayList<String> author = new ArrayList<>();
    private ArrayList<String> image = new ArrayList<>();
    private ArrayList<String> link = new ArrayList<>();
    private ArrayList<String> newest_title = new ArrayList<>();
    private ArrayList<String> newest_link = new ArrayList<>();

    class AlertBinder extends Binder{
        AlertService getService(){
            return AlertService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        System.out.println("onBind()");
        return iBinder;
    }

    @Override
    public void onCreate() {
        System.out.println("onCreate()");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("onStartCommand()");

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    gPHP = new GettingPHP();
                    gPHP.execute(url);
                    SystemClock.sleep(1000 * 30);
                }
            }
        });

        thread.setDaemon(true);
        thread.start();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        System.out.println("onDestroy()");

        thread.interrupt();

        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        System.out.println("onUnbind()");
        return super.onUnbind(intent);
    }

    class GettingPHP extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {
            StringBuilder jsonHtml = new StringBuilder();
            try {
                URL phpUrl = new URL(params[0]);
                HttpURLConnection conn = (HttpURLConnection)phpUrl.openConnection();

                conn.setReadTimeout(5000);
                conn.setConnectTimeout(5000);
                conn.connect();

                int responseStatusCode = conn.getResponseCode();

                InputStream inputStream;
                if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = conn.getInputStream();
                }
                else{
                    inputStream = conn.getErrorStream();
                }

                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line;

                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }

                bufferedReader.close();

                return sb.toString().trim();
            } catch ( Exception e ) {
                e.printStackTrace();
                return null;
            }
        }

        protected void onPostExecute(String str) {
            try {
                // PHP에서 받아온 JSON 데이터를 JSON오브젝트로 변환
                JSONObject jObject = new JSONObject(str);
                // results라는 key는 JSON배열로 되어있다.
                JSONArray results = jObject.getJSONArray("results");

                id = new ArrayList<>();
                title = new ArrayList<>();
                author = new ArrayList<>();
                image = new ArrayList<>();
                link = new ArrayList<>();
                newest_title = new ArrayList<>();
                newest_link = new ArrayList<>();

                for ( int i = 0; i < results.length(); ++i ) {
                    JSONObject temp = results.getJSONObject(i);
                    id.add(Integer.parseInt(temp.get("id").toString()));
                    title.add(temp.get("title").toString());
                    author.add(temp.get("author").toString());
                    image.add(temp.get("image").toString());
                    link.add(temp.get("link").toString());
                    newest_title.add(temp.get("newest_title").toString());
                    newest_link.add(temp.get("newest_link").toString());
                    System.out.println(id.get(i) + "\t" + title.get(i) + "\t" + author.get(i) + "\t" + image.get(i) + "\t");
                }

                DBOpenHelper dbOpenHelper = new DBOpenHelper(AlertService.this);
                dbOpenHelper.open();

                Cursor c = dbOpenHelper.selectColumns();

                while(c.moveToNext()){
                    int index = id.indexOf(c.getInt(c.getColumnIndex("id")));
                    if(!newest_title.get(index).equals(c.getString(c.getColumnIndex("newest_title")))){
                        dbOpenHelper.updateColumn(c.getInt(c.getColumnIndex("id")), newest_title.get(index));

                        //Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.icon1);
                        //알림 사운드
                        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                        //알림 클릭시 이동할 인텐트
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(newest_link.get(index)));
                        //노티피케이션을 생성할때 매개변수는 PendingIntent이므로 Intent를 PendingIntent로 만들어주어야함
                        PendingIntent pendingIntent = PendingIntent.getActivity(AlertService.this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

                        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                        NotificationCompat.Builder notificationBuilder;
                        if (Build.VERSION.SDK_INT >= 26) {
                            NotificationChannel mChannel = new NotificationChannel("andokdcapp", "andokdcapp", NotificationManager.IMPORTANCE_DEFAULT);
                            notificationManager.createNotificationChannel(mChannel);
                            notificationBuilder = new NotificationCompat.Builder(AlertService.this, mChannel.getId());
                        } else {
                            notificationBuilder = new NotificationCompat.Builder(AlertService.this);
                        }

                        //노티피케이션 빌더 : 위에서 생성한 이미지나 텍스트, 사운드등을 설정
                        notificationBuilder.setAutoCancel(true)
                                .setSmallIcon(R.drawable.smallicon)
                                .setContentText("『" + title.get(index) + "』의 새 편이 업데이트 되었습니다.")
                                .setAutoCancel(true)
                                .setSound(soundUri)
                                .setContentIntent(pendingIntent);

                        // 노티피케이션 생성
                        notificationManager.notify(0
                                // ID of notification
                                , notificationBuilder.build());

                        System.out.println("노티피케이션 생성");
                    }
                    System.out.println(newest_title.get(id.indexOf(c.getInt(c.getColumnIndex("id")))));
                    System.out.println(c.getString(c.getColumnIndex("newest_title")));
                }

                dbOpenHelper.close();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
