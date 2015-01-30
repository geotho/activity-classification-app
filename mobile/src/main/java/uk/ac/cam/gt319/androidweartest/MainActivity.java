package uk.ac.cam.gt319.androidweartest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Chronometer;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi.MessageListener;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

import uk.ac.cam.gt319.accelerometerdata.AccelerometerDataCaptureService;


public class MainActivity extends Activity implements MessageListener {

  private static final String TAG = "MobileMainActivity";
  private GoogleApiClient googleApiClient;
  private Spinner activitySpinner;
  private TextView userNameTextView;
  private Chronometer chronometer;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    activitySpinner = (Spinner) findViewById(R.id.spinner);
    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
        R.array.activities_array, android.R.layout.simple_spinner_item);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    activitySpinner.setAdapter(adapter);

    userNameTextView = (TextView) findViewById(R.id.nameText);

    chronometer = (Chronometer) findViewById(R.id.chronometer);

    googleApiClient = buildGoogleApiClient();
    googleApiClient.connect();
    Log.d(TAG, "created");
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

  private GoogleApiClient buildGoogleApiClient() {
    return new GoogleApiClient.Builder(this)
        .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
          @Override
          public void onConnected(Bundle connectionHint) {
            Log.d(TAG, "onConnected: " + connectionHint);
            registerListener();
          }

          @Override
          public void onConnectionSuspended(int cause) {
            Log.d(TAG, "onConnectionSuspended: " + cause);
          }
        })
        .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
          @Override
          public void onConnectionFailed(ConnectionResult result) {
            Log.d(TAG, "onConnectionFailed: " + result);
          }
        })
        .addApi(Wearable.API)
        .build();
  }

  @Override
  public void onMessageReceived(MessageEvent messageEvent) {
    Log.d(TAG, "Message received...");
    Log.d(TAG, "Message path = " + messageEvent.getPath());
    if (messageEvent.getPath().equals("/start/MainActivity")) {
      Log.d(TAG, "Start message received.");
      sendFilename();
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          chronometer.setBase(SystemClock.elapsedRealtime());
          chronometer.start();
          Toast.makeText(MainActivity.this, "Started recording", Toast.LENGTH_SHORT).show();
        }
      });
      startService(new Intent(this, PhoneAccelerometerDataCaptureService.class)
          .setAction(PhoneAccelerometerDataCaptureService.SET_FILE_NAME_ACTION)
          .putExtra("username", getUserName())
          .putExtra("useractivity", getUserActivity()));
    } else if (messageEvent.getPath().equals("/end/MainActivity")) {
      Log.d(TAG, "Stop message received.");
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          chronometer.stop();
          Toast.makeText(MainActivity.this, "Stopped recording", Toast.LENGTH_SHORT).show();
        }
      });
      stopService(new Intent(this, PhoneAccelerometerDataCaptureService.class));
    }
  }

  private void registerListener() {
    Wearable.MessageApi.addListener(googleApiClient, this);
    Log.d(TAG, "Message API listener added");
  }

  private String getUserName() {
    return userNameTextView.getText().toString();
  }

  private String getUserActivity() {
    return activitySpinner.getSelectedItem().toString();
  }

  private void sendFilename() {
    startService(new Intent(this, WearAccelerometerDataListenerService.class)
        .setAction(WearAccelerometerDataListenerService.SET_FILE_NAME_ACTION)
        .putExtra("username", getUserName())
        .putExtra("useractivity", getUserActivity()));
  }

}
