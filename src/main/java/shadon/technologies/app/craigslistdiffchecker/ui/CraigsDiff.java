package shadon.technologies.app.craigslistdiffchecker.ui;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import shadon.technologies.app.craigslistdiffchecker.service.CraigsDiffBackgroundService;
import shadon.technologies.app.craigslistdiffchecker.R;

public class CraigsDiff extends AppCompatActivity {

    //todo The flag to determine if the user wants the service to actually die is a sloppy global. Find out how to do this in a cleaner way.

    public static final String TAG = "CraigsDiff";
    public static boolean USER_STOPPED = false;

    Button btnStartService;
    Button btnStopService;
    Button btnManageSearches;
    TextView textViewServiceStatus;
    Intent backgroundService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnStartService = (Button) findViewById(R.id.btnStartService);
        btnStopService = (Button) findViewById(R.id.btnStopService);
        btnManageSearches = (Button) findViewById(R.id.btnManageSearches);

        textViewServiceStatus = (TextView) findViewById(R.id.textViewServiceStatus);

        backgroundService = new Intent(getBaseContext(), CraigsDiffBackgroundService.class);

        btnStartService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CraigsDiff.USER_STOPPED = false;
                startService(backgroundService);
                updateServiceStatusText();
            }
        });

        btnStopService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CraigsDiff.USER_STOPPED = true;
                stopService(backgroundService);
                updateServiceStatusText();
            }
        });

        btnManageSearches.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent nextScreen = new Intent(getApplicationContext(),ManageSearchesScreenActivity.class);
                startActivity(nextScreen);
            }
        });
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void updateServiceStatusText() {
        if (isMyServiceRunning(CraigsDiffBackgroundService.class)) {
            textViewServiceStatus.setText("RUNNING");
            textViewServiceStatus.setTextColor(Color.GREEN);
        } else {
            textViewServiceStatus.setText("NOT RUNNING");
            textViewServiceStatus.setTextColor(Color.RED);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateServiceStatusText();
    }

    // stopService is explicitly called here to trigger the service restart logic when the Android
    //  OS decides to kill the CraigsDiff app (this happens regularly for resource optimization)
    @Override
    protected void onDestroy() {
        stopService(backgroundService);
        Log.i(TAG, "onDestroy was called");
        super.onDestroy();

    }

    // This is done to stop the back button from temporarily killing the service when the user
    //  goes back to the home screen
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}
