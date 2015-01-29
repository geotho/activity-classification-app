package uk.ac.cam.gt319.androidweartest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageApi.MessageListener;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import uk.ac.cam.gt319.accelerometerdata.AccelerometerDataCaptureService;

public class MainActivity extends Activity {

  private final String TAG = "WearMainActivity";
  private GoogleApiClient googleApiClient;
  private CompoundButton button;
  private WatchViewStub stub;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    googleApiClient = buildGoogleApiClient();
    googleApiClient.connect();

    stub = (WatchViewStub) findViewById(R.id.watch_view_stub);

    stub.inflate();

    button = (ToggleButton) findViewById(R.id.evildoombutton);
  }

  public void onToggleClicked(View view) {
    if (((ToggleButton) view).isChecked()) {
      startService(new Intent(this, AccelerometerDataCaptureService.class));
      sendMessageToNodes("start");
    } else {
      stopService(new Intent(this, AccelerometerDataCaptureService.class));
      sendMessageToNodes("end");
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

  private void sendMessageToNodes(final String message) {
    Log.d(TAG, "Getting nodes now");
    PendingResult<NodeApi.GetConnectedNodesResult> result =
        Wearable.NodeApi.getConnectedNodes(googleApiClient);

    result.setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
      @Override
      public void onResult(NodeApi.GetConnectedNodesResult nodes) {
        for (Node node : nodes.getNodes()) {
          Log.d(TAG, "Adding node " + node.getId());

          PendingResult<MessageApi.SendMessageResult> result =
              Wearable.MessageApi.sendMessage(
                  googleApiClient, node.getId(), "/" + message + "/MainActivity", null);

          result.setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
            @Override
            public void onResult(MessageApi.SendMessageResult sendMessageResult) {
              Log.d(TAG, "Sent message");
            }
          });
        }
      }
    });
  }
}
