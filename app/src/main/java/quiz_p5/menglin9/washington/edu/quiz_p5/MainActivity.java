package quiz_p5.menglin9.washington.edu.quiz_p5;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.CountDownLatch;


public class MainActivity extends ActionBarActivity {
    public String[] topics = {"Math", "Physics", "Unix"};
    public int[] icons;
    private ListView quizList;
    private static final int SETTINGS_RESULT = 1;

    private DownloadManager dm;
    private long enqueue;
    int time = 10;
    AlarmManager am;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        boolean internet = isNetworkAvailable();
        boolean airplanemode = isAirplaneModeOn(this);


        if (!internet) {
            Toast.makeText(getApplicationContext(), "You didn't connect to the internet!",
                    Toast.LENGTH_SHORT).show();

            if(airplanemode) {
               // Toast.makeText(getApplicationContext(), "aIRPLANE MODE!",
                 //       Toast.LENGTH_SHORT).show();
                AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
                builder1.setMessage("Do you want to turn the airplane mode off?");
                builder1.setCancelable(true);
                builder1.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent intent =  new Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS);
                                startActivity(intent);
                                dialog.cancel();
                            }
                        });
                builder1.setNegativeButton("No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alert11 = builder1.create();
                alert11.show();
            } else if (!isMobileAvailable(this)) {
                Toast.makeText(getApplicationContext(), "You didn't have mobile signal!",
                        Toast.LENGTH_SHORT).show();
            }

        }


        IntentFilter filter = new IntentFilter("quiz_p5.menglin9.washington.edu.quiz_p5");
        filter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE); // Add more filters here that you want the receiver to listen to
        //registerReceiver(alarmReceiver, new IntentFilter("quiz_p4.menglin9.washington.edu.quiz_p4"));
        registerReceiver(receiver, filter);


        // Register Receiver aka Listen if the DownloadManager from Android OS publishes a "Download has complete"-like broadcast
        //      -note that  DownloadManager is a system service that can be accessed anywhere.


        // get your Application singleton this time//////
        QuizApp quizApp = (QuizApp) getApplication();

        if (quizApp.getSuccess()) {
            topics = quizApp.getAllTopics();
            icons = quizApp.getAllIcons();
        }
        //myApp.questions.get("blah blah idk"); // grab your repository from MyApp and get data from it

        quizList = (ListView) findViewById(R.id.listView);
        // ArrayAdapter<String> items = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, topics);
        //ArrayAdapter<String> items = new ArrayAdapter<String>(this, R.layout.mylist, R.id.Itemname, topics);
        //quizList.setAdapter(items);

        //set the custom adapter for loading the individual image
        quizList.setAdapter(new CustomAdapter(this, topics, icons));

        quizList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Log.i("in", "ink");
                Intent next = new Intent(MainActivity.this, Quiz_Content.class);
                next.putExtra("position", position);
                startActivity(next);

            }
        });
    }

    // This is your receiver that you registered in the onCreate that will receive any messages that match a download-complete like broadcast
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            dm = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);

            Log.i("MyApp BroadcastReceiver", "onReceive of registered download reciever");

            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                Log.i("MyApp BroadcastReceiver", "download complete!");
                long downloadID = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);

                // if the downloadID exists
                if (downloadID != 0) {

                    // Check status
                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(downloadID);
                    Cursor c = dm.query(query);
                    if(c.moveToFirst()) {
                        int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
                        Log.d("DM Sample","Status Check: "+status);
                        switch(status) {
                            case DownloadManager.STATUS_PAUSED:
                            case DownloadManager.STATUS_PENDING:
                            case DownloadManager.STATUS_RUNNING:
                                break;
                            case DownloadManager.STATUS_SUCCESSFUL:
                                // The download-complete message said the download was "successfu" then run this code
                                ParcelFileDescriptor file;
                                StringBuffer strContent = new StringBuffer("");

                                try {
                                    // Get file from Download Manager (which is a system service as explained in the onCreate)
                                    file = dm.openDownloadedFile(downloadID);
                                    FileInputStream fis = new FileInputStream(file.getFileDescriptor());

                                    // YOUR CODE HERE [convert file to String here]
                                    BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
                                    StringBuilder out = new StringBuilder();
                                    String line;
                                    while ((line = reader.readLine()) != null) {
                                        out.append(line);
                                    }
                                    String data = "";
                                    data = out.toString();

                                    // YOUR CODE HERE [write string to data/data.json]
                                    //      [hint, i wrote a writeFile method in MyApp... figure out how to call that from inside this Activity]
                                    QuizApp quizApp = (QuizApp) getApplication();
                                    quizApp.setData(data);
                                    quizApp.writeToFile(data);
                                    // convert your json to a string and echo it out here to show that you did download it

                                    /*

                                    String jsonString = ....myjson...to string().... chipotle burritos.... blah
                                    Log.i("MyApp - Here is the json we download:", jsonString);

                                    */

                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                break;
                            case DownloadManager.STATUS_FAILED:
                                // YOUR CODE HERE! Your download has failed! Now what do you want it to do? Retry? Quit application? up to you!
                                DownloadService.startOrStopAlarm(context, false);

                                AlertDialog.Builder builder2 = new AlertDialog.Builder(MainActivity.this);
                                builder2.setMessage("Do you want to retry or quitApp?");
                                builder2.setCancelable(true);
                                builder2.setPositiveButton("Retry",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                DownloadService.startOrStopAlarm(MainActivity.this,true);
                                                dialog.cancel();
                                            }
                                        });
                                builder2.setNegativeButton("Quit",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                finish();
                                                dialog.cancel();
                                            }
                                        });

                                AlertDialog alert22 = builder2.create();
                                alert22.show();
                                break;
                        }
                    }
                }
            }
        }
    };

    // reads InputStream of JSON file and returns the file in JSON String format
    public String readJSONFile(InputStream inputStream) throws IOException {

        int size = inputStream.available();
        byte[] buffer = new byte[size];
        inputStream.read(buffer);
        inputStream.close();

        return new String(buffer, "UTF-8");
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
            Intent next = new Intent(MainActivity.this, UserSettingActivity.class);
            startActivity(next);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==SETTINGS_RESULT)
        {
            //displayUserSettings();
        }

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * Gets the state of Airplane Mode.
     *
     * @param context
     * @return true if enabled.
     */
    private static boolean isAirplaneModeOn(Context context) {

        return Settings.System.getInt(context.getContentResolver(),
                Settings.System.AIRPLANE_MODE_ON, 0) != 0;

    }

    public static Boolean isMobileAvailable(Context context) {
        TelephonyManager tel = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return ((tel.getNetworkOperator() != null && tel.getNetworkOperator().equals("")) ? false : true);
    }

}
