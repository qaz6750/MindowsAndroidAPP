package com.mindows.toolbox.ui.home;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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

import com.mindows.toolbox.ImageActivity;
import com.mindows.toolbox.databinding.FragmentHomeBinding;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class HomeFragment extends Fragment {

    TextView textView;
    static boolean detected = false;
    boolean isABSupported;
    private FragmentHomeBinding binding;


    protected MyHandler mHandler = new MyHandler(this);

    public static class MyHandler extends Handler {
        private final WeakReference<HomeFragment> mOuter;

        public MyHandler(HomeFragment activity) {
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

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        boolean isForbidReboot;
        String result1;
        try {
            Class<?> clz = Class.forName("android.os.SystemProperties");
            Method get = clz.getDeclaredMethod("get", String.class, String.class);
            String result = (String) get.invoke(clz, "ro.build.ab_update", "false");
            isABSupported = result != null && result.equals("true");
            result1 = (String) get.invoke(clz, "ro.product.device", "false");
            isForbidReboot = result1 != null && Arrays.asList("duo", "").contains(result1);
        } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        if (getActivity().getSharedPreferences("data", 0).getBoolean("wrongAB", false)) {
            isABSupported = getActivity().getSharedPreferences("data", 0).getBoolean("customAB", false);
        }
        textView = binding.textLog;
        textView.setText("此处会显示重启日志\n");
        textView.setTextColor(Color.DKGRAY);
        final Button button1 = binding.helpReboot;
        //button1.setVisibility(isABSupported ? View.VISIBLE : View.GONE);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ImageActivity.class).putExtra("pic", "1");
                startActivity(intent);
            }
        });
        final Button button = binding.textHome;
        if (!detected) {
            detected = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Process process = Runtime.getRuntime().exec("su");
                        OutputStream out = process.getOutputStream();
                        DataOutputStream dataOutputStream = new DataOutputStream(out);
                        dataOutputStream.write(("if [ -d /sdcard/Mindows助手 ] ;then \n" +
                                "sleep 0\n" +
                                "else\n" +
                                "mkdir -p /sdcard/Mindows助手\n" +
                                "fi\n").getBytes(StandardCharsets.UTF_8));
                        dataOutputStream.flush();
                        dataOutputStream.close();
                    } catch (Exception ignored) {
                    }
                }
            }).start();
        }
        button.setText(isForbidReboot ? "暂不支持\n您的机型" : "重启到Windows");
        button.setEnabled(!isForbidReboot);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View.OnClickListener onClickListener1 = this;
                Toast.makeText(getActivity(), "再次点击以确认重启", Toast.LENGTH_SHORT).show();
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Looper.prepare();
                                try {
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


                                    //开启新线程，实时读取命令输出
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                BufferedReader mReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                                                String inline;
                                                while ((inline = mReader.readLine()) != null) {

                                                    //如果TextView的字符太多了（会使得软件非常卡顿），或者用户退出了执行界面（br为true），则停止读取
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

                                                    //如果TextView的字符太多了（会使得软件非常卡顿），或者用户退出了执行界面（br为true），则停止读取
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
                                            Toast.makeText(getActivity(), "将在1秒后重启Windows", Toast.LENGTH_SHORT).show();
                                            Process process1 = Runtime.getRuntime().exec("su");
                                            OutputStream out1 = process1.getOutputStream();
                                            DataOutputStream dataOutputStream1 = new DataOutputStream(out1);
                                            dataOutputStream1.writeBytes("sleep 1;reboot");
                                            dataOutputStream1.flush();
                                            dataOutputStream1.close();
                                            break;
                                        case 1:
                                            Toast.makeText(getActivity(), "未找到by-name文件夹", Toast.LENGTH_SHORT).show();
                                            break;
                                        case 2:
                                            Toast.makeText(getActivity(), "未找到/sdcard/Mindows助手/uefi.img", Toast.LENGTH_SHORT).show();
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
                button.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        button.setOnClickListener(onClickListener1);
                    }
                }, 2000);
            }
        });

        final TextView textAB = binding.textAB;
        textAB.setText(String.format("您的设备是%s分区的设备 (如果识别错误，请长按此处纠错)\n\n", isABSupported ? "A/B" : "A Only"));
        textAB.append((isABSupported ? "对于A/B分区的设备，我们会帮您将\"/sdcard/Mindows助手/uefi.img\"写入boot_a和boot_b分区，然后重启进入Windows系统。\n" : "对于A Only设备，我们会帮您将\"/sdcard/Mindows助手/uefi.img\"写入boot分区，然后重启进入Windows系统。\n") +
                "\n注意事项：\n1，重启前请保存好正在编辑的文件，否则会丢失。\n2，请自行将电脑上的Mindows工具箱安装文件夹中bin/res/" + result1 + "/usr/uefi.img（或bin/res/" + result1 + "/woa/uefi-common.img）文件复制到内部存储中的Mindows助手文件夹内(/sdcard/Mindows助手)，并改名为uefi.img。\n3，此操作需要Root权限，请授权Root否则无法完成重启。\n");
        textAB.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                new AlertDialog.Builder(getActivity()).setTitle("纠正AB分区状态").setMessage("请选择您的设备AB分区状态")
                        .setNegativeButton("A/B", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                getActivity().getSharedPreferences("data", 0).edit().putBoolean("customAB", true).putBoolean("wrongAB", true).apply();
                                Toast.makeText(getActivity(), "重新进入本界面后生效", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setPositiveButton("A Only", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                getActivity().getSharedPreferences("data", 0).edit().putBoolean("customAB", false).putBoolean("wrongAB", true).apply();
                                Toast.makeText(getActivity(), "重新进入本界面后生效", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .show();

                return false;
            }
        });
        return root;

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}