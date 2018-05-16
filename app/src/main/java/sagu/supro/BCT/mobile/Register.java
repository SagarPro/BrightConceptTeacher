package sagu.supro.BCT.mobile;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import sagu.supro.BCT.R;

public class Register extends Activity {

    EditText userName,userEmail,userPass;
    ImageView addDevice,minusDevice;
    TextView numberOfDevices;
    Button submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        userName = findViewById(R.id.et_name);
        userEmail = findViewById(R.id.et_email);
        userPass = findViewById(R.id.et_password);
        addDevice = findViewById(R.id.img_add);
        minusDevice = findViewById(R.id.img_minus);
        numberOfDevices = findViewById(R.id.tv_dev_added);
        submit = findViewById(R.id.b_submit);


        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }
}