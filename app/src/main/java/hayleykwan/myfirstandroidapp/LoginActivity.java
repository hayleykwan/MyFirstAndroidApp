package hayleykwan.myfirstandroidapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

public class LoginActivity extends AppCompatActivity {

    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fbLoginSetup();
        setContentView(R.layout.activity_login);
        //go to main activity

    }

    private void fbLoginSetup(){
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        callbackManager = CallbackManager.Factory.create();
//        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
//        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
//            @Override
//            public void onSuccess(LoginResult loginResult) {
//                Log.d("Success", loginResult.getAccessToken().toString());
//            }
//
//            @Override
//            public void onCancel() {
//                Toast.makeText(LoginActivity.this, "Login Cancel", Toast.LENGTH_LONG).show();
//            }
//
//            @Override
//            public void onError(FacebookException error) {
//                Toast.makeText(LoginActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
//            }
//        });
//        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile"));
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
