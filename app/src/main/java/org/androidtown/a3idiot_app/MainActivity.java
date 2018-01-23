package org.androidtown.a3idiot_app;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity {

    private Socket socket;
    private static final String SERVER_IP = "192.168.0.7";
    private Socket client;
    private PrintWriter printwriter;
    private BufferedReader bufferedReader;

    int i = 0;
    String str;

    Button operBtn;
    TextView stateText;
    TextView connectText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        operBtn = (Button) findViewById(R.id.operBtn);
        stateText = (TextView) findViewById(R.id.stateText);
        connectText = (TextView) findViewById(R.id.connecText);

        //상단바 숨기기
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        // Toast.makeText(getApplicationContext(), "Server has not bean started on port 8090..",Toast.LENGTH_SHORT).show();

        connectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TCPClient tcpClient = new TCPClient();
                tcpClient.execute();
            }
        });
        operBtn.setEnabled(false);

    }

    private class TCPClient extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                client = new Socket(SERVER_IP, 8090); // 소켓 생성

                if (client != null) {
                    //자동 flushing 기능이 있는 PrintWriter 객체를 생성한다.
                    //client.getOutputStream() 서버에 출력하기 위한 스트림을 얻는다.
                    printwriter = new PrintWriter(client.getOutputStream(), true);

                    //입력 스트림 inputStreamReader에 대해 기본 크기의 버퍼를 갖는 객체를 생성한다.
                    InputStreamReader inputStreamReader = new InputStreamReader(client.getInputStream());
                    bufferedReader = new BufferedReader(inputStreamReader);//서버로부터의 메세지를 읽는 객체
                } else {
                    Log.e("error", "Server has not bean started on port 8090.");
                }
            } catch (UnknownHostException e) {
                Log.e("error", "Faild to connect server " + SERVER_IP);
                e.printStackTrace();
            } catch (IOException e) {
                Log.e("error", "Faild to connect server " + SERVER_IP);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (client != null) {
                operBtn.setEnabled(true);
                operBtn.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        i = 1 - i;
                        if (i == 1) {
                            stateText.setText("The device is turned on.");
                            str = "1";
                        } else {
                            stateText.setText("The device is turned off.");
                            str = "2";
                        }

                        final Sender messageSender = new Sender();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                            messageSender.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, str);
                        } else {
                            messageSender.execute(str);
                        }
                    }
                });
                connectText.setEnabled(false);
                connectText.setPressed(true);
            } else {
                connectText.setEnabled(true);
                connectText.setPressed(false);
                operBtn.setEnabled(false);
            }
        }

        /**
         * 서버에게 메세지전송
         */
        private class Sender extends AsyncTask<String, String, Void> {

            private String message;

            @Override
            protected Void doInBackground(String... params) {
                message = params[0];

                //문자열을 스트림에 기록한다.
                printwriter.write(message + "\n");

                //스트림을 플러쉬한다.
                printwriter.flush();

                return null;
            }

            //클라이언트에서 입력한 문자열을 화면에 출력한다.
            @Override
            protected void onPostExecute(Void result) {

            }
        }
    }
}