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
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
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
    RelativeLayout questionLayout, waitingLayout, finishLayout;
    TextView previousQuestionAnswer, welcomeMessage;
    Timer timer;
    TimerTask timertask;
    ProgressDialog progressDialogForActiveQuestion;

    public void stopTimer()
    {
        try
        {
            timer.cancel();
            timertask.cancel();
        }catch (Exception e)
        {

        }
    }
    @Override
    protected void onStop() {
        super.onStop();
       stopTimer();

    }

    @Override
    protected void onStart() {
        super.onStart();
        timer = new Timer();
        timertask = new TimerTask() {
            @Override
            public void run() {
                callWebServiceUtilityForActiveQuestion();
            }
        };
        timer.schedule(timertask, 100, Constants.hearbeat);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_question);

        sharedPreferences = getSharedPreferences(Constants.PREFERENCE_NAME, MODE_PRIVATE);
        editor = sharedPreferences.edit();
        questionLayout = (RelativeLayout) findViewById(R.id.layout_question);
        waitingLayout = (RelativeLayout) findViewById(R.id.layout_waitingscreen);
        finishLayout = (RelativeLayout) findViewById(R.id.layout_finish_screen);
        submit = (Button) findViewById(R.id.submit);
        previousQuestionAnswer = (TextView) findViewById(R.id.rightAnswerText);
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        questionNumber = (TextView) findViewById(R.id.questionNumber);
        username = sharedPreferences.getString(Constants.USERNAME, "");
        welcomeMessage = (TextView) findViewById(R.id.welcomeMessage);
        welcomeMessage.setText("Welcome " + username);
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

            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
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
                        Toast.makeText(QuizQuestionActivity.this, "Answered saved succesfully", Toast.LENGTH_LONG).show();
                        waitingLayout.setVisibility(View.VISIBLE);
                        questionLayout.setVisibility(View.GONE);
                        radioGroup.clearCheck();
                    } else if (result.equalsIgnoreCase("FALSE")) {
                        Toast.makeText(QuizQuestionActivity.this, "You have Already answered this question", Toast.LENGTH_LONG).show();
                        waitingLayout.setVisibility(View.VISIBLE);
                        questionLayout.setVisibility(View.GONE);
                        radioGroup.clearCheck();
                    } else {
                        Toast.makeText(QuizQuestionActivity.this, "Issue with server, Try again", Toast.LENGTH_LONG).show();
                        waitingLayout.setVisibility(View.GONE);
                        questionLayout.setVisibility(View.VISIBLE);
                    }
                } else {
                    Toast.makeText(QuizQuestionActivity.this, "Issue with server, Try again", Toast.LENGTH_LONG).show();
                    waitingLayout.setVisibility(View.GONE);
                    questionLayout.setVisibility(View.VISIBLE);
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
            } catch (SocketTimeoutException e) {
                Toast.makeText(getApplicationContext(), "Server Not Responding", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();

            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
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
                    if (result.equalsIgnoreCase("P")) {
                        waitingLayout.setVisibility(View.VISIBLE);
                        questionLayout.setVisibility(View.GONE);
                        finishLayout.setVisibility(View.GONE);

                    } else if (result.equalsIgnoreCase("F")) {
                        finishLayout.setVisibility(View.VISIBLE);
                        waitingLayout.setVisibility(View.GONE);
                        questionLayout.setVisibility(View.GONE);
                        editor.clear().commit();
                        stopTimer();

                    } else {
                        currentQuestionNumber = result;
                        System.out.println("Question Current " + currentQuestionNumber);
                        String currentQuestionNumberFromPreferences = sharedPreferences.getString(Constants.CURRENT_QUESTION_NUMBER, "0");
                        questionNumber.setText(currentQuestionNumber);
                        if (!currentQuestionNumberFromPreferences.equalsIgnoreCase(currentQuestionNumber)) {
                            editor.putString(Constants.CURRENT_QUESTION_NUMBER, currentQuestionNumber).commit();


                                showDelayedQuestionAfterAnswer();


                        } else {

                        }
                    }

                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

       /* public void showDelayedAnswer() {
            Timer timer = new Timer();
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {


                        }
                    });


                }
            };
            timer.schedule(timerTask, 30000);
        }*/

        public void showDelayedQuestionAfterAnswer() {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            waitingLayout.setVisibility(View.GONE);
                            questionLayout.setVisibility(View.VISIBLE);
                            finishLayout.setVisibility(View.GONE);
                        }
                    });


        }
    }

}
