package uk.ac.cam.gt319.androidweartest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.ToggleButton;

import uk.ac.cam.gt319.accelerometerdata.AccelerometerDataCaptureService;

public class MainActivity extends Activity {

  private final String TAG = "WearMainActivity";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);

    stub.inflate();
  }

  public void onToggleClicked(View view) {
    if (((ToggleButton) view).isChecked()) {
      Log.d(TAG, "Registering listener.");
      startService(new Intent(this, AccelerometerDataCaptureService.class));
    } else {
      stopService(new Intent(this, AccelerometerDataCaptureService.class));
    }
  }
}
