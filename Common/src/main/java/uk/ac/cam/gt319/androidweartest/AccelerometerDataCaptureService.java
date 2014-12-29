package uk.ac.cam.gt319.androidweartest;

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
  private final int DEFAULT_CAPACITY = 3*60*30;

  @Override
  public void onCreate() {
    dataBlob = new AccelerometerDataBlob(DEFAULT_CAPACITY);

    googleApiClient = buildGoogleApiClient();

    sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
  }

  @Override
  public void onDestroy() {
    sendToPhone(dataBlob);
    sensorManager.unregisterListener(this);
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    return START_STICKY;
  }

  @Override
  public void onSensorChanged(SensorEvent event) {
    dataBlob.add(event);
    if (dataBlob.isFull()) {
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

    PutDataMapRequest dataMap = PutDataMapRequest.create("/acceldata");
    dataMap.getDataMap().putByteArray("AccelDataFromPhone", dataBlob.asByteArray());
    PutDataRequest request = dataMap.asPutDataRequest();

    PendingResult<DataItemResult> pendingResult = Wearable.DataApi
        .putDataItem(googleApiClient, request);

    googleApiClient.disconnect();
  }

  private GoogleApiClient buildGoogleApiClient() {
    final String TAG = "WearDataLayer";
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
