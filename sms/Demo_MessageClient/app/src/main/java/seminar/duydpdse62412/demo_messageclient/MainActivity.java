package seminar.duydpdse62412.demo_messageclient;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void clickToSendSMSIntent(View view) {
        Uri uri = Uri.parse("smsto:5556");
        Intent it = new Intent(Intent.ACTION_SENDTO, uri);
        it.putExtra("sms_body", "Send SMS using Intent - Hello!!!");
        startActivity(it);
    }

    public void clickToSend(View view) {
        EditText phone = (EditText) findViewById(R.id.edtPhone);
        EditText content = (EditText) findViewById(R.id.edtContent);
        final SmsManager sms = SmsManager.getDefault();
        Intent intent = new Intent("ACTION_MSG_SENT");
        final PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int result = getResultCode();
                String msg = "Sent!";
                if (result != Activity.RESULT_OK) {
                    msg = "Failed!";
                }
                Toast.makeText(getBaseContext(), msg, Toast.LENGTH_LONG).show();
            }
        }, new IntentFilter("ACTION_MSG_SENT"));
        sms.sendTextMessage(phone.getText().toString(), null, content.getText().toString(), pendingIntent, null);
        finish();
    }
}
