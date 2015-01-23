package uk.ac.cam.gt319.accelerometerdata;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataApi.DataItemResult;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

/**
 * Listener for accelerometer data. Stores data in an {@link AccelerometerDataBlob}.
 *
 */
public class AccelerometerDataCaptureService extends Service implements SensorEventListener {

  private GoogleApiClient googleApiClient;
  private AccelerometerDataBlob dataBlob;
  private SensorManager sensorManager;
  private Sensor accelerometer;
  private final String TAG = "AccelerometerDataCaptureService";

  @Override
  public void onCreate() {
    Log.d(TAG, "On Create called.");
    dataBlob = new AccelerometerDataBlob();

    googleApiClient = buildGoogleApiClient();
    Log.d(TAG, "Google Api Client built.");

    sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    Log.d(TAG, "Sensors registered.");
  }

  @Override
  public void onDestroy() {
    sendToPhone(dataBlob);
    sensorManager.unregisterListener(this);
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    Log.d(TAG, "Registering listener.");
    sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
    return START_STICKY;
  }

  @Override
  public void onSensorChanged(SensorEvent event) {
    dataBlob.add(event);
    Log.d(TAG, "Capacity is " + dataBlob.getCapacity());
    if (dataBlob.isFull()) {
      Log.d(TAG, "We're full. Attempting data push.");
      sendToPhone(dataBlob);
      dataBlob = new AccelerometerDataBlob(DEFAULT_CAPACITY);
    }
  }

  @Override
  public void onAccuracyChanged(Sensor sensor, int accuracy) {

  }

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  private void sendToPhone(AccelerometerDataBlob dataBlob) {
    googleApiClient.connect();

    Log.d(TAG, "Attempting data put");
    PutDataMapRequest dataMap = PutDataMapRequest.create("/acceldata");
    dataMap.getDataMap().putByteArray("AccelDataFromWear", dataBlob.asByteArray());
    PutDataRequest request = dataMap.asPutDataRequest();

    PendingResult<DataItemResult> pendingResult = Wearable.DataApi
        .putDataItem(googleApiClient, request);

    pendingResult.setResultCallback(new ResultCallback<DataItemResult>() {
      @Override
      public void onResult(DataItemResult dataItemResult) {

        Log.d(TAG, "Data result: " + dataItemResult.toString());
        googleApiClient.disconnect();
      }
    });
  }

  private GoogleApiClient buildGoogleApiClient() {
    return new GoogleApiClient.Builder(this)
        .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
          @Override
          public void onConnected(Bundle connectionHint) {
            Log.d(TAG, "onConnected: " + connectionHint);
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
}
