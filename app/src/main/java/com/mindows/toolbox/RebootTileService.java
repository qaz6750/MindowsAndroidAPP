package com.mindows.toolbox;

import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

public class RebootTileService extends TileService {

    @Override
    public void onClick() {
        Tile tile = getQsTile();
        if (tile == null) {
            return;
        }

        try {
            boolean isABSupported;
            try {
                Class<?> clz = Class.forName("android.os.SystemProperties");
                Method get = clz.getDeclaredMethod("get", String.class, String.class);
                String result = (String) get.invoke(clz, "ro.build.ab_update", "false");
                isABSupported = result != null && result.equals("true");
            } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException |
                     IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            if (getSharedPreferences("data", 0).getBoolean("wrongAB", false)) {
                isABSupported = getSharedPreferences("data", 0).getBoolean("customAB", false);
            }
            Process process = Runtime.getRuntime().exec("su");
            OutputStream out = process.getOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(out);

            dataOutputStream.write(isABSupported ? ("if [ -f /sdcard/Mindows助手/uefi.img ] ;then\n" +
                    "if [ -a /dev/block/by-name ] ;then  \n" +
                    "dd if=/sdcard/Mindows助手/uefi.img of=/dev/block/by-name/boot_a;dd if=/sdcard/Mindows助手/uefi.img of=/dev/block/by-name/boot_b\n" +
                    "elif  [ -a /dev/block/bootdevice/by-name ] ;then  \n" +
                    "dd if=/sdcard/Mindows助手/uefi.img of=/dev/block/bootdevice/by-name/boot_a;dd if=/sdcard/Mindows助手/uefi.img of=/dev/block/bootdevice/by-name/boot_b\n" +
                    "else\n" +
                    "echo '未找到by-name文件夹' >&2;return 1\n" +
                    "fi\n" +
                    "else \n" +
                    "echo '请您先放置uefi文件至/sdcard/Mindows助手/uefi.img' >&2;return 2\n" +
                    "fi").getBytes(StandardCharsets.UTF_8) : ("if [ -f /sdcard/Mindows助手/uefi.img ] ;then\n" +
                    "if [ -a /dev/block/by-name ] ;then  \n" +
                    "dd if=/sdcard/Mindows助手/uefi.img of=/dev/block/by-name/boot\n" +
                    "elif  [ -a /dev/block/bootdevice/by-name ] ;then  \n" +
                    "dd if=/sdcard/Mindows助手/uefi.img of=/dev/block/bootdevice/by-name/boot\n" +
                    "else\n" +
                    "echo '未找到by-name文件夹' >&2;return 1\n" +
                    "fi\n" +
                    "else \n" +
                    "echo '请您先放置uefi文件至/sdcard/Mindows助手/uefi.img' >&2;return 2\n" +
                    "fi").getBytes(StandardCharsets.UTF_8));
            dataOutputStream.flush();
            dataOutputStream.close();

            process.waitFor();
            switch (process.exitValue()) {
                case 0:
                    Toast.makeText(this, "将在1秒后重启Windows", Toast.LENGTH_SHORT).show();
                    Process process1 = Runtime.getRuntime().exec("su");
                    OutputStream out1 = process1.getOutputStream();
                    DataOutputStream dataOutputStream1 = new DataOutputStream(out1);
                    dataOutputStream1.writeBytes("sleep 1;reboot");
                    dataOutputStream1.flush();
                    dataOutputStream1.close();
                    break;
                case 1:
                    Toast.makeText(this, "未找到by-name文件夹", Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    Toast.makeText(this, "未找到/sdcard/Mindows助手/uefi.img", Toast.LENGTH_SHORT).show();
                    break;
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            Toast.makeText(this, "无法获取root权限", Toast.LENGTH_SHORT).show();
        }
        super.onClick();
    }
}
