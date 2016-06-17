package example.com.bluetoothble;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity {

    private BLEPeripheral blePeri;
    private CheckBox  adverstiseCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        adverstiseCheckBox = (CheckBox) findViewById(R.id.advertise_checkBox);

        blePeri = new BLEPeripheral();


        adverstiseCheckBox.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if(true == adverstiseCheckBox.isChecked())
                {
                    TextView textView;
                    textView = (TextView)findViewById(R.id.status_text);
                    textView.setText("advertising");
                    blePeri.startAdvertise();
                }
                else
                {
                    TextView textView;
                    textView = (TextView)findViewById(R.id.status_text);
                    textView.setText("disable");
                    blePeri.stopAdvertise();
                }
            }
        });

        adverstiseCheckBox.setEnabled(false);

        if(false == BLEPeripheral.isEnableBluetooth())
        {

            Intent intentBtEnabled = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            // The REQUEST_ENABLE_BT constant passed to startActivityForResult() is a locally defined integer (which must be greater than 0), that the system passes back to you in your onActivityResult()
            // implementation as the requestCode parameter.
            int REQUEST_ENABLE_BT = 1;
            startActivityForResult(intentBtEnabled, REQUEST_ENABLE_BT);

            Toast.makeText(this, "Please enable bluetooth and execute the application agagin.",
                    Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onResume(){

        super.onResume();

        int sts;
        sts = blePeri.init(this);

        if(0  > sts)
        {
            if(-1 == sts)
                Toast.makeText(this, "this device is without bluetooth module",
                        Toast.LENGTH_LONG).show();

            if(-2 == sts)
                Toast.makeText(this, "this device do not support Bluetooth low energy",
                        Toast.LENGTH_LONG).show();

            if(-3 == sts)
                Toast.makeText(this, "this device do not support to be a BLE peripheral, " +
                                "please buy nexus 6 or 9 then try again",
                        Toast.LENGTH_LONG).show();

            finish();
        }

        TextView textView;
        textView = (TextView)findViewById(R.id.mac_text);

        textView.setText(BLEPeripheral.getAddress());

        adverstiseCheckBox.setEnabled(true);
    }



    @Override
    protected void onStop() {
        super.onStop();
    }


}
