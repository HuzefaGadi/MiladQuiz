package com.saiflimited.miladquiz;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class QuizQuestionActivity extends AppCompatActivity {

    Button submit;
    RadioGroup radioGroup;
    TextView questionNumber;
    String username;
    String currentQuestionNumber;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    RelativeLayout questionLayout, waitingLayout;
    TextView previousQuestionAnswer;
    Timer timer;
    TimerTask timertask;
    ProgressDialog progressDialogForActiveQuestion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_question);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        sharedPreferences = getSharedPreferences(Constants.PREFERENCE_NAME, MODE_PRIVATE);
        editor = sharedPreferences.edit();
        questionLayout = (RelativeLayout) findViewById(R.id.layout_question);
        waitingLayout = (RelativeLayout) findViewById(R.id.layout_waitingscreen);
        submit = (Button) findViewById(R.id.submit);
        previousQuestionAnswer = (TextView) findViewById(R.id.rightAnswerText);
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        questionNumber = (TextView) findViewById(R.id.questionNumber);
        username = sharedPreferences.getString(Constants.USERNAME, "");
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int selectedRadioButton = radioGroup.getCheckedRadioButtonId();
                String selectedRadioButtonString = null;
                switch (selectedRadioButton) {
                    case R.id.radioButton1:
                        selectedRadioButtonString = "A";
                        break;
                    case R.id.radioButton2:
                        selectedRadioButtonString = "B";
                        break;
                    case R.id.radioButton3:
                        selectedRadioButtonString = "C";
                        break;
                    case R.id.radioButton4:
                        selectedRadioButtonString = "D";
                        break;
                    default:
                        selectedRadioButtonString = null;
                        break;
                }
                if (selectedRadioButtonString == null) {
                    Toast.makeText(QuizQuestionActivity.this, "Please select One Answer", Toast.LENGTH_LONG).show();
                } else {
                    callWebServiceUtility(selectedRadioButtonString);
                }
            }
        });

        progressDialogForActiveQuestion = new ProgressDialog(QuizQuestionActivity.this);
        progressDialogForActiveQuestion.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialogForActiveQuestion.setMessage("Please wait..");
        progressDialogForActiveQuestion.setTitle("Initializing");
        progressDialogForActiveQuestion.setCancelable(false);
        progressDialogForActiveQuestion.show();

        timer = new Timer();
        timertask = new TimerTask() {
            @Override
            public void run() {
                callWebServiceUtilityForActiveQuestion();
            }
        };

        timer.schedule(timertask, 100, 20000);

    }


    public void callWebServiceUtility(String selectedRadioButtonString) {
        new CallWebService().execute(selectedRadioButtonString);
    }

    public void callWebServiceUtilityForActiveQuestion() {
        new CallWebServiceForActiveQuestion().execute();
    }

    class CallWebService extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;

        @Override
        protected String doInBackground(String... voids) {

            URL url = null;
            try {
                String urlString = Constants.ANSWER_URL + "?username=" + username + "&ques_id=" + currentQuestionNumber + "&answer=" + voids[0];
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
            progressDialog = new ProgressDialog(QuizQuestionActivity.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage("Please wait..");
            progressDialog.setTitle("Submiting your answer");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            try {
                if (progressDialog != null || progressDialog.isShowing()) {
                    progressDialog.hide();
                }

                if (result != null) {

                    if (result.equalsIgnoreCase("TRUE")) {
                        Toast.makeText(QuizQuestionActivity.this, "ANSWER SAVED SUCCESSFULLY", Toast.LENGTH_LONG).show();
                        waitingLayout.setVisibility(View.VISIBLE);
                        questionLayout.setVisibility(View.GONE);
                    } else {
                        Toast.makeText(QuizQuestionActivity.this, "ISSUE WITH SERVER, TRY AGAIN", Toast.LENGTH_LONG).show();
                        waitingLayout.setVisibility(View.GONE);
                        questionLayout.setVisibility(View.VISIBLE);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                waitingLayout.setVisibility(View.GONE);
                questionLayout.setVisibility(View.VISIBLE);
            }


        }
    }


    class CallWebServiceForActiveQuestion extends AsyncTask<Void, Void, String> {


        @Override
        protected String doInBackground(Void... voids) {

            URL url = null;
            try {
                String urlString = Constants.ACTIVE_QUESTION_URL;
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

        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            try {
                // result = "7";
                if (progressDialogForActiveQuestion != null || progressDialogForActiveQuestion.isShowing()) {
                    progressDialogForActiveQuestion.hide();
                }
                if (result != null) {
                    currentQuestionNumber = result;
                    System.out.println("Answer Current " + currentQuestionNumber);
                    String currentQuestionNumberFromPreferences = sharedPreferences.getString(Constants.CURRENT_QUESTION_NUMBER, "0");
                    questionNumber.setText(currentQuestionNumber);
                    if (!currentQuestionNumberFromPreferences.equalsIgnoreCase(currentQuestionNumber)) {
                        editor.putString(Constants.CURRENT_QUESTION_NUMBER, currentQuestionNumber).commit();

                        if (!currentQuestionNumber.equalsIgnoreCase("1")) {
                            waitingLayout.setVisibility(View.VISIBLE);
                            questionLayout.setVisibility(View.GONE);
                            showDelayedAnswer(currentQuestionNumberFromPreferences, "B");
                        }

                    } else {

                    }


                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        public void showDelayedAnswer(final String previousQuestionNumber, final String answer) {
            Timer timer = new Timer();
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            previousQuestionAnswer.setText("Answer for Question No. " + previousQuestionNumber + "is " + answer);
                            previousQuestionAnswer.setVisibility(View.VISIBLE);
                            showDelayedQuestionAfterAnswer();
                        }
                    });


                }
            };
            timer.schedule(timerTask, 30000);
        }

        public void showDelayedQuestionAfterAnswer()
        {
            Timer timer = new Timer();
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            waitingLayout.setVisibility(View.GONE);
                            questionLayout.setVisibility(View.VISIBLE);
                        }
                    });


                }
            };
            timer.schedule(timerTask, 5000);
        }
    }

}
