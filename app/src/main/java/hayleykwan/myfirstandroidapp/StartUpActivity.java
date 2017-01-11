package hayleykwan.myfirstandroidapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class StartUpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);

        //animation showing logo

        //if previously logged in
        //then directly to mainactivity
        //else loginactivity then from login to mainactivity
    }
}
