package com.example.lxysss.smartboy.activity;


import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.lxysss.smartboy.R;
import com.example.lxysss.smartboy.adapter.ChatAdapter;
import com.example.lxysss.smartboy.entity.Chat;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;
import com.kymjs.rxvolley.RxVolley;
import com.kymjs.rxvolley.client.HttpCallback;

import org.json.JSONException;
import org.json.JSONObject;


import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private ListView mChatListView;

    private List<Chat> mList = new ArrayList<>();
    private ChatAdapter adapter;

    //TTS
    private SpeechSynthesizer mTts;
    //输入框
    private EditText et_text;
    //发送按钮
    private Button btn_send;

    @Override
    public void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SpeechUtility.createUtility(getApplicationContext(), SpeechConstant.APPID +"=5acaf213");
        findView();
    }

    //初始化View
    private void findView() {

        //1.创建SpeechSynthesizer对象, 第二个参数：本地合成时传InitListener
        mTts = SpeechSynthesizer.createSynthesizer(this, null);
        //2.合成参数设置，详见《科大讯飞MSC API手册(Android)》SpeechSynthesizer 类
        mTts.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan");//设置发音人
        mTts.setParameter(SpeechConstant.SPEED, "50");//设置语速
        mTts.setParameter(SpeechConstant.VOLUME, "80");//设置音量，范围0~100
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD); //设置云端


        mChatListView = (ListView) findViewById(R.id.mChatListView);
        et_text = (EditText) findViewById(R.id.et_text);
        btn_send = (Button) findViewById(R.id.btn_send);
        btn_send.setOnClickListener(this);

        //设置适配器
        adapter = new ChatAdapter(this, mList);
        mChatListView.setAdapter(adapter);

        addLeftItem("你好，我是小鲶！");
    }

    //点击事件
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_send:
                //获取输入框的内容
                String text = et_text.getText().toString();

                // 关闭软键盘
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm.isActive())
                {
                    imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT,
                            InputMethodManager.HIDE_NOT_ALWAYS);
                }

                //判断是否为空
                if (!TextUtils.isEmpty(text)) {
                        //清空当前的输入框
                        et_text.setText("");
                        addRightItem(text);

                        try{
                            text= URLEncoder.encode(text,"UTF-8");
                        }catch (UnsupportedEncodingException e){
                            e.printStackTrace();
                        }

                        String url = "http://apis.haoservice.com/efficient/robot?info=" + text
                            + "&address=&key=" + "be9b8d07937b4ca19f08fff44f5b031a";
                       RxVolley.get(url, new HttpCallback() {
                            @Override
                            public void onSuccess(String t) {
                                Log.i("MainActivity", "Json" + t);
                                parsingJson(t);
                            }
                        });

                    } else {
                    Toast.makeText(this, "输入不能为空！", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    //解析Json
    private void parsingJson(String t) {
        try {
            JSONObject jsonObhect = new JSONObject(t);
            JSONObject jsonresult = jsonObhect.getJSONObject("result");
            //拿到返回值
            String text = jsonresult.getString("text");
            //7.拿到机器人的返回值之后添加在left item
            addLeftItem(text);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



        //添加左边文本
    private void addLeftItem(String text) {

     //   boolean isSpeak = ShareUtils.getBoolean(getActivity(), "isSpeak", false);
     //   if (isSpeak) {
            startSpeak(text);
     //   }

        Chat date = new Chat();
        date.setType(ChatAdapter.VALUE_LEFT_TEXT);
        date.setText(text);
        mList.add(date);
        //通知adapter刷新
        adapter.notifyDataSetChanged();
        //滚动到底部
        mChatListView.setSelection(mChatListView.getBottom());
    }

    //添加右边文本
    private void addRightItem(String text) {

        Chat date = new Chat();

        date.setType(ChatAdapter.VALUE_RIGHT_TEXT);
        date.setText(text);
        mList.add(date);
        //通知adapter刷新
        adapter.notifyDataSetChanged();
        //滚动到底部
        mChatListView.setSelection(mChatListView.getBottom());
    }

    //开始说话
    private void startSpeak(String text) {
        //3.开始合成
        mTts.startSpeaking(text, mSynListener);
    }

    //合成监听器
    private SynthesizerListener mSynListener = new SynthesizerListener() {
        //会话结束回调接口，没有错误时，error为null
        public void onCompleted(SpeechError error) {
        }

        //缓冲进度回调
        //percent为缓冲进度0~100，beginPos为缓冲音频在文本中开始位置，endPos表示缓冲音频在文本中结束位置，info为附加信息。
        public void onBufferProgress(int percent, int beginPos, int endPos, String info) {
        }

        //开始播放
        public void onSpeakBegin() {
        }

        //暂停播放
        public void onSpeakPaused() {
        }

        //播放进度回调
        //percent为播放进度0~100,beginPos为播放音频在文本中开始位置，endPos表示播放音频在文本中结束位置.
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
        }

        //恢复播放回调接口
        public void onSpeakResumed() {
        }

        //会话事件回调接口
        public void onEvent(int arg0, int arg1, int arg2, Bundle arg3) {
        }
    };
}
