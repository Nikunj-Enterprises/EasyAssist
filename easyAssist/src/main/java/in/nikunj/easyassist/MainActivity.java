package in.nikunj.easyassist;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.java_websocket.WebSocket;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.reactivex.FlowableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.client.StompClient;
import in.nikunj.easyassist.model.ChatMessage;
import in.nikunj.easyassist.model.HelpRequested;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private SimpleAdapter mAdapter;
    private List<String> mDataSet = new ArrayList<>();
    private StompClient mStompClient;
    private StompClient mStompChatClient;

    private final SimpleDateFormat mTimeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
    private RecyclerView mRecyclerView;
    private Gson mGson = new GsonBuilder().create();

    //gets set through menu
    private static String HELP_SEEKER_ID = "kiosk1";
    //gets set through messaging
    private static String HELP_PROVIDER_ID;
    //gets set through menu
    private static String WS_SERVER_IP_PORT ="192.168.0.11:8080";

    private Animation animation;

    TextView helpCallingText;
    EditText chatEditText;
    Button chatMsgSendBtn;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main_actions, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_user:
                showAlert(MainActivity.this, "user");
                return true;
            case R.id.action_server:
                showAlert(MainActivity.this, "server");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void showAlert(Context activityContext, String type) {
        final EditText editText = new EditText(activityContext);
        final AlertDialog builder = new AlertDialog.Builder(activityContext,R.style.Theme_AppCompat_Dialog)
                .setPositiveButton("ok", null)
                .setNegativeButton("cancel", null)
                .create();

        builder.setView(editText);
        builder.setTitle("Enter :");
        builder.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                final Button btnAccept = builder.getButton(AlertDialog.BUTTON_POSITIVE);
                btnAccept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!editText.getText().toString().isEmpty()) {
                            if("user".equals(type)){
                                HELP_SEEKER_ID = editText.getText().toString();
                            }else if("server".equals(type)){
                                WS_SERVER_IP_PORT = editText.getText().toString();
                            }
                            Toast.makeText(activityContext, "Enter a user name", Toast.LENGTH_SHORT).show();
                            disconnectStomp(v);
                            connectStomp();
                        } else {
                            Log.d(TAG, "You have entered: " + editText.getText().toString());
                            builder.dismiss();
                        }
                    }
                });

                final Button btnDecline = builder.getButton(DialogInterface.BUTTON_NEGATIVE);
                btnDecline.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "Invitation declined");
                        builder.dismiss();
                    }
                });
            }
        });

        /* Show the dialog */
        builder.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mAdapter = new SimpleAdapter(mDataSet);
        mAdapter.setHasStableIds(true);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true));

        helpCallingText = (TextView) findViewById(R.id.txt_help);
        chatEditText = (EditText) findViewById(R.id.editText);
        chatMsgSendBtn = (Button) findViewById(R.id.btn_send);
        animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.bounce);
        connectStomp();
    }

    public void disconnectStomp(View view) {
        mStompClient.disconnect();
    }

    public void connectStomp() {
        mStompClient = Stomp.over(WebSocket.class, "ws://"+WS_SERVER_IP_PORT+"/assistance-websocket/websocket");

        mStompClient.lifecycle()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(lifecycleEvent -> {
                    switch (lifecycleEvent.getType()) {
                        case OPENED:
                            toast("Stomp connection opened");
                            break;
                        case ERROR:
                            Log.e(TAG, "Stomp connection error", lifecycleEvent.getException());
                            toast("Stomp connection error");
                            break;
                        case CLOSED:
                            toast("Stomp connection closed");
                    }
                });

        // Receive acked Messages
        mStompClient.topic("/topic/assistance")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(topicMessage -> {
                    Log.d(TAG, "Received " + topicMessage.getPayload());
                    toast("Received " + topicMessage.getPayload());
                    //addItem(mGson.fromJson(topicMessage.getPayload(), EchoModel.class));
                    try {
                        HelpRequested assistanceMsg = mGson.fromJson(topicMessage.getPayload(), HelpRequested.class);
                        if (assistanceMsg.getAcknowledgedBy() != null && assistanceMsg.getHelpSeekerId().equals(HELP_SEEKER_ID)) {
                            // acked msg received
                            openChatScreen(assistanceMsg.getAcknowledgedBy());
                        }
                    }catch (Exception e){
                        toast("Error Observed: "+e.getMessage());
                    }
                });

        mStompClient.topic("/topic/assistance/"+HELP_SEEKER_ID)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(topicMessage -> {
                    Log.d(TAG, "Received Chat Msg " + topicMessage.getPayload());
                    toast("Received Chat Msg " + topicMessage.getPayload());
                    //addItem(mGson.fromJson(topicMessage.getPayload(), EchoModel.class));
                    try {
                        ChatMessage chatMsg = mGson.fromJson(topicMessage.getPayload(), ChatMessage.class);
                        addChatMessage(chatMsg.getRepliedBy()+": "+chatMsg.getMessage());
                    }catch (Exception e){
                        toast("Error Observed Chat Msg : "+e.getMessage());
                    }
                });

        mStompClient.connect();
    }

    public void raiseHelpRequest(View view) {
        view.startAnimation(animation);
        helpCallingText.setVisibility(View.VISIBLE);

        HelpRequested requested = new HelpRequested();
        requested.setHelpSeekerId(HELP_SEEKER_ID);

        mStompClient.send("/assistance/requested/"+HELP_SEEKER_ID, mGson.toJson(requested))
                .compose(applySchedulers())
                .subscribe(aVoid -> {
                    Log.d(TAG, "STOMP echo send successfully");
                }, throwable -> {
                    Log.e(TAG, "Error send STOMP echo", throwable);
                    toast(throwable.getMessage());
                });
    }

    public void sendStompChatMsg(View view){
        EditText editText = (EditText)findViewById(R.id.editText);
        String message = editText.getText().toString();
        editText.setText("");

        mStompClient.send("/assistance/"+HELP_SEEKER_ID+"/chat/"+HELP_PROVIDER_ID, message)
                .compose(applySchedulers())
                .subscribe(aVoid -> {
                    Log.d(TAG, "STOMP chat send successfully");
                }, throwable -> {
                    Log.e(TAG, "Error send STOMP chat", throwable);
                    toast(throwable.getMessage());
                });
        addChatMessage(HELP_SEEKER_ID+": "+message);
    }

    public void closeChatSession(View view){
        mDataSet.clear();
        mAdapter.notifyDataSetChanged();

        findViewById(R.id.btn_help).setVisibility(View.VISIBLE);
        findViewById(R.id.btn_help).clearAnimation();
        findViewById(R.id.btn_end).setVisibility(View.GONE);
        findViewById(R.id.editText).setVisibility(View.GONE);
        findViewById(R.id.btn_send).setVisibility(View.GONE);
    }

    private void toast(String text) {
        Log.i(TAG, text);
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    private void openChatScreen(String acknowledgedBy) {
        helpCallingText.setVisibility(View.GONE);
        findViewById(R.id.btn_help).setVisibility(View.GONE);
        findViewById(R.id.btn_end).setVisibility(View.VISIBLE);
        findViewById(R.id.editText).setVisibility(View.VISIBLE);
        findViewById(R.id.btn_send).setVisibility(View.VISIBLE);

        HELP_PROVIDER_ID = acknowledgedBy;

        addChatMessage(HELP_PROVIDER_ID+": How can I help you?");
    }

    private void addChatMessage(String msg){
        mDataSet.add(msg);
        mAdapter.notifyDataSetChanged();
        mRecyclerView.smoothScrollToPosition(mDataSet.size() - 1);
    }

    protected <T> FlowableTransformer<T, T> applySchedulers() {
        return tFlowable -> tFlowable
                .unsubscribeOn(Schedulers.newThread())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    protected void onDestroy() {
        mStompClient.disconnect();
        //mStompChatClient.disconnect();
        super.onDestroy();
    }
}
