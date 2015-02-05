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
import android.os.ParcelFileDescriptor;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi.DataItemResult;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Listener for accelerometer data. Stores data in an {@link AccelerometerDataBlob}.
 *
 */
public abstract class AccelerometerDataCaptureService extends Service implements SensorEventListener {

  private AccelerometerDataBlob dataBlob;
  private SensorManager sensorManager;
  private Sensor accelerometer;
  private final String TAG = "AccelerometerDataCaptureService";
  private WakeLock wakeLock;

  public WakeLock getWakeLock() {
    return wakeLock;
  }

  public SensorManager getSensorManager() {
    return sensorManager;
  }

  public AccelerometerDataBlob getDataBlob() {
    return dataBlob;
  }

  @Override
  public void onCreate() {
  }

  public abstract void onDestroy();

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    Log.d(TAG, "On Create called.");
    dataBlob = new AccelerometerDataBlob(new File(getFilesDir(), genFilename()));
    sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
        "MyWakelockTag");
    wakeLock.acquire();
    Log.d(TAG, "Sensors registered.");
    Log.d(TAG, "Registering listener.");
    sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
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

  private String genFilename() {
    DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHms");
    String filename = dateFormat.format(new Date()) + ".tmp";
    return filename;
  }
}
