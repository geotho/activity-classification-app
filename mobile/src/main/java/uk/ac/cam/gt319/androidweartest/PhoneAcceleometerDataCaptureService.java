package uk.ac.cam.gt319.androidweartest;

import android.content.Intent;
import android.util.Log;

import uk.ac.cam.gt319.accelerometerdata.AccelerometerDataCaptureService;
import uk.ac.cam.gt319.accelerometerdata.FileSaver;

/**
 * Created by George on 30/01/15.
 */
public class PhoneAcceleometerDataCaptureService extends AccelerometerDataCaptureService {

  public static final String SET_FILE_NAME_ACTION = "SET_FILE_NAME_ACTION";
  private static final String TAG = "PhoneAcceleometerDataCaptureService";
  private String filename;

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    Log.d(TAG, "On Start of Phone Data Capture Service called.");
    if (intent.getAction().equals(SET_FILE_NAME_ACTION)) {
      setFilename(intent.getStringExtra("username") + "-" + intent.getStringExtra("useractivity"));
    }
    return super.onStartCommand(intent, flags, startId);
  }

  @Override
  public void onDestroy() {
    getSensorManager().unregisterListener(this);
    getDataBlob().done();
    FileSaver fileSaver = new FileSaver(this.filename);
    fileSaver.saveToDisk(getDataBlob().getFile());
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }
}
