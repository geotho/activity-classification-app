package uk.ac.cam.gt319.androidweartest;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageApi.SendMessageResult;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.NodeApi.GetConnectedNodesResult;
import com.google.android.gms.wearable.Wearable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


public class MainActivity extends Activity {

  private static final String TAG = "MobileMainActivity";
  private GoogleApiClient googleApiClient;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    Spinner spinner = (Spinner) findViewById(R.id.spinner);
    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
        R.array.activities_array, android.R.layout.simple_spinner_item);

    googleApiClient = buildGoogleApiClient();
    googleApiClient.connect();

    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

    spinner.setAdapter(adapter);
    Log.d(TAG, "created");
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

  public void sendMessage(View view) {
    for (String s : getNodes()) {
      Log.d(TAG, "Sending to node " + s);
      PendingResult<SendMessageResult> result =
          Wearable.MessageApi.sendMessage(googleApiClient, s, "/start/MainActivity", null);

      result.setResultCallback(new ResultCallback<SendMessageResult>() {
        @Override
        public void onResult(MessageApi.SendMessageResult sendMessageResult) {
          Log.d(TAG, "Sent message");
        }
      });
    }
  }

  private GoogleApiClient buildGoogleApiClient() {
    return new GoogleApiClient.Builder(this)
        .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
          @Override
          public void onConnected(Bundle connectionHint) {
            Log.d(TAG, "onConnected: " + connectionHint);
          }

          @Override
          public void onConnectionSuspended(int cause) {
            Log.d(TAG, "onConnectionSuspended: " + cause);
          }
        })
        .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
          @Override
          public void onConnectionFailed(ConnectionResult result) {
            Log.d(TAG, "onConnectionFailed: " + result);
          }
        })
        .addApi(Wearable.API)
        .build();
  }

  private Collection<String> getNodes() {
    Log.d(TAG, "Getting nodes now");
    final Set<String> results = new HashSet<>();
    PendingResult<GetConnectedNodesResult> result =
        Wearable.NodeApi.getConnectedNodes(googleApiClient);

    result.setResultCallback(new ResultCallback<GetConnectedNodesResult>() {
      @Override
      public void onResult(GetConnectedNodesResult nodes) {
        for (Node node : nodes.getNodes()) {
          Log.d(TAG, "Adding node " + node.getId());
          results.add(node.getId());

          PendingResult<SendMessageResult> result =
              Wearable.MessageApi.sendMessage(googleApiClient, node.getId(), "/start/MainActivity", null);

          result.setResultCallback(new ResultCallback<SendMessageResult>() {
            @Override
            public void onResult(MessageApi.SendMessageResult sendMessageResult) {
              Log.d(TAG, "Sent message");
            }
          });
        }
      }
    });
    Log.d(TAG, "results size = " + results.size());
    return results;
  }
}
