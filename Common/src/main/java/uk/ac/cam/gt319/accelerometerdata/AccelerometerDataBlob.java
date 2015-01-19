package uk.ac.cam.gt319.accelerometerdata;

import android.hardware.SensorEvent;

import com.google.common.base.Preconditions;

import java.nio.ByteBuffer;

/**
 * Stores timestamp-accelerometer data tuples. Fixed capacity assigned created on construction.
 * When full, no more data can be pushed in. Can flatten data for sending across DataApi.
 */
public class AccelerometerDataBlob {
  private final byte[][] DATA;
  private final int capacity;

  public int getCapacity() {
    return capacity;
  }

  private int count;

  /**
   * Create a new, empty AccelerometerDataBlob with fixed capacity.
   * @param capacity the fixed maximum capacity, in number of records, of this blob.
   */
  public AccelerometerDataBlob(int capacity) {
    this.capacity = capacity;
    DATA = new byte[capacity][20];
  }

  /**
   * Create a new AccelerometerDataBlob from byte array: the unmarshalling constructor.
   * @param flattened the byte array to insert into the Blob. Length must be a multiple of 20.
   */
  public AccelerometerDataBlob(byte[] flattened) {
    Preconditions.checkArgument(flattened.length % 20 == 0,
        "Array length must be a multiple of 20 bytes");

    this.capacity = flattened.length/20;
    this.count = capacity;
    DATA = new byte[capacity][20];

    for (int i = 0; i < capacity; i++) {
      for (int j = 0; j < 20; j++) {
        DATA[i][j] = flattened[20*i+j];
      }
    }
  }

  /**
   * Add the timestamp and accelData as a row in the data blob.
   * @param sensorEvent the sensorEvent from which to take the timestamp and accelData.
   */
  public synchronized boolean add(SensorEvent sensorEvent) {
    if (!this.isFull()) {
      DATA[count] = makeByteArray(sensorEvent.timestamp, sensorEvent.values);
      count++;
      // Return true if we are not yet full.
      return !this.isFull();
    }
    return false;
  }

  public byte[] asByteArray() {
    byte[] flattened = new byte[20 * count];
    for (int i = 0; i < count; i++) {
      for (int j = 0; j < 20; j++) {
        flattened[20*i+j] = DATA[i][j];
      }
    }
    return flattened;
  }

  public byte[] makeByteArray(long timestamp, float[] values) {
    return ByteBuffer.allocate(20)
        .putLong(timestamp)
        .putFloat(values[0])
        .putFloat(values[1])
        .putFloat(values[2])
        .array();
  }

  public boolean isFull() {
    return count >= capacity;
  }
}
