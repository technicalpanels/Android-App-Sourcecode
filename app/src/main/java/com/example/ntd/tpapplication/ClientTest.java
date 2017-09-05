package com.example.ntd.tpapplication;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class ClientTest extends AppCompatActivity {

    private EditText editIP, editPort;
    private Button btnHello, btnYes, btnPing;
    private Socket socket = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_test);
        InitBinding();
    }

    public void InitBinding() {
        editIP = (EditText) findViewById(R.id.txtIP);
        editPort = (EditText) findViewById(R.id.txtPort);
        btnHello = (Button) findViewById(R.id.btn1);
        btnYes = (Button) findViewById(R.id.btn2);
        btnPing = (Button) findViewById(R.id.btn3);
        btnHello.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Thread clientThread = new Thread(new ClientThread("Hello"));
                clientThread.start();
            }
        });
        btnYes.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Thread clientThread = new Thread(new ClientThread("Yes"));
                clientThread.start();
            }
        });
        btnPing.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Thread clientThread = new Thread(new PingThread());
                clientThread.start();
            }
        });
    }

    public class PingThread implements Runnable {
        @Override
        public void run() {
            try {
                String HOST = editIP.getText().toString();
                int PORT = Integer.parseInt(editPort.getText().toString());
                InetAddress serverAddr = InetAddress.getByName(HOST);
                SocketAddress sockaddr = new InetSocketAddress(serverAddr, PORT);
                // Create an unbound socket
                Socket sock = new Socket();
                int timeoutMs = 2000;   // 2 seconds
                sock.connect(sockaddr, timeoutMs);
                boolean checkConnection = true;
                if (checkConnection) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ClientTest.this, "Works", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ClientTest.this, "Not found", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } catch (Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ClientTest.this, "ERROR", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        }
    }

    public class ClientThread implements Runnable {
        String message;

        ClientThread(String message) {
            this.message = message;
        }

        String HOST;
        int PORT;

        @Override
        public void run() {
            try {
                HOST = editIP.getText().toString();
                InetAddress serverAddr = InetAddress.getByName(HOST);
                PORT = Integer.parseInt(editPort.getText().toString());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ClientTest.this, HOST + " " + PORT, Toast.LENGTH_SHORT).show();
                    }
                });
                socket = new Socket(serverAddr, PORT);
                PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                out.println(message);

            } catch (Exception e) {

            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_client_test, menu);
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
