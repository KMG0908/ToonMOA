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
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.DisplayMetrics;
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

public class SearchActivity extends AppCompatActivity implements View.OnClickListener {
    public static String url = NaverActivity.base_url + "search.php";
    GettingPHP gPHP;

    private ArrayList<Integer> id;
    private ArrayList<String> title;
    private ArrayList<String> author;
    private ArrayList<String> image;
    private ArrayList<String> link;
    private ArrayList<String> site;
    private ArrayList<String> newest_title;
    private ArrayList<String> newest_link;

    ImageView imageView[];
    Button button[];
    TextView textView[];
    TextView tv_site[];
    ImageButton alertButton[];

    int flag = 0;

    String postParameters = "";

    LinearLayout linearLayout;

    ProgressDialog progDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        linearLayout = (LinearLayout)findViewById(R.id.layout);
    }

    class GettingPHP extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {
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

                id = new ArrayList<>();
                title = new ArrayList<>();
                author = new ArrayList<>();
                image = new ArrayList<>();
                link = new ArrayList<>();
                site = new ArrayList<>();
                newest_title = new ArrayList<>();
                newest_link = new ArrayList<>();

                for ( int i = 0; i < results.length(); ++i ) {
                    JSONObject temp = results.getJSONObject(i);
                    id.add(Integer.parseInt(temp.get("id").toString()));
                    title.add(temp.get("title").toString());
                    author.add(temp.get("author").toString());
                    image.add(temp.get("image").toString());
                    link.add(temp.get("link").toString());
                    site.add(temp.get("site").toString());
                    newest_title.add(temp.get("newest_title").toString());
                    newest_link.add(temp.get("newest_link").toString());
                    System.out.println(id.get(i) + "\t" + title.get(i) + "\t" + author.get(i) + "\t" + image.get(i) + "\t");
                }

                imageView = new ImageView[title.size()];
                textView = new TextView[title.size()];
                tv_site = new TextView[title.size()];
                button = new Button[title.size()];
                alertButton = new ImageButton[title.size()];

                int two = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics());
                int three = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, getResources().getDisplayMetrics());
                int fifty = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics());

                Display display = getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                int width = size.x;

                DBOpenHelper dbOpenHelper = new DBOpenHelper(SearchActivity.this);
                dbOpenHelper.open();

                for(int i=0; i<title.size(); i++){
                    FrameLayout frameLayout = new FrameLayout(SearchActivity.this);
                    frameLayout.setLayoutParams(new FrameLayout.LayoutParams(width - fifty, fifty));

                    imageView[i] = new ImageView(SearchActivity.this);
                    imageView[i].setPadding(-60, two, two, two);
                    imageView[i].setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, fifty));

                    textView[i] = new TextView(SearchActivity.this);
                    textView[i].setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, fifty));
                    textView[i].setPadding(three, three, three, three);
                    textView[i].setGravity(Gravity.BOTTOM | Gravity.RIGHT);
                    textView[i].setText(author.get(i));

                    tv_site[i] = new TextView(SearchActivity.this);
                    tv_site[i].setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, fifty));
                    tv_site[i].setPadding(three, three, three, three);
                    tv_site[i].setGravity(Gravity.TOP | Gravity.RIGHT);
                    tv_site[i].setText(site.get(i));

                    button[i] = new Button(SearchActivity.this);
                    button[i].setId(i+1);
                    button[i].setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, fifty));
                    button[i].setGravity(Gravity.CENTER_VERTICAL);
                    button[i].setBackgroundDrawable(ContextCompat.getDrawable(SearchActivity.this, R.drawable.btn_bg));
                    button[i].setText("                     " + title.get(i));

                    alertButton[i] = new ImageButton(SearchActivity.this);
                    String id_ = "999" + (i + 1);
                    alertButton[i].setId(Integer.parseInt(id_));
                    alertButton[i].setLayoutParams(new RelativeLayout.LayoutParams(fifty, fifty));
                    Cursor c = dbOpenHelper.selectColumn(id.get(i));

                    if(c.moveToNext() != false){
                        alertButton[i].setBackgroundDrawable(ContextCompat.getDrawable(SearchActivity.this, R.drawable.alert_paint));
                    }
                    else{
                        alertButton[i].setBackgroundDrawable(ContextCompat.getDrawable(SearchActivity.this, R.drawable.alert));
                    }

                    LinearLayout linearLayout1 = new LinearLayout(SearchActivity.this);
                    linearLayout1.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, fifty));
                    linearLayout1.setBackgroundDrawable(ContextCompat.getDrawable(SearchActivity.this, R.drawable.btn_bg));

                    frameLayout.addView(imageView[i]);
                    frameLayout.addView(textView[i]);
                    frameLayout.addView(tv_site[i]);
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
                    button[i].setOnClickListener(SearchActivity.this);
                }

                for(int i=0; i<alertButton.length; i++){
                    alertButton[i].setOnClickListener(SearchActivity.this);
                }
            } catch (JSONException e) {
                progDialog.dismiss();

                DisplayMetrics displayMetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

                TextView t = new TextView(SearchActivity.this);
                t.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int)displayMetrics.heightPixels - 250));
                t.setText("검색 결과가 존재하지 않습니다.");
                t.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                linearLayout.addView(t);
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
                    alertButton[i].setBackgroundDrawable(ContextCompat.getDrawable(SearchActivity.this, R.drawable.alert_paint));
                    dbOpenHelper.insertColumn(id.get(i), newest_title.get(i));
                }
                else if(alertButton[i].getBackground().getConstantState().equals(getResources().getDrawable(R.drawable.alert_paint).getConstantState())){
                    alertButton[i].setBackgroundDrawable(ContextCompat.getDrawable(SearchActivity.this, R.drawable.alert));
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

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setQueryHint("웹툰 제목 검색");
        searchView.setMaxWidth(Integer.MAX_VALUE);

        Intent intent = getIntent();
        if(intent.getExtras() == null){
            searchItem.expandActionView();
        }
        else{
            postParameters = "title=" + intent.getExtras().getString("search_name");

            MenuItemCompat.expandActionView(searchItem);
            searchView.clearFocus();
            searchView.setQuery(intent.getExtras().getString("search_name"), false);

            progDialog = new ProgressDialog(this);
            progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progDialog.setMessage("잠시만 기다려주세요.");
            progDialog.show();
            progDialog.setCancelable(false);

            gPHP = new GettingPHP();
            gPHP.execute(url);
        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String str) {
                //검색 버튼이 눌렸을 때
                Intent intent = new Intent(getApplicationContext(), SearchActivity.class);
                intent.putExtra("search_name", str);
                finish();
                startActivity(intent);

                return false;
            }

            @Override
            public boolean onQueryTextChange(String str) {
                // 검색창에 글자를 칠 때
                return false;
            }
        });

        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                finish();
                return false;
            }
        });

        return true;
    }
}
