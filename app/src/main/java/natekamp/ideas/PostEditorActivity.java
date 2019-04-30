package natekamp.ideas;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class PostEditorActivity extends AppCompatActivity
{
    //extras
    private String postKey, subjectName;

    //firebase
    private DatabaseReference postedVideosRef;

    //views
    private Button updateButton, deleteButton;
    private EditText titleText, descriptionText;
    private ImageView attachmentImage;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_editor);

        //extras
        postKey = getIntent().getExtras().getString("EXTRA_POST_KEY", "placeholder_key");
        subjectName = getIntent().getExtras().getString("EXTRA_SUBJECT_NAME", "placeholder_name");

        //firebase
        postedVideosRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(subjectName).child("Videos").child(postKey);

        //views
        updateButton = (Button) findViewById(R.id.post_editor_update);
        deleteButton = (Button) findViewById(R.id.post_editor_delete);
        titleText = (EditText) findViewById(R.id.post_editor_title);
        descriptionText = (EditText) findViewById(R.id.post_editor_description);
        attachmentImage = (ImageView) findViewById(R.id.post_editor_attachment);


        //load post info from database into editor
        postedVideosRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                String title = dataSnapshot.child("Title").getValue().toString();
                String description = dataSnapshot.child("Description").getValue().toString();
                String thumbnail = dataSnapshot.child("Thumbnail").getValue().toString();
                //String attachment = dataSnapshot.child("Attachment").getValue().toString();

                titleText.setText(title);
                descriptionText.setText(description);
                Picasso.get().load(thumbnail).placeholder(R.drawable.placeholder_image).into(attachmentImage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {/*do nothing*/}
        });
    }
}
