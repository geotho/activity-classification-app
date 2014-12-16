package uk.ac.cam.gt319.androidweartest;

import android.util.Log;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.Arrays;

/**
 * Created by George on 15/12/14.
 */
public class WearAccelerometerDataListenerService extends WearableListenerService {

  @Override
  public void onDataChanged(DataEventBuffer dataEvents) {
    for (DataEvent d : dataEvents) {
      DataMap dm = DataMap.fromByteArray(d.getDataItem().getData());
      Log.d("PhoneDataListener", "onDataChanged: " + Arrays.toString(dm.getFloatArray("XYZ")));
    }
  }
}
