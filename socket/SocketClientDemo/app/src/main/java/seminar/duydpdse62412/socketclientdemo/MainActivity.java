package seminar.duydpdse62412.socketclientdemo;

import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    EditText et;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        et = (EditText)findViewById(R.id.edtText);
    }

    public void clickToSend(View view) {
        MessageSender messageSender =new MessageSender();
        messageSender.execute(et.getText().toString());
        et.setText("");
    }
}
