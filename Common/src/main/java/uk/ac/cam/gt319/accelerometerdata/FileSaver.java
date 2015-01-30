package uk.ac.cam.gt319.accelerometerdata;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by George on 30/01/15.
 */
public class FileSaver {

  private static final String TAG = "FileSaver";
  private final String filename;

  public FileSaver(String filename) {
    this.filename = filename;
  }

  private String genFilename() {
    DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHms");
    return "/" + this.filename + "-" + dateFormat.format(new Date()) + ".dat";
  }

  public void saveToDisk(File file) {
    try {
      saveToDisk(new FileInputStream(file));
    } catch (FileNotFoundException e) {
      Log.e(TAG, "File not found.", e);
    }
  }

  public void saveToDisk(InputStream assetInputStream) {
    File dir = getStorageDir("accelData");
    String filename = genFilename();
    File file = new File(dir.getPath() + filename);
    Log.d(TAG, dir.getAbsolutePath());

    if (assetInputStream == null) {
      Log.w(TAG, "Requested an unknown Asset.");
      return;
    }

    try {
      file.createNewFile();
      FileOutputStream fileOutputStream = new FileOutputStream(file);
      byte[] buf = new byte[1024];
      int len;
      while ((len = assetInputStream.read(buf)) != -1) {
        fileOutputStream.write(buf, 0, len);
      }
      Log.d(TAG, "Saved to " + file.getAbsolutePath());
      fileOutputStream.close();
    } catch (FileNotFoundException e) {
      Log.wtf(TAG, "Saving to disk - file not found", e);
    } catch (IOException e) {
      Log.wtf(TAG, "Saving to disk - IOException", e);
    }

    try {
      assetInputStream.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private File getStorageDir(String albumName) {
    File file = new File(Environment.getExternalStoragePublicDirectory(
        Environment.DIRECTORY_DOCUMENTS), albumName);
    file.mkdirs();
    return file;
  }
}
