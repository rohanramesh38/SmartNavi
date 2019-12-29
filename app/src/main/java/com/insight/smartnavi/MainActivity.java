package com.insight.smartnavi;


import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;


import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Intent i=getIntent();
        FragmentManager fm = getSupportFragmentManager();
        Fragment frag = fm.findFragmentById(R.id.fragment_container);
        if (frag == null) {
            frag = new CloudAnchorFragment();

            Bundle b=new Bundle();
            b.putString("Dest",i.getStringExtra("Dest"));
            b.putString("Lat",i.getStringExtra("Lat"));
            b.putString("Lng",i.getStringExtra("Lng"));
            frag.setArguments(b);
            fm.beginTransaction().add(R.id.fragment_container, frag).commit();
        }
    }

}
