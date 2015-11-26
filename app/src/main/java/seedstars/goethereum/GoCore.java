package seedstars.goethereum;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * GoCore is the service that runs the Go Ethereum Process
 * <p/>
 * In this first release the user doesn't have options to run the Go Ethereum console
 * In next releases the idea is create options when the Service is call to make possible
 * to call diff  methods and options when process starts.
 */
public class GoCore extends Service {

    Process goCore;                                         // Process that runs Go Ethereum binary
    Thread outputThread;                                    // Thread to read the output from Go Ethereum
    Thread errOutputThread;                                 // Thread to read the err output from Go Ethereum
    OutputStream goCoreOutput;                              // OutPutStream to the Go Ethereum
    private GoCoreCallback goCoreCallback;                  // Interface to communicate with the Main Activity
    private final IBinder binder = new LocalBinder();
    private String defauldCommand = null;

    // Class used for the client Binder.
    public class LocalBinder extends Binder {
        GoCore getService() {
            // Return this instance of MyService so clients can call public methods
            return GoCore.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    // Set the Activity methods
    public void setCallbacks(GoCoreCallback callbacks) {
        goCoreCallback = callbacks;
    }


    public GoCore() {
    }

    /**
     * This method receives a String from the Main Activity and send to the Go Ethereum Process
     *
     * @param cmd
     */
    public void setCommandToCore(String cmd) {
        try {
            goCoreOutput.write((cmd + '\n').getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onCreate() {
        super.onCreate();
    }

    @Override
    /**
     * Start the Service
     *
     * We need to set the path of the datadir folder and the ipcpath file because Go Ethereum tries to create
     * these folders in the root and don't have premitions.
     */
    public void onStart(Intent intent, int startId) {
        Bundle extras = intent.getExtras();
        try {

            String startGethCmd;
            // geth command "geth"
            String gethCommand = getFilesDir() + "/geth ";
            // datadir folder path
            String dataDir = "--datadir=" + getFilesDir() + "/datadir/ ";
            // ipcpath file path
            String ipcPath = "--ipcpath=" + getFilesDir() + "/datadir/geth.ipc ";
            // console option
            String console = "console";
            if (extras != null) {

                startGethCmd = gethCommand + dataDir + ipcPath + extras.get("defaultCommand") + " " + console;
            } else {

                startGethCmd = gethCommand + dataDir + ipcPath + console;
            }


            goCore = Runtime.getRuntime().exec(startGethCmd);

            goCoreOutput = goCore.getOutputStream();

            // Thread to listen output from goCore process
            outputThread = new Thread() {
                InputStream is = goCore.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);

                public void run() {

                    try {
                        String line;
                        Boolean lastLine = false;
                        while (true) {
                            if ((line = br.readLine()) != null) {
                                // Send output to Main Activity
                                if (goCoreCallback != null) {
                                    goCoreCallback.outputToView(line);

                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            // Thread to listen err output from goCore process
            errOutputThread = new Thread() {
                InputStream error = goCore.getErrorStream();
                InputStreamReader errorIsr = new InputStreamReader(error);
                BufferedReader brError = new BufferedReader(errorIsr);

                public void run() {
                    try {
                        String lineError;
                        while (true) {
                            if ((lineError = brError.readLine()) != null) {
                                if (goCoreCallback != null) {
                                    // Send err output to de Main Activity
                                    goCoreCallback.outputErrorToView(lineError);
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            // Start the Threads
            outputThread.start();
            errOutputThread.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
        // Show service start message to user
        Toast.makeText(this, "GoCore start running", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDestroy() {
        // Show service stop message to user
        Toast.makeText(this, "GoCore stop running", Toast.LENGTH_LONG).show();
        if (goCore != null) {
            goCore.destroy();
        }
        super.onDestroy();
    }

    public void setDefaultCommand(String command) {
        this.defauldCommand = command;
    }

}
