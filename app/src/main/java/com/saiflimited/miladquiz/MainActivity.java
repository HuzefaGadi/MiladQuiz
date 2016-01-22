package com.saiflimited.miladquiz;

import android.app.ProgressDialog;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    EditText username, password;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        preferences = getSharedPreferences(Constants.PREFERENCE_NAME, MODE_PRIVATE);
        editor = preferences.edit();
        Button login = (Button) findViewById(R.id.button);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String usernameString = username.getText().toString();
                String passwordString = password.getText().toString();

                if (usernameString != null && !usernameString.isEmpty() && passwordString != null && !passwordString.isEmpty()) {
                    WebServiceUtility(usernameString, passwordString);
                } else {
                    Toast.makeText(MainActivity.this, "Please Enter all details", Toast.LENGTH_LONG).show();
                }


            }
        });

        if(preferences.getString(Constants.USERNAME,null)!=null)
        {
            Intent intent = new Intent(MainActivity.this, QuizQuestionActivity.class);
            startActivity(intent);
            finish();
            return;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void WebServiceUtility(String username, String password) {
        new CallWebService().execute(username, password);
    }

    class CallWebService extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;
        String usernameString;

        @Override
        protected String doInBackground(String... voids) {
            usernameString = voids[0];
            URL url = null;
            try {
                String urlString = Constants.LOGIN_URL + "?username=" + voids[0] + "&password=" + voids[1];
                url = new URL(urlString);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(1000 * 60);
                conn.setRequestMethod("GET");
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                String jsonResultLine = reader.readLine();
                return jsonResultLine;

            } catch (MalformedURLException e) {
                e.printStackTrace();

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage("Please wait..");
            progressDialog.setTitle("Logging In");
            progressDialog.setCancelable(false);
            progressDialog.show();

        }

        @Override
        protected void onPostExecute(String result) {
            if (progressDialog != null || progressDialog.isShowing()) {
                progressDialog.hide();
            }
            if (result != null) {
                try {

                    if (result.equalsIgnoreCase("TRUE")) {
                        editor.putString(Constants.USERNAME, usernameString).commit();
                        Intent intent = new Intent(MainActivity.this, QuizQuestionActivity.class);
                        startActivity(intent);
                        finish();
                        return;
                    } else {
                        Toast.makeText(MainActivity.this, "Wrong credentials", Toast.LENGTH_LONG).show();
                        return;
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }

                Toast.makeText(MainActivity.this, "Something is Wrong with Server", Toast.LENGTH_LONG).show();
            }
        }
    }


}