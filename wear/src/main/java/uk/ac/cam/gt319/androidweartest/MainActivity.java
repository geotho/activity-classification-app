package uk.ac.cam.gt319.androidweartest;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.widget.TextView;

public class MainActivity extends Activity implements SensorEventListener {

  private SensorManager senSensorManager;
  private Sensor senAccelerometer;

  private TextView x;
  private TextView y;
  private TextView z;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL + 7);


    final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
    stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
      @Override
      public void onLayoutInflated(WatchViewStub stub) {
        x = (TextView) stub.findViewById(R.id.xAccel);
        y = (TextView) stub.findViewById(R.id.yAccel);
        z = (TextView) stub.findViewById(R.id.zAccel);
      }
    });
  }

  @Override
  public void onSensorChanged(SensorEvent event) {
    x.setText(String.format("%.2f", event.values[0]));
    y.setText(String.format("%.2f", event.values[1]));
    z.setText(String.format("%.2f", event.values[2]));
  }

  @Override
  public void onAccuracyChanged(Sensor sensor, int accuracy) {
  }
}
