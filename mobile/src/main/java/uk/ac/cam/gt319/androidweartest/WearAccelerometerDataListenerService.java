package uk.ac.cam.gt319.androidweartest;

import android.util.Log;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.WearableListenerService;

import uk.ac.cam.gt319.accelerometerdata.AccelerometerDataBlob;

/**
 * Created by George on 15/12/14.
 */
public class WearAccelerometerDataListenerService extends WearableListenerService {

  private static final String TAG = "PhoneDataListener";

  @Override
  public void onDataChanged(DataEventBuffer dataEvents) {
    Log.d(TAG, "onDataChanged called");
    for (DataEvent d : dataEvents) {
      DataMap dm = DataMap.fromByteArray(d.getDataItem().getData());
      AccelerometerDataBlob dataBlob = new AccelerometerDataBlob(dm.getByteArray("AccelDataFromWear"));
    }
  }
}
