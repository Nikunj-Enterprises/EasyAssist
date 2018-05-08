package in.nikunj.easyassist;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Nikunj on 5/6/2018.
 */
public class LoginActivity  extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        EditText mUserName = (EditText) findViewById(R.id.name);
        //mUserName.setVisibility(View.GONE);

        EditText mPasswordView = (EditText) findViewById(R.id.password);

        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });
    }

    private void attemptLogin() {
        EditText mUserName = (EditText) findViewById(R.id.name);
        EditText mPasswordView = (EditText) findViewById(R.id.password);

        if(mUserName.getText().toString() == null || mUserName.getText().toString().isEmpty()){
            Toast.makeText(LoginActivity.this,"user name is not valid ", Toast.LENGTH_SHORT).show();
            return;
        }

        if(mPasswordView.getText().toString() == null || !mPasswordView.getText().toString().equals("password")){
            Toast.makeText(LoginActivity.this,"Login Failed", Toast.LENGTH_SHORT).show();
            return;
        }

        MainActivity.setHelpSeekerId(mUserName.getText().toString());
        startActivity(new Intent(this, MainActivity.class));

        finish();
    }

    public void SignIn(View v){
        attemptLogin();
    }
}