package uk.ac.cam.gt319.androidweartest;

import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;

import java.io.FileNotFoundException;

import uk.ac.cam.gt319.accelerometerdata.AccelerometerDataBlob;
import uk.ac.cam.gt319.accelerometerdata.AccelerometerDataCaptureService;

/**
 * Created by George on 30/01/15.
 */
public class WearAccelerometerDataCaptureService extends AccelerometerDataCaptureService {
  private GoogleApiClient googleApiClient;
  private final String TAG = "WearAccelerometerDataCaptureService";

  @Override
  public void onCreate() {
    super.onCreate();
    googleApiClient = buildGoogleApiClient();
  }

  @Override
  public void onDestroy() {
    getSensorManager().unregisterListener(this);
    getDataBlob().done();
    sendToPhone(getDataBlob());
    getWakeLock().release();
  }

  private void sendToPhone(AccelerometerDataBlob dataBlob)  {
    googleApiClient.connect();

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

    PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi
        .putDataItem(googleApiClient, dataMapRequest.asPutDataRequest());

    pendingResult.setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
      @Override
      public void onResult(DataApi.DataItemResult dataItemResult) {

        Log.d(TAG, "Data result: " + dataItemResult.getDataItem());
        getDataBlob().deleteFile();
        googleApiClient.disconnect();
      }
    });
  }

  private GoogleApiClient buildGoogleApiClient() {
    Log.d(TAG, "Google Api Client building.");
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
