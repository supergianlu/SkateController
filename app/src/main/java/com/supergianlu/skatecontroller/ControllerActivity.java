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

import abak.tr.com.boxedverticalseekbar.BoxedVertical;

public class ControllerActivity extends AppCompatActivity {

    Button disconnectButton;
    BoxedVertical boxedVertical;
    String address = null;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        address = intent.getStringExtra(MainActivity.EXTRA_ADDRESS);

        setContentView(R.layout.activity_controller);

        boxedVertical = findViewById(R.id.boxed_vertical);

        boxedVertical.setOnBoxedPointsChangeListener(new BoxedVertical.OnValuesChangeListener() {
            @Override
            public void onPointsChanged(BoxedVertical boxedPoints, final int value) {
                //qui stampa tutti i valori
                sendSignal(String.valueOf(value));
                System.out.println(value);
            }

            @Override
            public void onStartTrackingTouch(BoxedVertical boxedPoints) {
                //Toast.makeText(ControllerActivity.this, "onStartTrackingTouch", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStopTrackingTouch(BoxedVertical boxedPoints) {
                //qui restituisce l'ultimo valore quando si è lasciato il robo
                //Toast.makeText(ControllerActivity.this, "onStopTrackingTouch", Toast.LENGTH_SHORT).show();
            }
        });

        disconnectButton = findViewById(R.id.button);

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

    private void sendSignal ( String number ) {
        if ( btSocket != null ) {
            try {
                btSocket.getOutputStream().write(number.getBytes());
            } catch (IOException e) {
                msg("Errore");
            }
        }
    }

    private void disconnect() {
        slowDownAndTurnOff();
        if ( btSocket!=null ) {
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

    private void slowDownAndTurnOff(){
        sendSignal("4");
    }

    @Override
    public void onPause(){
        slowDownAndTurnOff();
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
                slowDownAndTurnOff();
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                System.out.println("Volume down");
                slowDownAndTurnOff();
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
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();
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
