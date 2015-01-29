package uk.ac.cam.gt319.accelerometerdata;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataApi.DataItemResult;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

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
    dataBlob = new AccelerometerDataBlob(new File(getFilesDir(), genFilename()));

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
    try {
      dataBlob.add(event);
    } catch (IOException e) {
      Log.e(TAG, "Adding failed", e);
    }
  }

  @Override
  public void onAccuracyChanged(Sensor sensor, int accuracy) {

  }

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  private void sendToPhone(AccelerometerDataBlob dataBlob)  {
    googleApiClient.connect();
    dataBlob.done();

    Log.d(TAG, "Attempting data put");
    PutDataMapRequest dataMapRequest = PutDataMapRequest.create("/acceldata");
    ParcelFileDescriptor parcelFileDescriptor = null;
    try {
      parcelFileDescriptor = ParcelFileDescriptor.open(dataBlob.getFile(), ParcelFileDescriptor.MODE_READ_ONLY);
    } catch (FileNotFoundException e) {
      Log.e(TAG, "File not found.", e);
    }
    Asset asset = Asset.createFromFd(parcelFileDescriptor);
    dataMapRequest.getDataMap().putAsset("AccelDataFromWear", asset);

    PendingResult<DataItemResult> pendingResult = Wearable.DataApi
        .putDataItem(googleApiClient, dataMapRequest.asPutDataRequest());

    pendingResult.setResultCallback(new ResultCallback<DataItemResult>() {
      @Override
      public void onResult(DataItemResult dataItemResult) {

        Log.d(TAG, "Data result: " + dataItemResult.getDataItem());
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

  private String genFilename() {
    DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHms");
    String filename = dateFormat.format(new Date()) + ".tmp";
    return filename;
  }
}
