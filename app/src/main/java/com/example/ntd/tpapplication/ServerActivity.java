package com.example.ntd.tpapplication;

import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.ServerSocket;

public class ServerActivity extends AppCompatActivity {
    Socket socket = null;
    Socket clientSocket = null;
    private Button temp,temp2,temp3;
    private TextView text1;
    Thread serverThread = null;
    private ServerSocket serverSocket;
    public static final int SERVERPORT = 6000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);
        WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        text1 = (TextView) findViewById(R.id.textView);
        text1.setText(ip);
        temp = (Button)findViewById(R.id.buttonTemp);
        temp2 = (Button)findViewById(R.id.button);
        temp3 = (Button)findViewById(R.id.button2);
        temp.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(ServerActivity.this,"TEST",Toast.LENGTH_SHORT).show();
                try {
                    if(socket!=null) {
                        PrintWriter out = new PrintWriter(new BufferedWriter(
                                new OutputStreamWriter(socket.getOutputStream())), true);
                        out.println("ACT:A");
                    }
                    else
                        Toast.makeText(ServerActivity.this,"Error",Toast.LENGTH_SHORT).show();

                }
                catch(Exception e)
                {
                    Toast.makeText(ServerActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                }
            }
        });
        temp2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(ServerActivity.this,"TEST",Toast.LENGTH_SHORT).show();
                try {
                    if(socket!=null) {
                        PrintWriter out = new PrintWriter(new BufferedWriter(
                                new OutputStreamWriter(socket.getOutputStream())), true);
                        out.println("ACT:B");
                    }
                    else
                        Toast.makeText(ServerActivity.this,"Error",Toast.LENGTH_SHORT).show();

                }
                catch(Exception e)
                {
                    Toast.makeText(ServerActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                }
            }
        });
        temp3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(ServerActivity.this,"TEST",Toast.LENGTH_SHORT).show();
                try {
                    if(socket!=null) {
                        PrintWriter out = new PrintWriter(new BufferedWriter(
                                new OutputStreamWriter(socket.getOutputStream())), true);
                        out.println("ACT:C");
                    }
                    else
                        Toast.makeText(ServerActivity.this,"Error",Toast.LENGTH_SHORT).show();

                }
                catch(Exception e)
                {
                    Toast.makeText(ServerActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                }
            }
        });
        this.serverThread = new Thread(new ServerThread());
        this.serverThread.start();
    }


    public class ServerThread implements Runnable {
        private BufferedReader input;
        String input2 = "";
        @Override
        public void run() {

            try {
                serverSocket = new ServerSocket(SERVERPORT);
            } catch (Exception e) {
            }
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    socket = serverSocket.accept();
                    clientSocket = socket;
                    input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    input2 = input2 +"\r\n" + input.readLine();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                text1.setText(input2);
                            } catch (Exception e) {
                                Toast.makeText(ServerActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                }
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_server, menu);
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
}
