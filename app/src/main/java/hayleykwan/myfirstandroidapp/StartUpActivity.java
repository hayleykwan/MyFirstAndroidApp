package hayleykwan.myfirstandroidapp;

import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;


public class StartUpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);


        new CountDownTimer(5000, 1000) {

            public void onTick(long millisUntilFinished) {
                TextView tv = (TextView) findViewById(R.id.start_count_down);
                tv.setText("seconds remaining: " + millisUntilFinished / 1000);
                //animation showing logo
            }

            public void onFinish() {
                goToLogin();
                //if previously logged in
                //then directly to mainactivity
                //else loginactivity then from login to mainactivity
            }
        }.start();

    }

    private void goToLogin(){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}
