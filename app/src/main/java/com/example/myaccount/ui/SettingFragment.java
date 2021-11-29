package com.example.myaccount.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.myaccount.R;
import com.example.myaccount.db.DBManager;
import com.example.myaccount.utils.AnalysisUtils;

public class SettingFragment extends Fragment implements View.OnClickListener {
    String username;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.activity_setting, container, false);
        Context context = getActivity().getApplicationContext();
        username = AnalysisUtils.readLoginUserName(context);
        root.findViewById(R.id.setting_iv_back).setOnClickListener(this);
        root.findViewById(R.id.setting_tv_clear).setOnClickListener(this);
        return root;
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.setting_iv_back:
                getActivity().finish();
                break;
            case R.id.setting_tv_clear:
                showDeleteDialog();
                break;
        }
    }

    private void showDeleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("删除提示")
                .setMessage("您确定要删除所有记录么？\n注意：删除后无法恢复，请慎重选择！")
                .setPositiveButton("取消", null)
                .setNegativeButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DBManager.deleteAllAccount(username);
                        Toast.makeText(getActivity(), "删除成功！", Toast.LENGTH_SHORT).show();
                    }
                });
        builder.create().show();
    }
}
