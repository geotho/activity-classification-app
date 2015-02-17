package uk.ac.cam.gt319.accelerometerdata;

import android.hardware.SensorEvent;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Stores timestamp-accelerometer data tuples. Can return asset for DataApi.
 * Saves data into a tmp file.
 */
public class AccelerometerDataBlob {

  private static final String TAG = "AccelerometerDataBlob";
  private int count;
  private BufferedOutputStream output;
  private File file;

  /**
   * Create a new, empty AccelerometerDataBlob.
   */
  public AccelerometerDataBlob(File file) {
    this.count = 0;
    this.file = file;
    try {
      output = new BufferedOutputStream(new FileOutputStream(file));
    } catch (Exception e) {
      e.printStackTrace();
    }
    Log.d(TAG, "Free space: " + file.getFreeSpace());
  }

  /**
   * Append timestamp and accelData to the file.
   * @param sensorEvent the sensorEvent from which to take the timestamp and accelData.
   */
  public void add(SensorEvent sensorEvent) throws IOException {
    output.write(makeByteArray(sensorEvent.timestamp, sensorEvent.values));
    count++;

//    Log.d(TAG, sensorEvent.timestamp + " successfully written to file.");
  }

  public byte[] makeByteArray(long timestamp, float[] values) {
    return ByteBuffer.allocate(20)
        .putLong(timestamp)
        .putFloat(values[0])
        .putFloat(values[1])
        .putFloat(values[2])
        .array();
  }

  public void done() {
    try {
      output.flush();
      output.close();
      Log.d(TAG, "Blob successfully closed file.");
    } catch (IOException e) {
      Log.e(TAG, "IOException in Done", e);
    }
  }

  public File getFile() {
    return file;
  }

  public int getCount() {
    return count;
  }

  public boolean deleteFile() {
    return getFile().delete();
  }
}
