package uk.ac.cam.gt319.androidweartest;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
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
    senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

    x = (TextView) findViewById(R.id.xAccel);
    y = (TextView) findViewById(R.id.yAccel);
    z = (TextView) findViewById(R.id.zAccel);
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true;
    }

    return super.onOptionsItemSelected(item);
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
