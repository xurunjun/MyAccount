package com.example.myaccount.ui;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.example.myaccount.R;
import com.example.myaccount.adapter.ChartVPAdapter;
import com.example.myaccount.db.DBManager;
import com.example.myaccount.frag_chart.IncomChartFragment;
import com.example.myaccount.frag_chart.OutcomChartFragment;
import com.example.myaccount.utils.AnalysisUtils;
import com.example.myaccount.utils.CalendarDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MonthChartFragment extends Fragment implements View.OnClickListener {
    Button inBtn, outBtn;
    TextView dateTv, inTv, outTv;
    ViewPager chartVp;
    int year;
    int month;
    int selectPos = -1, selectMonth = -1;
    List<Fragment> chartFragList;
    private IncomChartFragment incomChartFragment;
    private OutcomChartFragment outcomChartFragment;
    private ChartVPAdapter chartVPAdapter;
    String username;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.activity_month_chart, container, false);
        Context context = getActivity().getApplicationContext();
        username = AnalysisUtils.readLoginUserName(context);
        initView(root);
        initTime();
        initStatistics(year, month);
        initFrag();
        setVPSelectListener();
        return root;
    }

    private void setVPSelectListener() {
        chartVp.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                setButtonStyle(position);
            }
        });
    }

    private void initFrag() {
        chartFragList = new ArrayList<>();
//        ??????Fragment?????????
        incomChartFragment = new IncomChartFragment();
        outcomChartFragment = new OutcomChartFragment();
//        ???????????????Fragment??????
        Bundle bundle = new Bundle();
        bundle.putInt("year", year);
        bundle.putInt("month", month);
        incomChartFragment.setArguments(bundle);
        outcomChartFragment.setArguments(bundle);
//        ???Fragment????????????????????????
        chartFragList.add(outcomChartFragment);
        chartFragList.add(incomChartFragment);
//        ???????????????
        chartVPAdapter = new ChartVPAdapter(getActivity().getSupportFragmentManager(), chartFragList);
        chartVp.setAdapter(chartVPAdapter);
//        ???Fragment?????????Acitivy??????
    }

    /* ???????????????????????????????????????*/
    private void initStatistics(int year, int month) {
        float inMoneyOneMonth = DBManager.getSumMoneyOneMonth(year, month, 1, username);  //???????????????
        float outMoneyOneMonth = DBManager.getSumMoneyOneMonth(year, month, 0, username); //???????????????
        int incountItemOneMonth = DBManager.getCountItemOneMonth(year, month, 1, username);  //???????????????
        int outcountItemOneMonth = DBManager.getCountItemOneMonth(year, month, 0, username); //???????????????
        dateTv.setText(year + "???" + month + "?????????");
        inTv.setText("???" + incountItemOneMonth + "?????????, ??? " + inMoneyOneMonth);
        outTv.setText("???" + outcountItemOneMonth + "?????????, ??? " + outMoneyOneMonth);

    }

    /**
     * ????????????????????????
     */
    private void initTime() {
        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH) + 1;
    }

    /**
     * ???????????????
     */
    private void initView(View root) {
        inBtn = root.findViewById(R.id.chart_btn_in);
        outBtn = root.findViewById(R.id.chart_btn_out);
        dateTv = root.findViewById(R.id.chart_tv_date);
        inTv = root.findViewById(R.id.chart_tv_in);
        outTv = root.findViewById(R.id.chart_tv_out);
        chartVp = root.findViewById(R.id.chart_vp);
        root.findViewById(R.id.chart_iv_back).setOnClickListener(this);
        root.findViewById(R.id.chart_iv_rili).setOnClickListener(this);
        root.findViewById(R.id.chart_btn_in).setOnClickListener(this);
        root.findViewById(R.id.chart_btn_out).setOnClickListener(this);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.chart_iv_back:
                getActivity().finish();
                break;
            case R.id.chart_iv_rili:
                showCalendarDialog();
                break;
            case R.id.chart_btn_in:
                setButtonStyle(1);
                chartVp.setCurrentItem(1);
                break;
            case R.id.chart_btn_out:
                setButtonStyle(0);
                chartVp.setCurrentItem(0);
                break;
        }
    }

    /* ?????????????????????*/
    private void showCalendarDialog() {
        CalendarDialog dialog = new CalendarDialog(getActivity(), selectPos, selectMonth);
        dialog.show();
        dialog.setDialogSize();
        dialog.setOnRefreshListener(new CalendarDialog.OnRefreshListener() {
            @Override
            public void onRefresh(int selPos, int year, int month) {
                MonthChartFragment.this.selectPos = selPos;
                MonthChartFragment.this.selectMonth = month;
                initStatistics(year, month);
                incomChartFragment.setDate(year, month);
                outcomChartFragment.setDate(year, month);
            }
        });
    }

    /* ???????????????????????????  ??????-0  ??????-1*/
    private void setButtonStyle(int kind) {
        if (kind == 0) {
            outBtn.setBackgroundResource(R.drawable.main_recordbtn_bg);
            outBtn.setTextColor(Color.WHITE);
            inBtn.setBackgroundResource(R.drawable.dialog_btn_bg);
            inBtn.setTextColor(Color.BLACK);
        } else if (kind == 1) {
            inBtn.setBackgroundResource(R.drawable.main_recordbtn_bg);
            inBtn.setTextColor(Color.WHITE);
            outBtn.setBackgroundResource(R.drawable.dialog_btn_bg);
            outBtn.setTextColor(Color.BLACK);
        }
    }
}
