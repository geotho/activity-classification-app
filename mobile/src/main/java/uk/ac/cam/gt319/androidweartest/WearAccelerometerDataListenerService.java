package uk.ac.cam.gt319.androidweartest;

import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.text.format.Time;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by George on 15/12/14.
 */
public class WearAccelerometerDataListenerService extends WearableListenerService {

  private static final String TAG = "PhoneDataListener";
  private GoogleApiClient googleApiClient;

  @Override
  public void onCreate() {
    super.onCreate();
    Log.d(TAG, "onCreate of Data Listener Service called");
    googleApiClient = buildGoogleApiClient();
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
    File dir = getStorageDir("accelData");
    File file = new File(dir.getPath() + filename);
    Log.d(TAG, dir.getAbsolutePath());
    ConnectionResult result = googleApiClient.blockingConnect(5000, TimeUnit.MILLISECONDS);
    if (!result.isSuccess()) {
      Log.d(TAG, "Unsuccessful connection");
      return;
    }

    InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
        googleApiClient, dataBlobAsset).await().getInputStream();
    googleApiClient.disconnect();


    if (assetInputStream == null) {
      Log.w(TAG, "Requested an unknown Asset.");
      return;
    }

    try {
      file.createNewFile();
      FileOutputStream fileOutputStream = new FileOutputStream(file);
      byte[] buf = new byte[1024];
      int len;
      while ((len = assetInputStream.read(buf)) != -1) {
        fileOutputStream.write(buf, 0, len);
      }
      Log.d(TAG, "Saved to " + file.getAbsolutePath());
      fileOutputStream.close();
    } catch (FileNotFoundException e) {
      Log.wtf(TAG, "Saving to disk - file not found", e);
    } catch (IOException e) {
      Log.wtf(TAG, "Saving to disk - IOException", e);
    }
  }

  private File getStorageDir(String albumName) {
    File file = new File(Environment.getExternalStoragePublicDirectory(
        Environment.DIRECTORY_DOCUMENTS), albumName);
    file.mkdirs();
    return file;
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

  private String genFilename(String prefix) {
    DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHms");
    String filename = "/" + prefix + "-" + dateFormat.format(new Date()) + ".dat";
    return filename;
  }
}
