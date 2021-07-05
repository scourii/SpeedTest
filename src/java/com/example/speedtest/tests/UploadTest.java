package com.example.speedtest.tests;

import java.io.DataOutputStream;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;

public class UploadTest extends Thread {
    public String UploadUrl;
    double StartTime;
    double EndTime;
    double TotalTime;
    int KBytesUploaded;
    double UploadRate;
    double InitialUploadRate;
    boolean status = false;
    HttpsURLConnection Connection;
    int TimeOut = 5;

    public UploadTest(String UploadUrl)
    {
        this.UploadUrl = UploadUrl;
    }
    
    public boolean CurrentStatus()
    {
        return status;
    }

    public void SetInitialUploadRate(int KBytesUploaded, Double TotalTime)
    {
        if (KBytesUploaded >= 0)
        {
            this.InitialUploadRate = (Long)Math.round(((KBytesUploaded * 8) / (1000 * 1000)) / TotalTime);
        }
        else
        {
            this.InitialUploadRate = 0.0;
        }
    }
    public double getFinalDownloadRate()
    {
        return Math.round(UploadRate * 100) / 100;
    }
    @Override
    public void run()
    {
        try
        {
            URL url = new URL(UploadUrl);
            KBytesUploaded = 0;
            StartTime = System.currentTimeMillis();

            ExecutorService executorService = Executors.newFixedThreadPool(8);
            for (int i = 0; i < 8; i++)
            {
                executorService.execute(new doUploadTests(url));
            }
            executorService.shutdown();
            while (!executorService.isTerminated())
                try
                {
                    Thread.sleep(1000);
                }
                catch (InterruptedException interruptedException)
                {
                    interruptedException.printStackTrace();
                }
            long TimeNow = System.currentTimeMillis();
            TotalTime = (TimeNow - StartTime) / 1000;
            UploadRate = ((KBytesUploaded / 1000) * 8) / TotalTime;
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }
}
class doUploadTests extends Thread
{
    int KBytesUploaded;
    URL url;
    public doUploadTests(URL url)
    {
        this.url = url;
    }
    public void run()
    {
        byte[] buffer = new byte[150 * 1024];
        long StartTime = System.currentTimeMillis();
        int timeout = 10;
        while (true)
        {
            try {
                HttpsURLConnection connection = null;
                connection = (HttpsURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.setSSLSocketFactory((SSLSocketFactory)SSLSocketFactory.getDefault());
                connection.setHostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                });
                connection.connect();

                DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());
                dataOutputStream.write(buffer, 0, buffer.length);
                dataOutputStream.flush();

                connection.getResponseCode();

                KBytesUploaded += buffer.length / 1024;
                long EndTime = System.currentTimeMillis();
                double TotalTime = (EndTime - StartTime) / 1000;
                if (TotalTime >= timeout)
                {
                    break;
                }
                dataOutputStream.close();
                connection.disconnect();
            }
            catch (Exception exception)
            {
                exception.printStackTrace();
                break;
            }
        }
    }
}