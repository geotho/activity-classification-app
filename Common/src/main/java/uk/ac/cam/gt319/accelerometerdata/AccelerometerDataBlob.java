package uk.ac.cam.gt319.accelerometerdata;

import android.hardware.SensorEvent;

import java.io.BufferedOutputStream;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Stores timestamp-accelerometer data tuples. Can return asset for DataApi.
 * Saves data into a tmp file.
 */
public class AccelerometerDataBlob {

  private int count;
  private BufferedOutputStream output;
  private FileDescriptor fileDescriptor;

  /**
   * Create a new, empty AccelerometerDataBlob.
   */
  public AccelerometerDataBlob(FileOutputStream fileOutputStream) {
    this.count = 0;
    try {
      fileDescriptor = fileOutputStream.getFD();
      output = new BufferedOutputStream(fileOutputStream);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Append timestamp and accelData to the file.
   * @param sensorEvent the sensorEvent from which to take the timestamp and accelData.
   */
  public void add(SensorEvent sensorEvent) throws IOException {
    output.write(makeByteArray(sensorEvent.timestamp, sensorEvent.values));
    count++;
  }

  public byte[] makeByteArray(long timestamp, float[] values) {
    return ByteBuffer.allocate(20)
        .putLong(timestamp)
        .putFloat(values[0])
        .putFloat(values[1])
        .putFloat(values[2])
        .array();
  }

  public FileDescriptor getFileDescriptor() {
    return fileDescriptor;
  }

  public int getCount() {
    return count;
  }
}
