package natekamp.ideas;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity
{
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private EditText userName, userGrade;
    private Button saveButton;
    private CircleImageView profileImage;
    String currentUserID;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        mAuth = FirebaseAuth.getInstance();
            currentUserID = mAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        userName = (EditText) findViewById(R.id.setup_name);
        userGrade = (EditText) findViewById(R.id.setup_grade);
        saveButton = (Button) findViewById(R.id.setup_save);
        profileImage = (CircleImageView) findViewById(R.id.setup_icon);
        loadingBar = new ProgressDialog(this);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                saveSetupInfo();
            }
        });
    }

    private void saveSetupInfo()
    {
        String username = userName.getText().toString();
        String grade = userGrade.getText().toString();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(grade))
            Toast.makeText(this, this.getString(R.string.error_field_msg), Toast.LENGTH_SHORT).show();
        else
        {
            loadingBar.setTitle(SetupActivity.this.getString(R.string.progress_title));
            loadingBar.setMessage(SetupActivity.this.getString(R.string.progress_setup_msg));
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            HashMap userMap = new HashMap();
                userMap.put("Username", username);
                userMap.put("Grade", grade);
                userMap.put("user data 3", "put data here");
            usersRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task)
                {
                    String resultMsg = SetupActivity.this.getString(R.string.success_setup_msg);
                    if (!task.isSuccessful()) resultMsg = "Error: " + task.getException().getMessage();

                    loadingBar.dismiss();
                    Toast.makeText(SetupActivity.this, resultMsg, Toast.LENGTH_SHORT).show();

                    if (task.isSuccessful()) sendToMainActivity();
                }
            });
        }
    }

    private void sendToMainActivity()
    {
        Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
