package natekamp.ideas;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity
{

    private EditText userName, userGrade;
    private Button saveButton;
    private CircleImageView profileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        userName = (EditText) findViewById(R.id.setup_name);
        userGrade = (EditText) findViewById(R.id.setup_grade);
        saveButton = (Button) findViewById(R.id.setup_save);
        profileImage = (CircleImageView) findViewById(R.id.setup_icon);
    }
}
