package com.mindows.toolbox.ui.backup;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.mindows.toolbox.databinding.FragmentBackupBinding;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

public class BackupFragment extends Fragment {

    TextView textView;
    boolean isABSupported;
    String resultAB, result1;
    ;
    private FragmentBackupBinding binding;

    protected MyHandler mHandler = new MyHandler(this);

    public static class MyHandler extends Handler {
        private final WeakReference<BackupFragment> mOuter;

        public MyHandler(BackupFragment activity) {
            mOuter = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {

            //msg.what 是1就是错误信息，是2是正常信息
            mOuter.get().textView.append(msg.what == 1 ? (SpannableString) msg.obj : (String) msg.obj);
        }
    }


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        try {
            Class<?> clz = Class.forName("android.os.SystemProperties");
            Method get = clz.getDeclaredMethod("get", String.class, String.class);
            String result = (String) get.invoke(clz, "ro.build.ab_update", "false");
            isABSupported = result != null && result.equals("true");
            result1 = (String) get.invoke(clz, "ro.product.device", "false");
            resultAB = (String) get.invoke(clz, "ro.boot.slot_suffix", "");
        } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        if (getActivity().getSharedPreferences("data", 0).getBoolean("wrongAB", false)) {
            isABSupported = getActivity().getSharedPreferences("data", 0).getBoolean("customAB", false);
            if (!isABSupported) resultAB = "";
        }

        binding = FragmentBackupBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        textView = binding.textLog2;
        textView.setText("此处会显示分区备份日志\n");
        textView.setTextColor(Color.DKGRAY);
        final Button button = binding.textBackup;
        button.setText("备份\n分区");

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Looper.prepare();
                        try {
                            Process process = Runtime.getRuntime().exec("su");
                            OutputStream out = process.getOutputStream();
                            DataOutputStream dataOutputStream = new DataOutputStream(out);
                            dataOutputStream.write((
                                    "if [ -d /sdcard/Mindows助手/Mindows备份 ] ;then \n" +
                                            "sleep 0\n" +
                                            "else\n" +
                                            "mkdir -p /sdcard/Mindows助手/Mindows备份\n" +
                                            "fi\n" +
                                            "cp -r /sys/firmware/fdt /sdcard/Mindows助手/Mindows备份/" + result1 + "_fdt\n" +
                                            "if [ -a /dev/block/by-name/ ] ;then \n" +
                                            "[ -a /dev/block/by-name/boot" + resultAB + " ] && dd if=/dev/block/by-name/boot" + resultAB + " of=/sdcard/Mindows助手/Mindows备份/" + result1 + "_boot" + resultAB + ".img;[ -a /dev/block/by-name/xbl" + resultAB + " ] && dd if=/dev/block/by-name/xbl" + resultAB + " of=/sdcard/Mindows助手/Mindows备份/" + result1 + "_xbl" + resultAB + ".img\n" +
                                            "elif [ -a /dev/block/bootdevice/by-name/ ] ;then \n" +
                                            "[ -a /dev/block/bootdevice/by-name/boot" + resultAB + " ] && dd if=/dev/block/bootdevice/by-name/boot" + resultAB + " of=/sdcard/Mindows助手/Mindows备份/" + result1 + "_boot" + resultAB + ".img;[ -a /dev/block/bootdevice/by-name/xbl" + resultAB + " ] && dd if=/dev/block/bootdevice/by-name/xbl" + resultAB + " of=/sdcard/Mindows助手/Mindows备份/" + result1 + "_xbl" + resultAB + ".img\n" +
                                            "else\n" +
                                            "echo '未在您的设备上找到boot和xbl分区!' >&2;return 2\n" +
                                            "fi\n" +
                                            "if [ -a /sdcard/Mindows助手/Mindows备份/" + result1 + "_boot" + resultAB + ".img ] ;then \n" +
                                            "cd /sdcard/Mindows助手/Mindows备份/;" + getActivity().getApplicationInfo().nativeLibraryDir + "/libmagiskboot.so unpack " + result1 + "_boot" + resultAB + ".img;rm kernel;rm ramdisk*\n" +
                                            "fi\n"
                            ).getBytes(StandardCharsets.UTF_8));
                            dataOutputStream.flush();
                            dataOutputStream.close();


                            //开启新线程，实时读取命令输出
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        BufferedReader mReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                                        String inline;
                                        while ((inline = mReader.readLine()) != null) {

                                            //如果TextView的字符太多了（会使得软件非常卡顿），则停止读取
                                            if (textView.length() > 2000) break;
                                            Message msg = new Message();
                                            msg.what = 0;
                                            msg.obj = inline.equals("") ? "\n" : inline + "\n";
                                            mHandler.sendMessage(msg);
                                        }
                                        mReader.close();
                                    } catch (Exception ignored) {
                                    }
                                }
                            }).start();

                            //开启新线程，实时读取命令报错信息
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        BufferedReader mReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                                        String inline;
                                        while ((inline = mReader.readLine()) != null) {

                                            //如果TextView的字符太多了（会使得软件非常卡顿），则停止读取
                                            if (textView.length() > 2000) break;
                                            Message msg = new Message();
                                            msg.what = 1;
                                            if (inline.equals(""))
                                                msg.obj = new SpannableString("\n");
                                            else {
                                                SpannableString ss = new SpannableString(inline + "\n");
                                                ss.setSpan(new ForegroundColorSpan(Color.RED), 0, ss.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                                msg.obj = ss;
                                            }
                                            mHandler.sendMessage(msg);
                                        }
                                        mReader.close();
                                    } catch (Exception ignored) {
                                    }
                                }
                            }).start();

                            process.waitFor();
                            switch (process.exitValue()) {
                                case 0:
                                    Toast.makeText(getActivity(), "备份成功，请前往 /sdcardMindows助手/Mindows备份 查看", Toast.LENGTH_SHORT).show();
                                    break;
                                case 2:
                                    Toast.makeText(getActivity(), "未找到您设备上的boot和xbl分区", Toast.LENGTH_SHORT).show();
                                    break;
                            }
                        } catch (IOException | InterruptedException e) {
                            e.printStackTrace();
                            Toast.makeText(getActivity(), "无法获取root权限", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).start();
            }
        });

        final TextView textbackupnote = binding.textbackupnote;
        textbackupnote.setText("设备代号：" + result1 + "\n功能说明：备份您的boot分区、xbl分区和fdt文件(即/sys/firmware/fdt)至存储根目录下的'Mindows备份'文件夹（即/sdcard/Mindows备份）。\n\n");
        textbackupnote.append(isABSupported ? "您的设备是A/B分区，且当前为" + resultAB + "槽位。所以，我们会将boot" + resultAB + "和xbl" + resultAB + "分区备份至/sdcard。" : "您的设备是A only分区。所以我们会将boot分区和xbl分区备份至/sdcard。");

        return root;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}