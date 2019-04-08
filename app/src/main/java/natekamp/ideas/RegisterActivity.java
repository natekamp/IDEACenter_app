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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity
{
    private FirebaseAuth mAuth;

    private EditText userEmail, userPassword, userConfirm;
    private Button createButton;

    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

    //firebase authentication
        mAuth = FirebaseAuth.getInstance();
    //Buttons and EditTexts
        userEmail = (EditText) findViewById(R.id.register_email);
        userPassword = (EditText) findViewById(R.id.register_password);
        userConfirm = (EditText) findViewById(R.id.register_confirm);
        createButton = (Button) findViewById(R.id.register_create);

        loadingBar = new ProgressDialog(this);


        createButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                createNewAccount();
            }
        });
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) sendToMainActivity();
    }

    private void createNewAccount()
    {
        String email = userEmail.getText().toString();
        String password = userPassword.getText().toString();
        String confirm = userConfirm.getText().toString();

        if (!email.contains("@thompsonschools.org"))
            Toast.makeText(this, this.getString(R.string.error_invalid_email_domain), Toast.LENGTH_SHORT).show();
        else if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirm))
            Toast.makeText(this, this.getString(R.string.error_field_msg), Toast.LENGTH_SHORT).show();
        else if (!password.equals(confirm))
            Toast.makeText(this, this.getString(R.string.error_password_msg), Toast.LENGTH_SHORT).show();
        else
        {
            loadingBar.setTitle(RegisterActivity.this.getString(R.string.progress_title));
            loadingBar.setMessage(RegisterActivity.this.getString(R.string.progress_register_msg));
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            //TODO: Add email verification

                            String resultMsg = task.isSuccessful() ?
                                    RegisterActivity.this.getString(R.string.success_auth_msg) :
                                    "Error: " + task.getException().getMessage();

                            loadingBar.dismiss();
                            Toast.makeText(RegisterActivity.this, resultMsg, Toast.LENGTH_SHORT).show();

                            if (task.isSuccessful()) sendToSetupActivity();
                        }
                    });
        }
    }

    private void sendToSetupActivity()
    {
        Intent setupIntent = new Intent(RegisterActivity.this, SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }

    private void sendToMainActivity()
    {
        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
