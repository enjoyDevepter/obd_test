package com.mapbar.hamster.log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;

import timber.log.Timber;

/**
 * Created by guomin on 2018/6/11.
 */

public class FileLoggingTree extends Timber.Tree {


    private File file;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");


    public FileLoggingTree(String filePath) {
        File dir = new File(filePath);
        while (null != dir.listFiles() && dir.listFiles().length > 5) {
            File[] files = dir.listFiles();
            File deleteFile = null;
            if (files[0].getName().equals("rssi.txt")) {
                deleteFile = files[1];
            } else {
                deleteFile = files[0];
            }
            for (int i = 0; i < files.length; i++) {
                if (deleteFile != files[i] && deleteFile.lastModified() > files[i].lastModified() && !files[i].getName().equals("rssi.txt")) {
                    deleteFile = files[i];
                }
            }
            deleteFile.delete();
        }
        file = new File(filePath, "/" + sdf.format(System.currentTimeMillis()) + ".txt");
        new File(file.getParent()).mkdirs();
    }

    @Override
    protected void log(int priority, String tag, String message, Throwable t) {
        if (null == file) {
            return;
        }

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file, true);
            fos.write(message.getBytes());
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
