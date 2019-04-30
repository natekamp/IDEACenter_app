package natekamp.ideas;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class PostEditorActivity extends AppCompatActivity
{
    //extras
    private String postKey;

    //views
    private Button updateButton, deleteButton;
    private EditText titleText, descriptionText;
    private ImageButton attachmentButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_editor);

        //extras


        //views
        updateButton = (Button) findViewById(R.id.post_editor_update);
        deleteButton = (Button) findViewById(R.id.post_editor_delete);
        titleText = (EditText) findViewById(R.id.post_editor_title);
        descriptionText = (EditText) findViewById(R.id.post_editor_description);
        attachmentButton = (ImageButton) findViewById(R.id.post_editor_attachment);
    }
}
