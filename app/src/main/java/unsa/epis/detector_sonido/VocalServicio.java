package unsa.epis.detector_sonido;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

public class VocalServicio extends Service implements DetectarSonidoListener {
    private DetectorThread detectorThread;
    private RecorderThread recorderThread;
    private static final int NOTIFICATION_Id = 001;
    public static final int DETECT_NONE = 0;
    public static final int DETECT_WHISTLE = 1;
    public static int selectedDetection = DETECT_NONE;

    private ServiceCallbacks serviceCallbacks;
    private final IBinder binder = new LocalBinder();
    // Class used for the client Binder.
    public class LocalBinder extends Binder {
        VocalServicio getService() {
            // Return this instance of MyService so clients can call public methods
            return VocalServicio.this;
        }
    }
    public void setCallbacks(ServiceCallbacks callbacks) {
        serviceCallbacks = callbacks;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub

        return START_STICKY;
    }
    public void initNotification(){
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.icono_microfono)
                        .setContentTitle("Detección de silbidos")
                        .setContentText("La detección de silbidos está activada.");
        Intent resultIntent = new Intent(this, MainActivity.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity( this,0,
                resultIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.notify(NOTIFICATION_Id, mBuilder.build());
    }
    public void startDetection(){
        selectedDetection = DETECT_WHISTLE;
        recorderThread = new RecorderThread();
        recorderThread.start();
        detectorThread = new DetectorThread(recorderThread);
        detectorThread.setDetectarSonidoListener(this);
        detectorThread.start();
        Toast.makeText(this, "Servicio iniciado", Toast.LENGTH_LONG).show();
    }
    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if (recorderThread != null) {
            recorderThread.stopRecording();
            recorderThread = null;
        }
        if (detectorThread != null) {
            detectorThread.stopDetection();
            detectorThread = null;
        }
        selectedDetection = DETECT_NONE;
        Toast.makeText(this, "Servicio detenido", Toast.LENGTH_LONG).show();
        stopNotification();
    }
    @Override
    public IBinder onBind(Intent intent) {
        initNotification();
        startDetection();
        return binder;
    }
    @Override
    public void onWhistleDetected() {

        if (serviceCallbacks != null) {
            Log.d("silvido","dentro del servicio ");
            serviceCallbacks.doSomething();
        }
//        Intent intent = new Intent(this,VocalAlerta.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(intent);
//        Toast.makeText(this, "Silbido detectado", Toast.LENGTH_LONG).show();

    }
    public void stopNotification(){
        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.cancel(NOTIFICATION_Id);
    }
}