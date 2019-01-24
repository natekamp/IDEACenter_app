package natekamp.ideas;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends AppCompatActivity
{
    private Button loginButton, registerButton;
    private EditText userEmail, userPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginButton = (Button) findViewById(R.id.aLogin_login);
        registerButton = (Button) findViewById(R.id.aLogin_register);
        userEmail = (EditText) findViewById(R.id.aLogin_email);
        userPassword = (EditText) findViewById(R.id.aLogin_password);

        registerButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendToRegisterActivity();
            }
        });
    }

    private void sendToRegisterActivity()
    {
        Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(registerIntent);
        //removing finish() allows the back button to go to the login activity instead of exiting the app
    }
}
