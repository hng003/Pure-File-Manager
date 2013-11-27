package com.docd.purefm;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.io.File;
import java.util.Set;
import java.util.TreeSet;

public final class Environment {
    
    private Environment() {}
    
    private static final Set<File> usbStorageDirectories = new TreeSet<File>();
    public static final File rootDirectory = File.listRoots()[0];
    public static final File externalStorageDirectory = android.os.Environment.getExternalStorageDirectory();
    public static final File androidRootDirectory = android.os.Environment.getRootDirectory();
    private static File secondaryStorageDirectory;
    private static boolean isExternalStorageMounted;
    
    private static Context context;

    public static boolean hasBusybox;
    public static boolean hasRoot;
    
    public static String busybox;
    
    static void init(final PureFM pureFM) {
        context = pureFM;
        busybox = getUtilPath("busybox");
        if (busybox == null) {
            busybox = getUtilPath("busybox-ba");
        }
        hasBusybox = busybox != null;
        hasRoot = isUtilAvailable("su");
        updateExternalStorageState();
        ActivityMonitor.addOnActivitiesOpenedListener(new ActivityMonitorListener());
    }
    
    public static boolean isExternalStorageMounted() {
        return isExternalStorageMounted;
    }
    
    public static File getSecondaryStorageDirectory() {
        return secondaryStorageDirectory;
    }
    
    public static Set<File> getUsbStorageDirectories() {
        return usbStorageDirectories;
    }
    
    @SuppressLint("SdCardPath")
    public static String getUtilPath(String utilname) {
        final String[] places = { "/sbin/", "/system/bin/", "/system/xbin/", "/data/local/xbin/",
                "/data/local/bin/", "/system/sd/xbin/", "/system/bin/failsafe/", "/data/local/", "/data/data/burrows.apps.busybox/app_busybox/", "/data/data/burrows.apps.busybox.paid/app_busybox/"};
        
        for (int i = 0; i < places.length; i++) {
            final File[] files = new File(places[i]).listFiles();
            if (files != null) {
                for (int j = 0; j < files.length; j++) {
                    final File current = files[j];
                    if (current.getName().equals(utilname)) {
                        return current.getAbsolutePath();
                    }
                }
            }
        }
        return null;
    }
    
    @SuppressLint("SdCardPath")
    public static boolean isUtilAvailable(String utilname) {
        final String[] places = { "/sbin/", "/system/bin/", "/system/xbin/", "/data/local/xbin/",
                "/data/local/bin/", "/system/sd/xbin/", "/system/bin/failsafe/", "/data/local/", "/data/data/burrows.apps.busybox/app_busybox/", "/data/data/burrows.apps.busybox.paid/app_busybox/"};
        
        for (int i = 0; i < places.length; i++) {
            final String[] files = new File(places[i]).list();
            if (files != null) {
                for (int j = 0; j < files.length; j++) {
                    if (files[j].equals(utilname)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    private static void resolveStorages() {
        final File externalStorage = android.os.Environment.getExternalStorageDirectory();
        final File parent = externalStorage.getParentFile();
        if (parent != null) {
            for (final File file : parent.listFiles()) {
                if (!file.equals(externalStorage) && file.canRead() && file.canWrite() && file.canExecute()) {
                    final String fileName = file.getName();
                    if (fileName.equals("extSdCard") || fileName.equals("external_sd")) {
                        secondaryStorageDirectory = file;
                    } else if (fileName.contains("usb") || fileName.contains("USB") || fileName.contains("Usb")) {
                        usbStorageDirectories.add(file);
                    }
                }
            }
        }
    }
    
    private static boolean isExternalMounted() {
        final String state = android.os.Environment.getExternalStorageState();
        return state.equals(android.os.Environment.MEDIA_MOUNTED) ||
                state.equals(android.os.Environment.MEDIA_MOUNTED_READ_ONLY);
    }
        
    // ============== STORAGE LISTENER ===============
    
    static void updateExternalStorageState() {
        isExternalStorageMounted = isExternalMounted();
        if (isExternalStorageMounted && secondaryStorageDirectory == null) {
            resolveStorages();
        }
    }
    
    static final ExternalStorageStateReceiver externalStorageStateReceiver =
            new ExternalStorageStateReceiver();
    
    static final class ExternalStorageStateReceiver extends BroadcastReceiver {
        
        static final IntentFilter intentFilter = new IntentFilter();
        
        static {
            intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
            intentFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
        }
        
        @Override
        public void onReceive(Context context, Intent intent) {
            updateExternalStorageState();
        }
    }
    
    // =============== ACTIVITY MONITOR ===============

    private static final class ActivityMonitorListener implements ActivityMonitor.OnActivitiesOpenedListener {

        @Override
        public void onActivitiesOpen() {
            context.registerReceiver(externalStorageStateReceiver, ExternalStorageStateReceiver.intentFilter);
        }

        @Override
        public void onActivitiesClosed() {
            context.unregisterReceiver(externalStorageStateReceiver);
        }
    }
}
