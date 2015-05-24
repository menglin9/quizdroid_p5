package quiz_p5.menglin9.washington.edu.quiz_p5;

/**
 * Created by Menglin on 5/18/15.
 */
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by eric on 5/17/15.
 */
public class AlarmReceiver extends BroadcastReceiver{

    public AlarmReceiver() {}

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("AlarmReceiver", "entered onReceive() from AlarmReceiver");
        //Toast.makeText(AlarmReceiver.this,"HI!",Toast.LENGTH_SHORT).show();

        // This is where we start our DownloadService class! aka tell our IntentService to start the download!
        Intent downloadServiceIntent = new Intent(context, DownloadService.class);
        context.startService(downloadServiceIntent);
    }
}
