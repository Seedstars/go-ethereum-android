package seedstars.goethereum;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;


import seedstars.goethereum.components.CommandLine;

public class MainActivity extends AppCompatActivity implements GoCoreCallback {

    Intent intent;
    ViewFlipper viewFlipper;
    TextView textView;                      // Text View for Go Ethereum output
    TextView outputErrorView;               // Text View for Go Ethereum Err/Debug output
    ScrollView scroll;                      // Scroll View for textView
    ScrollView scrollErrOut;                // Scroll View for outputErrorView
    InputMethodManager mgr;                 // InputMethodManager for Keyboard
    private boolean bound = false;

    private GoCore goCore;                  // Go Ethereum Service
    CommandLine currentCommand;             // Current Command in use
    LinkedList<CommandLine> commandList;    // List of all commands
    boolean doScroll;                       // Should scroll view
    boolean goIsRunning;                    // True if Go Ethereum is running or false otherwise
    float lastX;                            // Last touch position


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        viewFlipper = (ViewFlipper) findViewById(R.id.view_flipper);

        // Set Touch Listener to the view Flipper
        viewFlipper.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                onTouchEvent(event);
                return false;
            }
        });

        // Keyboard Input
        mgr = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);

        currentCommand = new CommandLine();
        commandList = new LinkedList<CommandLine>();
        doScroll = true; // Should scroll. False is not implemented

        textView = (TextView) this.findViewById(R.id.textView);
        outputErrorView = (TextView) this.findViewById(R.id.errorOutput);

        scroll = (ScrollView) this.findViewById(R.id.command_line_scroll_view);
        scrollErrOut = (ScrollView) this.findViewById(R.id.scroll_err_out);

        // Add the Go Binary and the genesis block to de source folder
        try {

            InputStream insGeth = getResources().openRawResource(R.raw.geth);
            byte[] bufferGeth = new byte[insGeth.available()];
            insGeth.read(bufferGeth);
            insGeth.close();
            FileOutputStream fosGeth = openFileOutput("geth", MODE_PRIVATE);
            fosGeth.write(bufferGeth);
            fosGeth.close();

            File file = getFileStreamPath("geth");
            file.setExecutable(true);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Start Go Ethereum Service.
     */
    public void startService() {

        intent = new Intent(this, GoCore.class);
        startService(intent);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        goIsRunning = true;
        //Show Keyboard

        mgr.toggleSoftInput(1, 1);
    }

    /**
     * Stop Go Ethereum Service.
     */
    public void stopGethService() {
        unbindService(serviceConnection);
        stopService(intent);
        goIsRunning = false;

        // Hide Keyboard
        mgr.toggleSoftInput(0, 0);
    }

    /**
     * Callbacks for service binding, passed to bindService().
     */
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // cast the IBinder and get MyService instance
            GoCore.LocalBinder binder = (GoCore.LocalBinder) service;
            goCore = binder.getService();
            bound = true;
            goCore.setCallbacks(MainActivity.this); // register
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem menuItem = menu.findItem(R.id.myswitch);
        View view = MenuItemCompat.getActionView(menuItem);
        Switch switch_menu = (Switch) view.findViewById(R.id.switch_goCore);
        switch_menu.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    startService();
                } else {
                    stopGethService();
                }
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.keyboard_button) {
            // Show/Hide Keyboard
            mgr.toggleSoftInput(1, 0);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    /**
     * This method implements the input like a command line.
     */
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // Only enter if the service are running
        if (goIsRunning) {
            switch (keyCode) {
                /**
		 * When the user inserts a geth command, it is added to command history.
                 */
                case KeyEvent.KEYCODE_ENTER:
                    if (currentCommand.getCommandLine().length() != 0) {
                        if (currentCommand.getCommandLine().equals("exit")) {
                            // Message is shown to the user
                            Toast.makeText(this, "Please turn off on witch the  instead of command exit", Toast.LENGTH_LONG).show();
                            currentCommand.setCommandLine("");
                            textView.append("\n>");
                            break;
                        }
                        // Command is sent to the core
                        goCore.setCommandToCore(currentCommand.getCommandLine());

                        // Case command Object was already inserted before
                        if (commandList.indexOf(currentCommand) != -1) {
                            CommandLine newCommand = new CommandLine();
                            newCommand.setCommandLine(currentCommand.getCommandLine());
                            commandList.add(newCommand);
                        } else {
                            commandList.add(currentCommand);
                        }
                        currentCommand = new CommandLine();
                        textView.append("\n");
                    }
                    break;
                /**
                 * Case Del/BackSpace is pressed.
                 *
                 * If currentCommand length is greater than 1 the last char from the currentCommand and the textView is deleted.
                 */
                case KeyEvent.KEYCODE_DEL:
                    if (currentCommand.getCommandLength() > 0) {
                        currentCommand.delChar();
                        textView.setText(textView.getText().subSequence(0, textView.getText().length() - 1));
                    }
                    break;
                /**
                 * Case Volume Up key is pressed.
                 *
                 * You will have access to shortcuts. Currently, we only have a shortcut, that is activated when you write 'W'.
                 */

                case KeyEvent.KEYCODE_VOLUME_UP:
                    // If currentComand already in shortcut mode
                    if (currentCommand.inShortcut()) {
                        currentCommand.setShortcut(false);
                    } else {
                        currentCommand.setShortcut(true);
                    }
                    break;
                /**
                 * Case Key 'W' is pressed.
                 *
                 * If the shortcut is off when 'W' is pressed, the default case is used on 'W'. 
		 * Otherwise the default case isn't used and the shortcut is triggered.
                 *
                 * The shortcut for key 'W' accesses the last command in history.
                 * (Works kike the 'Key Up' in linux command line)
                 */
                case KeyEvent.KEYCODE_W:
                    if (currentCommand.inShortcut()) {
                        currentCommand.setShortcut(false);
                        int index = commandList.indexOf(currentCommand);
                        if (index == -1) {
                            index = commandList.size();
                        }
                        if (index > 0) {
                            String lastText = (String) textView.getText().toString();
                            Boolean blankLine = currentCommand.getCommandLength() == 0;
                            int sizePastCommand = currentCommand.getCommandLength();
                            currentCommand = (commandList.get(index - 1));

                            if (blankLine) {
                                textView.append(currentCommand.getCommandLine());
                            } else {
                                textView.setText(lastText.subSequence(0, lastText.length() - sizePastCommand) + currentCommand.getCommandLine());
                            }
                        }
                        break;
                    }
                    /**
                     * Other Keys
                     *
                     * By default the character is appended to the currentCommand object and the textView is updated.
                     */
                default:
                    if (event.getUnicodeChar() != 0) {
                        currentCommand.addChar((char) event.getUnicodeChar());
                        String lastText = (String) textView.getText().toString();
                        if (currentCommand.getCommandLength() == 1) {
                            textView.append(currentCommand.getCommandLine());
                        } else if (lastText.subSequence(textView.length() - currentCommand.getCommandLength() + 1, lastText.length()).equals(currentCommand.getCommandLine().subSequence(0, currentCommand.getCommandLength() - 1))) {
                            textView.append("" + (char) event.getUnicodeChar());
                        } else {
                            textView.append(">" + currentCommand.getCommandLine());
                        }
                    }
                    break;
            }
            // call the ScrollTextView method
            if (doScroll) {
                this.scrollTextView();
            }
        }
        return true;
    }

    /**
     * This method makes the textView to scroll down until the end of the text
     */
    public void scrollTextView() {
        scroll.post(new Runnable() {
            @Override
            public void run() {
                scroll.fullScroll(View.FOCUS_DOWN);
                scrollErrOut.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    /**
     * This method put the String sent by the Go Ethereum Service in to the textView.
     *
     * @param response
     */
    public void outputToView(final String response) {
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.append(response + "\n");
                MainActivity.this.scrollTextView();
            }
        });

    }

    /**
     * This method put the String sent by the Go Ethereum Service in to the outputErrorView.
     *
     * @param response
     */
    public void outputErrorToView(final String response) {
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                outputErrorView.append(response + "\n");
                MainActivity.this.scrollTextView();
            }
        });
    }

    // Method to handle touch event like left to right swap and right to left swap
    public boolean onTouchEvent(MotionEvent touchevent) {

        switch (touchevent.getAction()) {
            // when user first touches the screen to swap
            case MotionEvent.ACTION_DOWN: {
                lastX = touchevent.getX();
                break;
            }
            case MotionEvent.ACTION_UP: {
                float currentX = touchevent.getX();

                // if left to right swipe on screen
                if (lastX < currentX) {
                    // If no more View/Child to flip
                    if (viewFlipper.getDisplayedChild() == 0)
                        break;

                    // set the required Animation type to ViewFlipper
                    // The Next screen will come in form Left and current Screen will go OUT from Right
                    viewFlipper.setInAnimation(this, R.anim.in_from_left);
                    viewFlipper.setOutAnimation(this, R.anim.out_to_right);
                    // Show the next Screen
                    viewFlipper.showNext();
                }

                // if right to left swipe on screen
                if (lastX > currentX) {
                    if (viewFlipper.getDisplayedChild() == 1)
                        break;
                    // set the required Animation type to ViewFlipper
                    // The Next screen will come in form Right and current Screen will go OUT from Left
                    viewFlipper.setInAnimation(this, R.anim.in_from_right);
                    viewFlipper.setOutAnimation(this, R.anim.out_to_left);
                    // Show The Previous Screen
                    viewFlipper.showPrevious();
                }
                break;
            }
        }
        return false;
    }

}


