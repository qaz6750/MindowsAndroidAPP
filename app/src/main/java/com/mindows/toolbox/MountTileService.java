package com.mindows.toolbox;

import android.content.SharedPreferences;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class MountTileService extends TileService {
    @Override
    public void onClick() {
        Tile tile = getQsTile();
        if (tile == null) {
            return;
        }

        try {
            final String mountAddr;
            SharedPreferences sharedPreferences = getSharedPreferences("data", 0);
            switch (sharedPreferences.getInt("customMountState", 0)) {
                case 0:
                    mountAddr = "/sdcard/Mindows助手";
                    break;
                case 1:
                    mountAddr = "/data/Mindows助手";
                    break;
                default:
                    mountAddr = sharedPreferences.getString("customMountAddr", "/sdcard/Mindows助手");
                    break;
            }

            Process process = Runtime.getRuntime().exec("su");
            OutputStream out = process.getOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(out);
            dataOutputStream.write((

                    "let a=0;let b=0\n" +

//                                            "if mount | grep '/sdcard/Mindows助手/Windows系统分区' ;then\n" +
//                                            "echo '您的Windows系统分区已挂载，请前往 /sdcard/Mindows助手/Windows系统分区 查看';let a=1\n" +
//                                            "fi\n" +
//
//                                            "if mount | grep '/sdcard/Mindows助手/Windows数据分区' ;then\n" +
//                                            "echo '您的Windows数据分区已挂载，请前往 /sdcard/Mindows助手/Windows数据分区 查看';let b=1\n" +
//                                            "fi\n" +


                            "if [ -d " + mountAddr + "/Windows系统分区 ] ;then\n" +
                            "sleep 0\n" +
                            "else\n" +
                            "mkdir -p " + mountAddr + "/Windows系统分区\n" +
                            "fi \n" +


                            "if [ -d " + mountAddr + "/Windows数据分区 ] ;then\n" +
                            "sleep 0\n" +
                            "else\n" +
                            "mkdir -p " + mountAddr + "/Windows数据分区\n" +
                            "fi \n" +


//                                            "if [ $a -eq 1 ];then\n" +
//                                            "if [ $b -eq 1 ];then\n" +
//                                            "return 3;" +
//                                            "fi\n" +
//                                            "fi\n" +


//                                            "if [ $a -eq 0 ];then\n" +
                            "mindowswinAddr=$(" + getApplicationInfo().nativeLibraryDir + "/libblktool.so -n -N mindowswin -l 2>&1)\n" +
                            getApplicationInfo().nativeLibraryDir + "/libblktool.so -n -N mindowswin -l >/dev/null 2>&1\n" +
                            "if [ $? -eq 0 ];then\n" +
                            "umount -f -d \"$mindowswinAddr\" >/dev/null 2>&1\n" +
                            getApplicationInfo().nativeLibraryDir + "/libntfs-3g.so -o rw \"$mindowswinAddr\" " + mountAddr + "/Windows系统分区;let a=1\n" +
                            "fi\n" +
//                                            "fi\n" +

//                                            "if [ $b -eq 0 ];then\n" +
                            "mindowsdatAddr=$(" + getApplicationInfo().nativeLibraryDir + "/libblktool.so -n -N mindowsdat -l 2>&1)\n" +
                            getApplicationInfo().nativeLibraryDir + "/libblktool.so -n -N mindowsdat -l >/dev/null 2>&1\n" +
                            "if [ $? -eq 0 ];then\n" +
                            "umount -f -d \"$mindowsdatAddr\" >/dev/null 2>&1\n" +
                            getApplicationInfo().nativeLibraryDir + "/libntfs-3g.so -o rw \"$mindowsdatAddr\" " + mountAddr + "/Windows数据分区;let b=1\n" +
                            "fi\n" +
//                                            "fi\n" +


                            "if [ $a -eq 0 ];then\n" +
                            "echo '未在您的设备上找到Windows系统分区' >&2\n" +
                            "else\n" +
                            "if mount | grep \"$mindowswinAddr\" ;then\n" +
                            "echo '成功挂载Windows系统分区！请前往 " + mountAddr + "/Windows系统分区 查看'\n" +
                            "fi\n" +
                            "fi\n" +

                            "if [ $b -eq 0 ];then\n" +
                            "echo '未在您的设备上找到Windows数据分区'\n" +
                            "else\n" +
                            "if mount | grep \"$mindowsdatAddr\" ;then\n" +
                            "echo '成功挂载Windows数据分区！请前往 " + mountAddr + "/Windows数据分区 查看'\n" +
                            "fi\n" +
                            "fi\n" +


                            "if [ $a -eq 0 ];then\n" +
                            "return 1\n" +
                            "fi\n"

            ).getBytes(StandardCharsets.UTF_8));
            dataOutputStream.flush();
            dataOutputStream.close();

            process.waitFor();
            switch (process.exitValue()) {
                case 0:
                    Toast.makeText(this, "挂载成功，请前往 " + mountAddr + " 查看文件", Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    Toast.makeText(this, "未找到您设备上的共享分区", Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    Toast.makeText(this, "挂载失败", Toast.LENGTH_SHORT).show();
                    break;
                case 3:
                    Toast.makeText(this, "已经挂载成功了，无需再挂载啦", Toast.LENGTH_SHORT).show();
                    break;
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            Toast.makeText(this, "无法获取root权限", Toast.LENGTH_SHORT).show();
        }
        super.onClick();
    }
}
