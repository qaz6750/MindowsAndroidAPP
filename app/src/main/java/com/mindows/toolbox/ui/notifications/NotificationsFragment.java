package com.mindows.toolbox.ui.notifications;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.mindows.toolbox.databinding.FragmentNotificationsBinding;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textNotifications;
        textView.append(Html.fromHtml("<br><font color=\"#B0B0B0\"><small><i>Mindows工具箱的手机端辅助软件</></></>"));
        textView.append(Html.fromHtml("<br><font color=\"#B0B0B0\"><small><i>点我向开发者反馈 bug或建议！</></></>"));
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("http://www.coolapk.com/u/2638279");
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                intent.setData(uri);
                startActivity(intent);
            }
        });

        final TextView textView1 = binding.text1;
        textView1.append(Html.fromHtml("<br><font color=\"#B0B0B0\"><small><i>为手机一键刷入 Windows</></></>"));
        textView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("https://mindows.cn");
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                intent.setData(uri);
                startActivity(intent);
            }
        });

        final TextView textView2 = binding.text2;
        textView2.append(Html.fromHtml("<br><font color=\"#B0B0B0\"><small><i>Mindows项目主要成员</></></>"));
        textView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("http://www.coolapk.com/u/3463951");
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                intent.setData(uri);
                startActivity(intent);
            }
        });

        final TextView textView3 = binding.text3;
        textView3.append(Html.fromHtml("<br><font color=\"#B0B0B0\"><small><i>点击访问项目 Github主页</></></>"));
        textView3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("https://renegade-project.tech/zh/home");
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                intent.setData(uri);
                startActivity(intent);
            }
        });


        final TextView textView4 = binding.text4;
        textView4.append(Html.fromHtml("<br><font color=\"#B0B0B0\"><small><i>点击访问项目 Github主页</></></>"));
        textView4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("https://github.com/WOA-Project");
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                intent.setData(uri);
                startActivity(intent);
            }
        });


        final TextView textView5 = binding.text5;
        textView5.append(Html.fromHtml("<br><font color=\"#B0B0B0\"><small><i>点击访问项目 Github主页</></></>"));
        textView5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("https://github.com/woa-msmnile");
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                intent.setData(uri);
                startActivity(intent);
            }
        });

        final Button button = binding.b1;
        try {
            button.append("\n当前版本："+getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(),0).versionName);
        } catch (Exception ignored) {

        }
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("https://www.123pan.com/s/8eP9-0DTGA");
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                intent.setData(uri);
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