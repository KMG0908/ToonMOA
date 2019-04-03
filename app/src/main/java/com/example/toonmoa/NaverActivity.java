package com.example.toonmoa;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class NaverActivity extends AppCompatActivity implements View.OnClickListener {
    public static String base_url = "http://192.168.0.25:81/toonmoa/";
    private String url = base_url + "webtoon.php";
    private GettingPHP gPHP;

    private String site = "네이버";

    private ArrayList<Integer> id = new ArrayList<>();
    private ArrayList<String> title = new ArrayList<>();
    private ArrayList<String> author = new ArrayList<>();
    private ArrayList<String> image = new ArrayList<>();
    private ArrayList<String> link = new ArrayList<>();
    private ArrayList<String> newest_title = new ArrayList<>();
    private ArrayList<String> newest_link = new ArrayList<>();

    ImageView imageView[];
    Button button[];
    TextView textView[];
    ImageButton alertButton[];

    int flag = 0;

    ProgressDialog progDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_naver);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("네이버 웹툰");

        progDialog = new ProgressDialog(this);
        progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDialog.setMessage("잠시만 기다려주세요.");
        progDialog.show();
        progDialog.setCancelable(false);

        gPHP = new GettingPHP();
        gPHP.execute(url);
    }

    class GettingPHP extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {
            String postParameters = "site=" + site;
            try {
                URL phpUrl = new URL(params[0]);
                HttpURLConnection conn = (HttpURLConnection)phpUrl.openConnection();

                conn.setReadTimeout(5000);
                conn.setConnectTimeout(5000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.connect();

                OutputStream outputStream = conn.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();

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

                imageView = new ImageView[title.size()];
                textView = new TextView[title.size()];
                button = new Button[title.size()];
                alertButton = new ImageButton[title.size()];

                LinearLayout linearLayout = (LinearLayout)findViewById(R.id.layout);

                int two = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics());
                int three = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, getResources().getDisplayMetrics());
                int fifty = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics());

                Display display = getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                int width = size.x;

                DBOpenHelper dbOpenHelper = new DBOpenHelper(NaverActivity.this);
                dbOpenHelper.open();

                for(int i=0; i<title.size(); i++){
                    FrameLayout frameLayout = new FrameLayout(NaverActivity.this);
                    frameLayout.setLayoutParams(new FrameLayout.LayoutParams(width - fifty, fifty));

                    imageView[i] = new ImageView(NaverActivity.this);
                    imageView[i].setPadding(-60, two, two, two);
                    imageView[i].setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, fifty));

                    textView[i] = new TextView(NaverActivity.this);
                    textView[i].setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, fifty));
                    textView[i].setPadding(three, three, three, three);
                    textView[i].setGravity(Gravity.BOTTOM | Gravity.RIGHT);
                    textView[i].setText(author.get(i));

                    button[i] = new Button(NaverActivity.this);
                    button[i].setId(i+1);
                    button[i].setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, fifty));
                    button[i].setGravity(Gravity.CENTER_VERTICAL);
                    button[i].setBackgroundDrawable(ContextCompat.getDrawable(NaverActivity.this, R.drawable.btn_bg));
                    button[i].setText("                     " + title.get(i));

                    alertButton[i] = new ImageButton(NaverActivity.this);
                    String id_ = "999" + (i + 1);
                    alertButton[i].setId(Integer.parseInt(id_));
                    alertButton[i].setLayoutParams(new RelativeLayout.LayoutParams(fifty, fifty));
                    Cursor c = dbOpenHelper.selectColumn(id.get(i));

                    if(c.moveToNext() != false){
                        alertButton[i].setBackgroundDrawable(ContextCompat.getDrawable(NaverActivity.this, R.drawable.alert_paint));
                    }
                    else{
                        alertButton[i].setBackgroundDrawable(ContextCompat.getDrawable(NaverActivity.this, R.drawable.alert));
                    }

                    LinearLayout linearLayout1 = new LinearLayout(NaverActivity.this);
                    linearLayout1.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, fifty));
                    linearLayout1.setBackgroundDrawable(ContextCompat.getDrawable(NaverActivity.this, R.drawable.btn_bg));

                    frameLayout.addView(imageView[i]);
                    frameLayout.addView(textView[i]);
                    frameLayout.addView(button[i]);
                    linearLayout1.addView(frameLayout);
                    linearLayout1.addView(alertButton[i]);
                    linearLayout.addView(linearLayout1);
                }

                for(int i=0; i<image.size(); i++) {
                    PostImage postImage = new PostImage();
                    postImage.execute(new String[]{image.get(i)});
                }

                for(int i=0; i<button.length; i++){
                    button[i].setOnClickListener(NaverActivity.this);
                }

                for(int i=0; i<alertButton.length; i++){
                    alertButton[i].setOnClickListener(NaverActivity.this);
                }

                //progDialog.dismiss();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void onClick(View v) {
        for(int i=0; i<title.size(); i++)
        {
            String id_ = "999" + (i + 1);
            if(v.getId() == Integer.parseInt(id_)){
                DBOpenHelper dbOpenHelper = new DBOpenHelper(this);
                dbOpenHelper.open();
                if(alertButton[i].getBackground().getConstantState().equals(getResources().getDrawable(R.drawable.alert).getConstantState())){
                    alertButton[i].setBackgroundDrawable(ContextCompat.getDrawable(NaverActivity.this, R.drawable.alert_paint));
                    dbOpenHelper.insertColumn(id.get(i), newest_title.get(i));
                }
                else if(alertButton[i].getBackground().getConstantState().equals(getResources().getDrawable(R.drawable.alert_paint).getConstantState())){
                    alertButton[i].setBackgroundDrawable(ContextCompat.getDrawable(NaverActivity.this, R.drawable.alert));
                    dbOpenHelper.deleteColumn(id.get(i));
                }
                dbOpenHelper.close();
            }
            else if(v.getId() == i + 1)
            {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link.get(i))));
            }
        }
    }

    private class PostImage extends AsyncTask<String, Void, Bitmap> {
        //BackGround 작업 진행
        protected Bitmap doInBackground (String...urls) {
            Bitmap map = null;
            for (String url : urls) {
                map = downloadImage(url);
            }
            return map;
        }

        //BackGround 작업 후 UI 작업 진행
        protected void onPostExecute(Bitmap result) { //이미지뷰에 이미지 표시
            imageView[flag].setImageBitmap(result);
            flag += 1;
            if(flag == title.size() - 1) progDialog.dismiss();
        }

        private Bitmap downloadImage(String url) { //이미지 다운 후 bitmap으로 변환한 뒤 리턴
            Bitmap bitmap = null;
            InputStream stream = null;
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inSampleSize = 1;

            try {
                stream = getHttpConnection(url);
                bitmap = BitmapFactory.decodeStream(stream, null, bmOptions);
                bitmap = Bitmap.createScaledBitmap(bitmap, 300, 250, true);
                stream.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return bitmap;
        }

        private InputStream getHttpConnection(String urlString) throws IOException {
            InputStream stream = null;
            URL url = new URL(urlString);
            URLConnection connection = url.openConnection();

            try {
                HttpURLConnection httpConnection = (HttpURLConnection) connection;
                httpConnection.setRequestMethod("GET");
                httpConnection.connect();

                if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    stream = httpConnection.getInputStream();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return stream;
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
