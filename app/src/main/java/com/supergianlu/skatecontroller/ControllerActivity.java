package com.supergianlu.skatecontroller;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import java.io.IOException;
import java.util.UUID;

public class ControllerActivity extends AppCompatActivity {

    private static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String STOP = "0";
    private static final String NEXT_GEAR = "1";

    private String address = null;
    private ProgressDialog progress;
    private BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    private long timeVolumeUp;
    private long timeVolumeDown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        address = intent.getStringExtra(MainActivity.EXTRA_ADDRESS);

        timeVolumeUp = System.currentTimeMillis();
        timeVolumeDown = System.currentTimeMillis();

        setContentView(R.layout.activity_controller);

        final Button disconnectButton = findViewById(R.id.button);

        new ConnectBT().execute();


        disconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                Toast.makeText(getApplicationContext(), "Tenere premuto per disconnettere", Toast.LENGTH_LONG).show();
            }
        });

        disconnectButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                disconnect();
                return true;
            }
        });
    }

    private void sendSignal (String number) {
        if (btSocket != null) {
            try {
                btSocket.getOutputStream().write(number.getBytes());
            } catch (IOException e) {
                msg("Errore");
            }
        }
    }

    private void disconnect() {
        slowSpeed();
        if (btSocket!=null) {
            try {
                btSocket.close();
            } catch(IOException e) {
                msg("Errore");
            }
        }
        finish();
    }

    private void msg (String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

    private void slowSpeed(){
        sendSignal(STOP);
    }

    @Override
    public void onPause(){
        slowSpeed();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        disconnect();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                System.out.println("Volume up");
                if(System.currentTimeMillis() - timeVolumeUp > 500){
                    timeVolumeUp = System.currentTimeMillis();
                    sendSignal(NEXT_GEAR);
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                System.out.println("Volume down");
                if(System.currentTimeMillis() - timeVolumeDown > 500){
                    timeVolumeDown = System.currentTimeMillis();
                    slowSpeed();
                }
                return true;
            default:
                return super.dispatchKeyEvent(event);
        }
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void> {
        private boolean connectSuccess = true;

        @Override
        protected  void onPreExecute () {
            progress = ProgressDialog.show(ControllerActivity.this, "Connessione in corso...", "Attendere!");
        }

        @Override
        protected Void doInBackground (Void... devices) {
            try {
                if ( btSocket==null || !isBtConnected ) {
                    final BluetoothAdapter myBluetooth = BluetoothAdapter.getDefaultAdapter();
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();
                }
            } catch (IOException e) {
                connectSuccess = false;
            }

            return null;
        }

        @Override
        protected void onPostExecute (Void result) {
            super.onPostExecute(result);

            if (!connectSuccess) {
                msg("Connessione fallita.");
                finish();
            } else {
                msg("Connessione riuscita");
                isBtConnected = true;
            }

            progress.dismiss();
        }
    }

}
