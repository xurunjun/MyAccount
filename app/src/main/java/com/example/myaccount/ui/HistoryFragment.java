package com.example.myaccount.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.myaccount.LoginActivity;
import com.example.myaccount.R;
import com.example.myaccount.adapter.AccountAdapter;
import com.example.myaccount.db.DBManager;
import com.example.myaccount.model.AccountBean;
import com.example.myaccount.utils.AnalysisUtils;
import com.example.myaccount.utils.CalendarDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class HistoryFragment extends Fragment implements View.OnClickListener {
    ListView historyLv;
    TextView timeTv;

    List<AccountBean> mDatas;
    AccountAdapter adapter;
    int year, month;
    int dialogSelPos = -1;
    int dialogSelMonth = -1;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.activity_history, container, false);
        Context context = getActivity().getApplicationContext();
        historyLv = root.findViewById(R.id.history_lv);
        timeTv = root.findViewById(R.id.history_tv_time);
        mDatas = new ArrayList<>();
        // 设置适配器
        adapter = new AccountAdapter(getActivity(), mDatas);
        historyLv.setAdapter(adapter);
        initTime();
        timeTv.setText(year + "年" + month + "月");
        loadData(year, month);
        setLVClickListener();
        root.findViewById(R.id.history_iv_back).setOnClickListener(this);
        root.findViewById(R.id.history_iv_rili).setOnClickListener(this);
        return root;
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.history_iv_back:
                getActivity().finish();
                break;
            case R.id.history_iv_rili:
                CalendarDialog dialog = new CalendarDialog(getActivity(), dialogSelPos, dialogSelMonth);
                dialog.show();
                dialog.setDialogSize();
                dialog.setOnRefreshListener(new CalendarDialog.OnRefreshListener() {
                    @Override
                    public void onRefresh(int selPos, int year, int month) {
                        timeTv.setText(year + "年" + month + "月");
                        loadData(year, month);
                        dialogSelPos = selPos;
                        dialogSelMonth = month;
                    }
                });
                break;
        }
    }

    /*设置ListView每一个item的长按事件*/
    private void setLVClickListener() {
        historyLv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                AccountBean accountBean = mDatas.get(position);
                deleteItem(accountBean);
                return false;
            }
        });
    }

    private void deleteItem(final AccountBean accountBean) {
        final int delId = accountBean.getId();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("提示信息").setMessage("您确定要删除这条记录么？")
                .setNegativeButton("取消", null)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Context context = getActivity().getApplicationContext();
                        DBManager.deleteItemFromAccounttbById(delId);
                        mDatas.remove(accountBean);   //实时刷新，从数据源删除
                        adapter.notifyDataSetChanged();
                    }
                });
        builder.create().show();
    }

    /* 获取指定年份月份收支情况的列表*/
    private void loadData(int year, int month) {
        List<AccountBean> list = DBManager.getAccountListOneMonthFromAccounttb(year, month, LoginActivity.getmUsername());
        mDatas.clear();
        mDatas.addAll(list);
        adapter.notifyDataSetChanged();
    }

    private void initTime() {
        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH) + 1;
    }

}
