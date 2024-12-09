package com.mindows.toolbox.ui.dashboard;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.mindows.toolbox.ImageActivity;
import com.mindows.toolbox.databinding.FragmentDashboardBinding;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;

public class DashboardFragment extends Fragment {

    TextView textView;
    private FragmentDashboardBinding binding;

    protected MyHandler mHandler = new MyHandler(this);

    public static class MyHandler extends Handler {
        private final WeakReference<DashboardFragment> mOuter;

        public MyHandler(DashboardFragment activity) {
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

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("data", 0);
        textView = binding.textLog1;
        textView.setText("此处会显示挂载日志\n");
        // boolean  isNight = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_YES) == Configuration.UI_MODE_NIGHT_YES;
        textView.setTextColor(Color.DKGRAY);
        final Button button = binding.textDashboard;
        button.setText("挂载\n分区");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Looper.prepare();
                        try {
                            final String mountAddr;
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
                                            "mindowswinAddr=$(" + getActivity().getApplicationInfo().nativeLibraryDir + "/libblktool.so -n -N mindowswin -l 2>&1)\n" +
                                            getActivity().getApplicationInfo().nativeLibraryDir + "/libblktool.so -n -N mindowswin -l >/dev/null 2>&1\n" +
                                            "if [ $? -eq 0 ];then\n" +
                                            "umount -f -d \"$mindowswinAddr\" >/dev/null 2>&1\n" +
                                            getActivity().getApplicationInfo().nativeLibraryDir + "/libntfs-3g.so -o rw \"$mindowswinAddr\" " + mountAddr + "/Windows系统分区;let a=1\n" +
                                            "fi\n" +
//                                            "fi\n" +

//                                            "if [ $b -eq 0 ];then\n" +
                                            "mindowsdatAddr=$(" + getActivity().getApplicationInfo().nativeLibraryDir + "/libblktool.so -n -N mindowsdat -l 2>&1)\n" +
                                            getActivity().getApplicationInfo().nativeLibraryDir + "/libblktool.so -n -N mindowsdat -l >/dev/null 2>&1\n" +
                                            "if [ $? -eq 0 ];then\n" +
                                            "umount -f -d \"$mindowsdatAddr\" >/dev/null 2>&1\n" +
                                            getActivity().getApplicationInfo().nativeLibraryDir + "/libntfs-3g.so -o rw \"$mindowsdatAddr\" " + mountAddr + "/Windows数据分区;let b=1\n" +
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
                                    Toast.makeText(getActivity(), "挂载成功，请前往 " + mountAddr + " 查看文件", Toast.LENGTH_SHORT).show();
                                    break;
                                case 1:
                                    Toast.makeText(getActivity(), "未找到您设备上的共享分区", Toast.LENGTH_SHORT).show();
                                    break;
                                case 2:
                                    Toast.makeText(getActivity(), "挂载失败", Toast.LENGTH_SHORT).show();
                                    break;
                                case 3:
                                    Toast.makeText(getActivity(), "已经挂载成功了，无需再挂载啦", Toast.LENGTH_SHORT).show();
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

        final TextView textDashboard = binding.textShare;
        textDashboard.setText("功能说明：Mindows助手支持挂载Windows系统分区和数据分区（数据分区即“D盘”，如果你选择创建了的话）。挂载成功后即可在手机上访问、修改分区内的数据。\n\n长按此处可自定义挂载位置(默认挂载位置为\"/sdcard/Mindows助手\")。\n\n注意事项：1，此功能需要Root权限，请授权Root后使用。2，");
        SpannableString ss = new SpannableString("请将Magisk--设置--挂载命名空间模式 改为“全局命名空间” ");
        ss.setSpan(new ForegroundColorSpan(Color.RED), 0, ss.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        textDashboard.append(ss);
        textDashboard.append("。3，每次重启后都需要重新挂载。");


        textDashboard.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                final String[] items = {"/sdcard/Mindows助手(默认)", "/data/Mindows助手", "自定义(" + sharedPreferences.getString("customMountAddr", "暂未设置") + ")"};
                new AlertDialog.Builder(getActivity())
                        .setTitle("自定义挂载位置")
                        .setSingleChoiceItems(items, sharedPreferences.getInt("customMountState", 0), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                sharedPreferences.edit().putInt("customMountState", which).apply();
                                //选择一个选项之后，就立即关闭对话框
                                dialog.dismiss();
                                if (which == 2) {
                                    final EditText editText = new EditText(getActivity());
                                    editText.setHint("/sdcard/Mindows助手");
                                    String str = sharedPreferences.getString("customMountAddr", null);
                                    if (str != null)
                                        editText.setText(str);
                                    editText.setSelectAllOnFocus(true);

                                    new AlertDialog.Builder(getActivity())
                                            .setTitle("设置挂载路径")
                                            .setView(editText)
                                            .setNegativeButton("保存", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    Editable text = editText.getText();
                                                    String str = editText.getHint().toString();
                                                    if (text != null) {
                                                        String str1 = text.toString();
                                                        if (str1.length() > 0 && str1.startsWith("/"))
                                                            str = str1;
                                                    }

                                                    sharedPreferences.edit().putString("customMountAddr", str).putInt("customMountState", 2).apply();

                                                }
                                            })
                                            .setPositiveButton("返回", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    sharedPreferences.edit().putInt("customMountState", 0).apply();
                                                }
                                            })
                                            .show();
                                }
                            }
                        })
                        .show();
                return false;
            }
        });

        final Button help = binding.help;
        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getActivity(), ImageActivity.class).putExtra("pic", "2");
                startActivity(intent);
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