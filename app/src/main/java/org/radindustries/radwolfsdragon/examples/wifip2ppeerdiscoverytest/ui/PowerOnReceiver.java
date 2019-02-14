package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PowerOnReceiver extends BroadcastReceiver {
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null && action.equals(Intent.ACTION_BOOT_COMPLETED)) {
                context.startService(new Intent(context, MKDTNService.class));
            }
        }
    }
}
