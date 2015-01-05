package uk.ac.cam.gt319.androidweartest;

import android.os.Environment;
import android.text.format.Time;
import android.util.Log;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

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
      saveToDisk(dm.getByteArray("AccelDataFromWear"));
    }
  }

  private void saveToDisk(byte[] dataBlobArray) {
    DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
    Date date = new Date();
    String filename = dateFormat.format(date);
    File dir = getStorageDir("accelData");
    dir.mkdirs();
    File file = new File(dir.getPath() + filename);
    Log.d(TAG, dir.getAbsolutePath());
    try {
      file.createNewFile();
      FileOutputStream f = new FileOutputStream(file);
      f.write(dataBlobArray);
      f.close();
    } catch (FileNotFoundException e) {
      Log.wtf(TAG, "Saving to disk - file not found", e);
    } catch (IOException e) {
      Log.wtf(TAG, "Saving to disk - IOException", e);
    }
  }

  private File getStorageDir(String albumName) {
    File file = new File(Environment.getExternalStoragePublicDirectory(
        Environment.DIRECTORY_DOCUMENTS), albumName);
    if (!file.mkdirs()) {
      Log.e(TAG, "Directory not created");
    }
    return file;
  }
}
