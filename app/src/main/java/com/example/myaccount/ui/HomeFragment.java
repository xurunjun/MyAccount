package com.example.myaccount.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

import com.example.myaccount.MonthChartActivity;
import com.example.myaccount.R;
import com.example.myaccount.RecordActivity;
import com.example.myaccount.SearchActivity;
import com.example.myaccount.adapter.AccountAdapter;
import com.example.myaccount.db.DBManager;
import com.example.myaccount.model.AccountBean;
import com.example.myaccount.utils.AnalysisUtils;
import com.example.myaccount.utils.BudgetDialog;
import com.example.myaccount.utils.MoreDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class HomeFragment extends Fragment implements View.OnClickListener{
    ListView todayLv;  //展示今日收支情况的ListView
    Button editBtn;
    ImageButton moreBtn;
    //声明数据源
    List<AccountBean> mDatas;
    AccountAdapter adapter;
    int year,month,day;
    //头布局相关控件
    View headerView;
    TextView topOutTv,topInTv,topbudgetTv,topConTv;
    ImageView topShowIv;
    String username;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        initTime();
        initView(root);
        //添加ListView的头布局
        addLVHeaderView();
        mDatas = new ArrayList<>();
        //设置适配器：加载每一行数据到列表当中
        adapter = new AccountAdapter(getContext(), mDatas);
        todayLv.setAdapter(adapter);
        Context context=getActivity().getApplicationContext();
        username= AnalysisUtils.readLoginUserName(context);
        return root;
    }


    /** 初始化自带的View的方法*/
    private void initView(View root) {
        todayLv = root.findViewById(R.id.main_lv);
        editBtn = root.findViewById(R.id.main_btn_edit);
        moreBtn = root.findViewById(R.id.main_btn_more);
        editBtn.setOnClickListener(this);
        moreBtn.setOnClickListener(this);
        setLVLongClickListener();
    }
    /** 设置ListView的长按事件*/
    private void setLVLongClickListener() {
        todayLv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {  //点击了头布局
                    return false;
                }
                int pos = position-1;
                AccountBean clickBean = mDatas.get(pos);  //获取正在被点击的这条信息

                //弹出提示用户是否删除的对话框
                showDeleteItemDialog(clickBean);
                return false;
            }
        });
    }
    /* 弹出是否删除某一条记录的对话框*/
    private void showDeleteItemDialog(final  AccountBean clickBean) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("提示信息").setMessage("您确定要删除这条记录么？")
                .setNegativeButton("取消",null)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int click_id = clickBean.getId();
                        float money= DBManager.getItemFromAccounttbById(click_id);
                        float budge_money=DBManager.getBudgettbMoney(username);
                        DBManager.updateBudgettb(money+budge_money,username);
                        //执行删除的操作
                        DBManager.deleteItemFromAccounttbById(click_id);
                        mDatas.remove(clickBean);   //实时刷新，移除集合当中的对象
                        adapter.notifyDataSetChanged();   //提示适配器更新数据
                        setTopTvShow();   //改变头布局TextView显示的内容
                    }
                });
        builder.create().show();   //显示对话框
    }

    /** 给ListView添加头布局的方法*/
    private void addLVHeaderView() {
        //将布局转换成View对象
        headerView = getLayoutInflater().inflate(R.layout.item_mainlv_top, null);
        todayLv.addHeaderView(headerView);
        //查找头布局可用控件
        topOutTv = headerView.findViewById(R.id.item_mainlv_top_tv_out);
        topInTv = headerView.findViewById(R.id.item_mainlv_top_tv_in);
        topbudgetTv = headerView.findViewById(R.id.item_mainlv_top_tv_budget);
        topConTv = headerView.findViewById(R.id.item_mainlv_top_tv_day);
        topShowIv = headerView.findViewById(R.id.item_mainlv_top_iv_hide);

        topbudgetTv.setOnClickListener(this);
        headerView.setOnClickListener(this);
        topShowIv.setOnClickListener(this);

    }
    /* 获取今日的具体时间*/
    private void initTime() {
        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH)+1;
        day = calendar.get(Calendar.DAY_OF_MONTH);
    }

    // 当activity获取焦点时，会调用的方法
    @Override
    public void onResume() {
        super.onResume();
        loadDBData();
        setTopTvShow();
    }
    /* 设置头布局当中文本内容的显示*/
    private void setTopTvShow() {
        //获取今日支出和收入总金额，显示在view当中
        float incomeOneDay = DBManager.getSumMoneyOneDay(year, month, day, 1,username);
        float outcomeOneDay = DBManager.getSumMoneyOneDay(year, month, day, 0,username);
        String infoOneDay = "今日支出 ￥"+outcomeOneDay+"  收入 ￥"+incomeOneDay;
        topConTv.setText(infoOneDay);
//        获取本月收入和支出总金额
        float incomeOneMonth = DBManager.getSumMoneyOneMonth(year, month, 1,username);
        float outcomeOneMonth = DBManager.getSumMoneyOneMonth(year, month, 0,username);
        topInTv.setText("￥"+incomeOneMonth);
        topOutTv.setText("￥"+outcomeOneMonth);

//    设置显示运算剩余
        float bmoney=DBManager.getBudgettbMoney(username);
        if (bmoney == 0) {
            topbudgetTv.setText("￥ 0");
        }else{
            float syMoney = bmoney-outcomeOneMonth;
            topbudgetTv.setText("￥"+syMoney);
        }
    }

    // 加载数据库数据
    private void loadDBData() {
        List<AccountBean> list = DBManager.getAccountListOneDayFromAccounttb(year, month, day,username);
        mDatas.clear();
        mDatas.addAll(list);
        adapter.notifyDataSetChanged();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_btn_edit:
                Intent it1 = new Intent(getContext(), RecordActivity.class);  //跳转界面
                startActivity(it1);
                break;
            case R.id.main_btn_more:
                MoreDialog moreDialog = new MoreDialog(getActivity());
                moreDialog.show();
                moreDialog.setDialogSize();
                break;
            case R.id.item_mainlv_top_tv_budget:
                showBudgetDialog();
                break;
            case R.id.item_mainlv_top_iv_hide:
                // 切换TextView明文和密文
                toggleShow();
                break;
        }
        if (v == headerView) {
            //头布局被点击了
            Intent intent = new Intent();
            intent.setClass(getContext(), MonthChartActivity.class);
            startActivity(intent);
        }
    }
    /** 显示运算设置对话框*/
    private void showBudgetDialog() {
        BudgetDialog dialog = new BudgetDialog(getActivity());
        dialog.show();
        dialog.setDialogSize();
        dialog.setOnEnsureListener(new BudgetDialog.OnEnsureListener() {
            @Override
            public void onEnsure(float money) {
                //计算剩余金额
                float outcomeOneMonth = DBManager.getSumMoneyOneMonth(year, month, 0,username);
                float syMoney = money-outcomeOneMonth;//预算剩余 = 预算-支出
                DBManager.updateBudgettb(syMoney,username);
                topbudgetTv.setText("￥"+syMoney);
            }
        });
    }

    boolean isShow = true;
    /**
     * 点击头布局眼睛时，如果原来是明文，就加密，如果是密文，就显示出来
     * */
    private void toggleShow() {
        if (isShow) {   //明文====》密文
            PasswordTransformationMethod passwordMethod = PasswordTransformationMethod.getInstance();
            topInTv.setTransformationMethod(passwordMethod);   //设置隐藏
            topOutTv.setTransformationMethod(passwordMethod);   //设置隐藏
            topbudgetTv.setTransformationMethod(passwordMethod);   //设置隐藏
            topShowIv.setImageResource(R.mipmap.ih_hide);
            isShow = false;   //设置标志位为隐藏状态
        }else{  //密文---》明文
            HideReturnsTransformationMethod hideMethod = HideReturnsTransformationMethod.getInstance();
            topInTv.setTransformationMethod(hideMethod);   //设置隐藏
            topOutTv.setTransformationMethod(hideMethod);   //设置隐藏
            topbudgetTv.setTransformationMethod(hideMethod);   //设置隐藏
            topShowIv.setImageResource(R.mipmap.ih_show);
            isShow = true;   //设置标志位为隐藏状态
        }
    }
}