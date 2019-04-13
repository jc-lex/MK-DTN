package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.ui;

import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.BuildConfig;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.DConstants;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.R;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.BWDTN;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.aa.app.DTNClient;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.aa.app.DTNTextMessenger;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.aa.app.DTNUI;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.PrimaryBlock;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.fragmentmanager.Daemon2FragmentManager;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.manager.DTNManager;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.router.Daemon2Router;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class MKDTNService extends Service implements DTNUI {
    
    private DTNManager dtnManager;
    private static DTNClient dtnClient;
    private static DTNTextMessenger dtnTextMessenger;
    private static String[] peers;
    
    private static final String LOG_TAG
        = DConstants.MAIN_LOG_TAG + "_" + MKDTNService.class.getSimpleName();
    
    @Override
    public void onReceiveDTNMessage(byte[] message, String sender) {
        String text = new String(message);
        text = String.format(getString(R.string.new_message_text), sender, text);
        notifyUser(Html.fromHtml(text));
    
        broadcastMessages();
    }
    
    @Override
    public void onBundleStatusReceived(String recipient, String msg) {
        String text = String.format(
            getString(R.string.bundle_status_notification_message), recipient, msg
        );
        notifyUser(Html.fromHtml(text));
    
        broadcastMessages();
    }
    
    private void broadcastMessages() {
        for (Messenger messenger : theirMessengers) {
            Message msg = Message.obtain(null, MSG_GET_RECEIVED_DTN_MESSAGES);
            Bundle data = new Bundle();
            data.putSerializable(MESSAGES_KEY,
                (ArrayList) dtnTextMessenger.getDeliveredTextMessages());
            msg.setData(data);
            if (messenger != null) {
                try {
                    messenger.send(msg);
                } catch (RemoteException e) {
//                    e.printStackTrace();
                    theirMessengers.remove(messenger);
                }
            }
        }
    }
    
    private static final String MK_DTN_NOTIFICATION_TAG = BuildConfig.APPLICATION_ID;
    private static final int NOTIFICATION_VISIBILITY = NotificationCompat.VISIBILITY_PRIVATE;
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                MK_DTN_NOTIFICATION_TAG,
                getString(R.string.mkdtn_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription(getString(R.string.mkdtn_channel_description));
            channel.enableVibration(true);
            channel.setLockscreenVisibility(NOTIFICATION_VISIBILITY);
            
            NotificationManager notificationManager
                = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null &&
                notificationManager.getNotificationChannel(MK_DTN_NOTIFICATION_TAG) == null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
    
    private NotificationCompat.Builder makeNotification(Spanned text) {
        return new NotificationCompat.Builder(this, MK_DTN_NOTIFICATION_TAG)
            .setSmallIcon(R.drawable.ic_mkdtn_notification)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(text)
            .setGroup(getString(R.string.notification_group_key))
            .setGroupSummary(true)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NOTIFICATION_VISIBILITY);
    }
    
    private void notifyUser(Spanned text) {
        NotificationManagerCompat notificationManagerCompat
            = NotificationManagerCompat.from(this);
        
        // check if notifications are enabled
        if (!notificationManagerCompat.areNotificationsEnabled()) return;
        
        // create and register channel with the system
        createNotificationChannel();
        
        // make notification builder
        NotificationCompat.Builder notificationBuilder = makeNotification(text);
        
        // create intent for opening messages activity
//        Intent intent = new Intent(this, MessagesActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        PendingIntent pendingIntent = PendingIntent.getActivity(
//            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT
//        );
//        notificationBuilder.setContentIntent(pendingIntent);
        
        notificationManagerCompat.notify(
            (int) (Math.random() * Integer.MAX_VALUE),
            notificationBuilder.build()
        );
    }
    
    @Override
    public void onPeerListChanged(String[] peerList) {
        peers = peerList;
    
        broadcastPeers();
    }
    
    private void broadcastPeers() {
        for (Messenger messenger : theirMessengers) {
            Message msg = Message.obtain(null, MSG_GET_PEER_LIST);
        
            Bundle data = new Bundle();
            data.putStringArray(PEER_LIST_KEY, peers);
            msg.setData(data);
        
            if (messenger != null) {
                try {
                    messenger.send(msg);
                } catch (RemoteException e) {
//                    e.printStackTrace();
                    theirMessengers.remove(messenger);
                }
            }
        }
    }
    
    private static ArrayList<Messenger> theirMessengers;
    
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
    
    private final Messenger ourMessenger = new Messenger(new UIMessageHandler(this));
    private static class UIMessageHandler extends Handler {
        private Context context;
    
        private UIMessageHandler(Context context) {
            this.context = context;
        }
    
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            try {
                switch (msg.what) {
                    case MSG_REGISTER_CLIENT: registerClient(msg); break;
                    case MSG_UNREGISTER_CLIENT: unregisterClient(msg.arg1); break;
                    case MSG_SEND: sendOutboundMessage(msg.getData()); break;
                    case MSG_GET_PEER_LIST: sendPeerList(msg); break;
                    case MSG_GET_DTN_CLIENT_ID: sendClientID(msg); break;
                    case MSG_GET_RECEIVED_DTN_MESSAGES: getDeliveredMessages(msg); break;
                    default: break;
                }
            } catch (RemoteException e) {
                e.printStackTrace();
                unregisterClient(msg.arg1);
            }
        }
        
        private void unregisterClient(int regNum) {
            theirMessengers.remove(regNum);
        }
        
        private void getDeliveredMessages(Message msg) throws RemoteException {
            Bundle data = new Bundle();
            Message messages = Message.obtain(msg.getTarget(), MSG_GET_RECEIVED_DTN_MESSAGES);
            data.putSerializable(MESSAGES_KEY,
                (ArrayList) dtnTextMessenger.getDeliveredTextMessages());
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
            if (theirMessengers.contains(msg.replyTo)) return;
            
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
        private static final int MAX_TXT_SIZE = 34;
        private void sendOutboundMessage(Bundle messageBundle) {
            String recipient = messageBundle.getString(RECIPIENT_KEY,
                context.getString(R.string.mkdtn_null_endpoint_id));
            if (recipient.equals(dtnClient.getID())) return;
            
            String text = messageBundle.getString(TEXT_KEY,
                context.getString(R.string.mkdtn_hello_message));
            if (text.equals(context.getString(R.string.long_text_indicator))) {
                StringBuilder textBuilder = new StringBuilder(text);
                for (int i = 0; i < MAX_TXT_SIZE; i++) {
                    textBuilder.append(context.getString(R.string.mkdtn_long_hello_message));
                }
                text = textBuilder.toString();
            }
            
            Daemon2Router.RoutingProtocol protocol
                = Daemon2Router.RoutingProtocol.valueOf(
                messageBundle.getString(PROTOCOL_KEY,
                    context.getString(R.string.pref_default_routing_protocol))
            );
            
            PrimaryBlock.PriorityClass priority
                = PrimaryBlock.PriorityClass.valueOf(
                messageBundle.getString(PRIORITY_KEY,
                    context.getString(R.string.pref_default_priority_class))
            );
            
            PrimaryBlock.LifeTime lifeTime
                = PrimaryBlock.LifeTime.valueOf(
                messageBundle.getString(LIFETIME_KEY,
                    context.getString(R.string.pref_default_lifetime))
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
    
    public MKDTNService() {
        peers = new String[0];
        theirMessengers = new ArrayList<>();
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        BWDTN.init(this);
        dtnManager = BWDTN.getDTNManager();
        dtnClient = BWDTN.getDTNClient(this);
        dtnTextMessenger = BWDTN.getDTNTextMessenger(this);
        
        showPersistentNotification();
    }
    
    private void showPersistentNotification() {
        NotificationManagerCompat notificationManagerCompat
            = NotificationManagerCompat.from(this);
    
        // check if notifications are enabled
        if (!notificationManagerCompat.areNotificationsEnabled()) return;
    
        // create and register channel with the system
        createNotificationChannel();
    
        // make notification builder
        NotificationCompat.Builder notificationBuilder
            = new NotificationCompat.Builder(this, MK_DTN_NOTIFICATION_TAG)
            .setSmallIcon(R.drawable.ic_mkdtn_notification)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.dtn_service_started))
            .setTicker(getString(R.string.dtn_service_started))
            .setShowWhen(false)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NOTIFICATION_VISIBILITY);
    
        // create intent for opening manager activity
        Intent intent = new Intent(this, ManagerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT
        );
        notificationBuilder.setContentIntent(pendingIntent);
        
        startForeground(666, notificationBuilder.build());
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return ourMessenger.getBinder();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        dtnManager.start();
        return Service.START_STICKY;
    }
    
    @Override
    public boolean onUnbind(Intent intent) {
        return false;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        dtnManager.stop();
    }
    
    public static boolean isRunning(@NonNull Context context) {
        ActivityManager manager
            = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (manager == null) return false;

        List<ActivityManager.RunningServiceInfo> services
            = manager.getRunningServices(Integer.MAX_VALUE);
        if (services == null || services.isEmpty()) return false;

        for (ActivityManager.RunningServiceInfo service : services) {
            if (service.service.getClassName().equals(MKDTNService.class.getName())) {
                return true;
            }
        }

        return false;
    }
    
    private static final String MK_DTN_CONFIGURATION_FILE = "mkdtn.conf";
    
    public synchronized static boolean configFileDoesNotExist(@NonNull Context context) {
        if (context.fileList().length == 0) {
//            Log.e(LOG_TAG, "no config file");
            return true;
        } else {
            for (String fileName : context.fileList()) {
                if (fileName.equals(MK_DTN_CONFIGURATION_FILE)) {
//                    Log.i(LOG_TAG, "config file exists");
                    return false;
                }
            }
        }
//        Log.e(LOG_TAG, "no config file");
        return true;
    }
    
    public synchronized static void writeDefaultConfig(@NonNull Context context) {
        try (PrintWriter writer = new PrintWriter(
            context.openFileOutput(MK_DTN_CONFIGURATION_FILE, Context.MODE_PRIVATE))) {
            
            DTNConfig defaultConfig = new DTNConfig();
            
            writer.println(defaultConfig.routingProtocol);
            writer.println(defaultConfig.priorityClass);
            writer.println(defaultConfig.lifetime);
            writer.println(defaultConfig.maxFragmentPayloadSize);
            writer.println(defaultConfig.enableManualMode);
            writer.println(defaultConfig.transmissionMode);
//            Log.i(LOG_TAG, "default config = " + defaultConfig);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Could not write default config", e);
        }
    }
    
    public synchronized static void updateConfig(
        @NonNull Context context, @NonNull DTNConfig updatedConfig
    ) {
        try (PrintWriter writer = new PrintWriter(
            context.openFileOutput(MK_DTN_CONFIGURATION_FILE, Context.MODE_PRIVATE))) {
            writer.println(updatedConfig.routingProtocol);
            writer.println(updatedConfig.priorityClass);
            writer.println(updatedConfig.lifetime);
            writer.println(updatedConfig.maxFragmentPayloadSize);
            writer.println(updatedConfig.enableManualMode);
            writer.println(updatedConfig.transmissionMode);
//            Log.i(LOG_TAG, "updated config = " + updatedConfig);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Could not update config", e);
        }
    }
    
    public synchronized static DTNConfig getConfig(@NonNull Context context) {
        try (Scanner scanner = new Scanner(context.openFileInput(MK_DTN_CONFIGURATION_FILE))) {
            DTNConfig config = new DTNConfig();
            config.routingProtocol = scanner.nextLine();
            config.priorityClass = scanner.nextLine();
            config.lifetime = scanner.nextLine();
            config.maxFragmentPayloadSize = scanner.nextLine();
            config.enableManualMode = Boolean.parseBoolean(scanner.nextLine());
            config.transmissionMode = scanner.nextLine();
//            Log.i(LOG_TAG, "read config = " + config);
            return config;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Could not read config", e);
            return new DTNConfig();
        }
    }
    
    public static class DTNConfig {
        public String routingProtocol;
        public String priorityClass;
        public String lifetime;
        public String maxFragmentPayloadSize;
        public boolean enableManualMode;
        public String transmissionMode;
        
        DTNConfig() {
            routingProtocol = Daemon2Router.RoutingProtocol.PROPHET.toString();
            priorityClass = PrimaryBlock.PriorityClass.NORMAL.toString();
            lifetime = PrimaryBlock.LifeTime.FIVE_HOURS.toString();
            maxFragmentPayloadSize = Daemon2FragmentManager.MAXIMUM_FRAGMENT_PAYLOAD_SIZES[0];
            enableManualMode = false; // auto mode
//            transmissionMode = Daemon2PeerDiscoverer.ServiceMode.SOURCE.toString();
        }
    
        @NonNull
        @Override
        public String toString() {
            return "Config{" +
                "routingProtocol=" + routingProtocol +
                ",priorityClass=" + priorityClass +
                ",lifetime=" + lifetime +
                ",maxFragmentPayloadSize=" + maxFragmentPayloadSize +
                ",enableManualMode=" + enableManualMode +
                ",transmissionMode=" + transmissionMode +
                '}';
        }
    }
}
