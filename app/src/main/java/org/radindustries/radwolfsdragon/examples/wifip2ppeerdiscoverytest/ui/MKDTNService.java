package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.ui;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.BuildConfig;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.DConstants;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.R;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.BWDTN;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.aa.app.DTNClient;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.aa.app.DTNUI;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.PrimaryBlock;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.manager.DTNManager;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.router.Daemon2Router;

import java.util.ArrayList;
import java.util.HashMap;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class MKDTNService extends Service implements DTNUI {
    
    private static DTNClient dtnClient;
    private static String[] peers;
    private static HashMap<String, String> receivedDTNMessages;
    static NotificationCompat.InboxStyle myNotificationStyle;
    
    private DTNManager dtnManager;
    private boolean running = false;
    
    public static final String LOG_TAG
        = DConstants.MAIN_LOG_TAG + "_" + MKDTNService.class.getSimpleName();
    
    public MKDTNService() {
        peers = new String[0];
        receivedDTNMessages = new HashMap<>();
        myNotificationStyle = new NotificationCompat.InboxStyle();
        theirMessengers = new ArrayList<>();
    }
    
    @Override
    public void onReceiveDTNMessage(byte[] message, String sender) {
        String text = new String(message);
        receivedDTNMessages.put(sender, text);
        notifyUser(sender, text);
    
        for (Messenger messenger : theirMessengers) {
            Message msg = Message.obtain(null, MSG_GET_RECEIVED_DTN_MESSAGES);
            Bundle data = new Bundle();
            data.putSerializable(MESSAGES_KEY, receivedDTNMessages);
            msg.setData(data);
            if (messenger != null) {
                try {
                    messenger.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    theirMessengers.remove(messenger);
                }
            }
        }
    }
    
    @Override
    public void onOutboundBundleReceived(String recipient) {
        notifyUser(
            getString(R.string.delivery_report_title),
            String.format(getString(R.string.delivery_report_message), recipient)
        );
    }
    
    public static final String MKDTN_NOTIFICATION_TAG = BuildConfig.APPLICATION_ID;
    static final int MKDTN_NOTIFICATION_ID = 69;
    private static final int MKDTN_SERVICE_NOTIFICATION_ID = 666;
    private static final int NOTIFICATION_VISIBILITY = NotificationCompat.VISIBILITY_PRIVATE;
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                MKDTN_NOTIFICATION_TAG,
                getString(R.string.mkdtn_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription(getString(R.string.mkdtn_channel_description));
            channel.enableVibration(true);
            channel.setLockscreenVisibility(NOTIFICATION_VISIBILITY);
            
            NotificationManager notificationManager
                = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null &&
                notificationManager.getNotificationChannel(MKDTN_NOTIFICATION_TAG) == null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
    
    private NotificationCompat.Builder makeNotification(String title, String text) {
        return new NotificationCompat.Builder(this, MKDTN_NOTIFICATION_TAG)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.public_notification_text))
            .setWhen(System.currentTimeMillis())
            .setShowWhen(true)
            .setGroup(getString(R.string.notification_group_key))
            .setGroupSummary(true)
            .setAutoCancel(true)
            .setStyle(myNotificationStyle.addLine(title + " => " + text))
            .setPublicVersion(
                new NotificationCompat.Builder(this, MKDTN_NOTIFICATION_TAG)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.public_notification_text))
                .build()
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NOTIFICATION_VISIBILITY);
    }
    
    private void notifyUser(String title, String text) {
        NotificationManagerCompat notificationManagerCompat
            = NotificationManagerCompat.from(this);
        
        // check if notifications are enabled
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (!prefs.getBoolean(getString(R.string.pref_enable_dtn_notifications_key),
            getResources().getBoolean(R.bool.pref_enable_dtn_notification_default)) ||
            !notificationManagerCompat.areNotificationsEnabled()) return;
        
        // create and register channel with the system
        createNotificationChannel();
        
        // make notification builder
        NotificationCompat.Builder notificationBuilder
            = makeNotification(title, text);
        
        // create intent for opening main activity
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT
        );
        notificationBuilder.setContentIntent(pendingIntent);
        
        // for those that don't support grouping,
        notificationManagerCompat.notify(
            MKDTN_NOTIFICATION_TAG, MKDTN_NOTIFICATION_ID, notificationBuilder.build()
        );
    }
    
    @Override
    public void onPeerListChanged(String[] peerList) {
        peers = peerList;
    
        for (Messenger messenger : theirMessengers) {
            Message msg = Message.obtain(null, MSG_GET_PEER_LIST);
            Bundle data = new Bundle();
            data.putStringArray(PEER_LIST_KEY, peers);
            msg.setData(data);
            if (messenger != null) {
                try {
                    messenger.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    theirMessengers.remove(messenger);
                }
            }
        }
    }
    
    private static ArrayList<Messenger> theirMessengers;
    private final Messenger ourMessenger = new Messenger(new UIMessageHandler());
    
    public static final int MSG_GET_PEER_LIST = 1;
    public static final int MSG_GET_DTN_CLIENT_ID = 2;
    public static final int MSG_GET_RECEIVED_DTN_MESSAGES = 3;
    
    public static final int MSG_SEND = 7;
    public static final String RECIPIENT_KEY = "recipient";
    public static final String TEXT_KEY = "message";
    public static final String PROTOCOL_KEY = "RP";
    public static final String PRIORITY_KEY = "PC";
    public static final String LIFETIME_KEY = "LT";
    
    public static final int MSG_REGISTER_CLIENT = 5;
    public static final int MSG_REGISTRATION_NUMBER = 8;
    public static final int MSG_UNREGISTER_CLIENT = 6;
    
    public static final String PEER_LIST_KEY = "peerList";
    public static final String MESSAGES_KEY = "messages";
    public static final String DTN_CLIENT_ID_KEY = "dtnClientID";
    
    @Override
    public void onCreate() {
        super.onCreate();
        BWDTN.init(this);
        
        dtnManager = BWDTN.getDTNManager();
        dtnClient = BWDTN.getDTNClient(this);
    
//        Log.i(LOG_TAG, "Service created");
        showPersistentNotification();
    }
    
    private void showPersistentNotification() {
        NotificationManagerCompat notificationManagerCompat
            = NotificationManagerCompat.from(this);
    
        // check if notifications are enabled
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (!prefs.getBoolean(getString(R.string.pref_enable_dtn_notifications_key),
            getResources().getBoolean(R.bool.pref_enable_dtn_notification_default)) ||
            !notificationManagerCompat.areNotificationsEnabled()) return;
    
        // create and register channel with the system
        createNotificationChannel();
    
        // make notification builder
        NotificationCompat.Builder notificationBuilder
            = new NotificationCompat.Builder(this, MKDTN_NOTIFICATION_TAG)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.dtn_service_started))
            .setTicker(getString(R.string.dtn_service_started))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT);
    
        // create intent for opening main activity
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT
        );
        notificationBuilder.setContentIntent(pendingIntent);
        
        startForeground(MKDTN_SERVICE_NOTIFICATION_ID, notificationBuilder.build());
    }
    
    static class UIMessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            try {
                switch (msg.what) {
                    case MSG_REGISTER_CLIENT: registerClient(msg); break;
                    case MSG_UNREGISTER_CLIENT: theirMessengers.remove(msg.arg1); break;
                    case MSG_SEND: sendMessage(msg.getData()); break;
                    case MSG_GET_PEER_LIST: sendPeerList(msg); break;
                    case MSG_GET_DTN_CLIENT_ID: sendClientID(msg); break;
                    case MSG_GET_RECEIVED_DTN_MESSAGES: sendMessages(msg); break;
                    default: break;
                }
            } catch (RemoteException e) {
                e.printStackTrace();
                theirMessengers.remove(msg.arg1);
            }
        }
    
        private void sendMessages(Message msg) throws RemoteException {
            Bundle data = new Bundle();
            Message messages = Message.obtain(msg.getTarget(), MSG_GET_RECEIVED_DTN_MESSAGES);
            data.putSerializable(MESSAGES_KEY, receivedDTNMessages);
            messages.setData(data);
            if (theirMessengers.get(msg.arg1) != null)
                theirMessengers.get(msg.arg1).send(messages);
        }
        
        private void sendClientID(Message msg) throws RemoteException {
            Bundle data = new Bundle();
            Message id = Message.obtain(msg.getTarget(), MSG_GET_DTN_CLIENT_ID);
            data.putString(DTN_CLIENT_ID_KEY, dtnClient.getID());
            id.setData(data);
            if (theirMessengers.get(msg.arg1) != null)
                theirMessengers.get(msg.arg1).send(id);
        }
    
        private void registerClient(Message msg) throws RemoteException {
            theirMessengers.add(msg.replyTo);
    
            int regNum = theirMessengers.indexOf(msg.replyTo);
    
            Message registrationInfo = Message.obtain(
                msg.getTarget(), MSG_REGISTRATION_NUMBER, regNum, 0
            );
            if (theirMessengers.get(regNum) != null)
                theirMessengers.get(regNum).send(registrationInfo);
        }
        
        private void sendPeerList(Message msg) throws RemoteException {
            Bundle data = new Bundle();
            peers = dtnClient.getPeerList();
            Message peerList = Message.obtain(msg.getTarget(), MSG_GET_PEER_LIST);
            data.putStringArray(PEER_LIST_KEY, peers);
            peerList.setData(data);
            if (theirMessengers.get(msg.arg1) != null)
                theirMessengers.get(msg.arg1).send(peerList);
        }
        
        private void sendMessage(Bundle messageBundle) {
            String recipient = messageBundle.getString(RECIPIENT_KEY, "dtn:null");
            
            String text = messageBundle.getString(TEXT_KEY, "hi");
            
            Daemon2Router.RoutingProtocol protocol
                = Daemon2Router.RoutingProtocol.valueOf(
                messageBundle.getString(PROTOCOL_KEY, "PER_HOP")
            );
            
            PrimaryBlock.PriorityClass priority
                = PrimaryBlock.PriorityClass.valueOf(
                messageBundle.getString(PRIORITY_KEY, "NORMAL")
            );
            
            PrimaryBlock.LifeTime lifeTime
                = PrimaryBlock.LifeTime.valueOf(
                messageBundle.getString(LIFETIME_KEY, "THREE_DAYS")
            );
    
            dtnClient.send(
                text.getBytes(),
                recipient,
                priority,
                lifeTime,
                protocol
            );
        }
    }
    
    @Override
    public IBinder onBind(Intent intent) {
//        Log.i(LOG_TAG, "binding...");
        return ourMessenger.getBinder();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!running) {
            running = dtnManager.start();
//            if (running) {
//                Log.i(LOG_TAG, "Service started");
//            }
        }
        return START_STICKY;
    }
    
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.i(LOG_TAG, "Stopping service due to low memory");
        stopSelf();
    }
    
    @Override
    public boolean onUnbind(Intent intent) {
//        Log.i(LOG_TAG, "unbinding...");
        return true;
    }
    
    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
//        Log.i(LOG_TAG, "rebinding...");
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (running) {
            running = !dtnManager.stop();
//            if (!running) Log.i(LOG_TAG, "Service stopped");
        }
    }
}
