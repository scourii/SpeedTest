package com.example.speedtest;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.example.speedtest.databinding.ActivityMainBinding;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {
    private TextView ipAddress = (TextView) findViewById(R.id.ipAddress);
    private TextView downloadSpeed = (TextView) findViewById(R.id.downloadSpeed);
    private TextView uploadSpeed = (TextView) findViewById(R.id.uploadSpeed);
    private TextView ping = (TextView) findViewById(R.id.pingNumber);
    private TextView jitter = (TextView) findViewById(R.id.jitterNumber);
    private ActivityMainBinding binding;
    final DecimalFormat Format = new DecimalFormat("#.##");
    HashSet<String> BlackList;
    HostServer HostServerHandler;

    @Override
    public void onResume() {
        super.onResume();
        HostServerHandler = new HostServer();
        HostServerHandler.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        BlackList = new HashSet<>();

        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);


        ipAddress = (TextView) findViewById(R.id.ipAddress);
        final Button SpeedTestButton = (Button) findViewById(R.id.startButton);
        HostServerHandler = new HostServer();
        HostServerHandler.start();

        SpeedTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SpeedTestButton.setEnabled(false);
                if (HostServerHandler == null){
                    HostServerHandler = new HostServer();
                    HostServerHandler.start();
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setContentView(R.layout.startSpeedTest);
                            }
                        });
                        int minute = 600;
                        while (!HostServerHandler.returnStatus())
                            minute--;
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            if (minute <= 0){
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), "ERROR", Toast.LENGTH_LONG).show();
                                        SpeedTestButton.setEnabled(true);
                                        SpeedTestButton.setText(R.string.restartButtonText);
                                    }
                                });
                                HostServerHandler = null;
                                return;
                            }
                    }
                    HashMap<Integer, String> key = HostServerHandler.getKey();
                    HashMap<Integer, List<String>> value = HostServerHandler.getValue();
                    double Latitude = HostServerHandler.getLatitude();
                    double Longitude = HostServerHandler.getLongitude();
                    double dist = 0.0;
                    int ServerIndex = 0;

                });
                setContentView(R.layout.speedtest_results);
            }
        });

    }

}
