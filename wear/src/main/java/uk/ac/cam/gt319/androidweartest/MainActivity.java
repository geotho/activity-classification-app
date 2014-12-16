package uk.ac.cam.gt319.androidweartest;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataApi.DataItemResult;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

public class MainActivity extends Activity implements SensorEventListener {

  private final String TAG = "WearMainActivity";
  private GoogleApiClient googleApiClient;
  private SensorManager senSensorManager;
  private Sensor senAccelerometer;
  private TextView x;
  private TextView y;
  private TextView z;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    googleApiClient = buildGoogleApiClient();


    senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

    final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
    stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
      @Override
      public void onLayoutInflated(WatchViewStub stub) {
        x = (TextView) stub.findViewById(R.id.xAccel);
        y = (TextView) stub.findViewById(R.id.yAccel);
        z = (TextView) stub.findViewById(R.id.zAccel);
      }
    });

    stub.inflate();
  }

  @Override
  protected void onStart() {
    super.onStart();
    googleApiClient.connect();
    senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
  }

  @Override
  public void onSensorChanged(SensorEvent event) {
    PutDataMapRequest dataMap = PutDataMapRequest.create("/acceldata");

    dataMap.getDataMap().putFloatArray("XYZ", event.values);
    PutDataRequest request = dataMap.asPutDataRequest();
    PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi
        .putDataItem(googleApiClient, request);

    pendingResult.setResultCallback(new ResultCallback<DataItemResult>() {
      @Override
      public void onResult(final DataItemResult result) {
        if (result.getStatus().isSuccess()) {
          Log.d(TAG, "Data item set: " + result.getDataItem().getData());
        }
      }
    });

//    Log.d(TAG,
//        String.format("%.2f, %.2f, %.2f", event.values[0], event.values[1], event.values[2]));

    x.setText(String.format("%.2f", event.values[0]));
    y.setText(String.format("%.2f", event.values[1]));
    z.setText(String.format("%.2f", event.values[2]));
  }

  @Override
  public void onAccuracyChanged(Sensor sensor, int accuracy) {
  }

  private GoogleApiClient buildGoogleApiClient() {
    final String TAG = "WearDataLayer";
    return new GoogleApiClient.Builder(this)
        .addConnectionCallbacks(new ConnectionCallbacks() {
          @Override
          public void onConnected(Bundle connectionHint) {
            Log.d(TAG, "onConnected: " + connectionHint);
            // Now you can use the data layer API
          }

          @Override
          public void onConnectionSuspended(int cause) {
            Log.d(TAG, "onConnectionSuspended: " + cause);
          }
        })
        .addOnConnectionFailedListener(new OnConnectionFailedListener() {
          @Override
          public void onConnectionFailed(ConnectionResult result) {
            Log.d(TAG, "onConnectionFailed: " + result);
          }
        })
        .addApi(Wearable.API)
        .build();
  }
}
