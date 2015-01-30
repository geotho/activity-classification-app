package uk.ac.cam.gt319.androidweartest;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import uk.ac.cam.gt319.accelerometerdata.FileSaver;

/**
 * Created by George on 15/12/14.
 */
public class WearAccelerometerDataListenerService extends WearableListenerService {

  public static final String SET_FILE_NAME_ACTION = "SET_FILE_NAME_ACTION";

  private static final String TAG = "PhoneDataListener";
  private GoogleApiClient googleApiClient;

  private String fileName;

  @Override
  public void onCreate() {
    super.onCreate();
    Log.d(TAG, "onCreate of Data Listener Service called");
    googleApiClient = buildGoogleApiClient();
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    Log.d(TAG, "On Start of Data Listener Service called");
    if (intent.getAction().equals(SET_FILE_NAME_ACTION)) {
      setFileName(intent.getStringExtra("username") + "-" + intent.getStringExtra("useractivity"));
    }
    return super.onStartCommand(intent, flags, startId);
  }

  @Override
  public void onDataChanged(DataEventBuffer dataEvents) {
    Log.d(TAG, "onDataChanged called");
    for (DataEvent event : dataEvents) {
      if (event.getType() == DataEvent.TYPE_CHANGED &&
          event.getDataItem().getUri().getPath().equals("/acceldata")) {

        DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
        Asset accelDataAsset = dataMapItem.getDataMap().getAsset("AccelDataFromWear");
        saveToDisk(accelDataAsset);
      }
    }
  }

  private void saveToDisk(Asset dataBlobAsset) {
    ConnectionResult result = googleApiClient.blockingConnect(5000, TimeUnit.MILLISECONDS);
    if (!result.isSuccess()) {
      Log.d(TAG, "Unsuccessful connection");
      return;
    }

    InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
        googleApiClient, dataBlobAsset).await().getInputStream();
    googleApiClient.disconnect();

    FileSaver fileSaver = new FileSaver(getFileName());
    fileSaver.saveToDisk(assetInputStream);
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

  public String getFileName() {
    return "wear-" + fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }
}
