package hayleykwan.myfirstandroidapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class RecyclerViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler_view);


        TextView hello = (TextView) findViewById(R.id.testing1);
        TextView bye = (TextView) findViewById(R.id.testing2);
        TextView sdl = (TextView) findViewById(R.id.testing3);

        ArrayList<TextView> list = new ArrayList<TextView>();
        list.add(hello);
        list.add(bye);
        list.add(sdl);

        for (TextView a : list){
            a.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Intent intent = new Intent(v.getContext(), MainActivity.class);
                    Intent intent = new Intent();
                    intent.putExtra("testing", v.toString());
                    System.out.println(intent + "from recycler with testing string" + v.toString());
//                    startActivity(intent);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            });
        }
    }
}
