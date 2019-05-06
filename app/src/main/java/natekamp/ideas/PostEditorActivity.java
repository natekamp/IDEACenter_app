package natekamp.ideas;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class PostEditorActivity extends AppCompatActivity
{
    //extras
    private String postKey, subjectName;
    private Boolean postTypeIsVideo, postIsEditable;

    //firebase
    private DatabaseReference postedVideosRef;
    private StorageReference postThumbnailRef, postAttachmentRef;
    private FirebaseAuth mAuth;
    private String currentUserID, postUserID,postTitle, postDescription, postThumbnail, postAttachment;

    //toolbar
    private Toolbar mToolbar;

    //views
    private Button updateButton, deleteButton;
    private EditText titleText, descriptionText;
    private ImageView attachmentImage;

    //other
    private boolean authorized;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_editor);

        //extras
        postKey = getIntent().getExtras().getString("EXTRA_POST_KEY", "placeholder_key");
        subjectName = getIntent().getExtras().getString("EXTRA_SUBJECT_NAME", "placeholder_name");
        postTypeIsVideo = getIntent().getExtras().getBoolean("EXTRA_IS_VIDEO", true);
        postIsEditable = getIntent().getExtras().getBoolean("EXTRA_IS_EDITABLE", false);

        //firebase
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        postedVideosRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(subjectName).child("Videos").child(postKey);

        //toolbar
        mToolbar = (Toolbar) findViewById(R.id.post_editor_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Edit Post");

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
                if (dataSnapshot.exists())
                {
                    postTitle = dataSnapshot.child("Title").getValue().toString();
                    postDescription = dataSnapshot.child("Description").getValue().toString();
                    postThumbnail = dataSnapshot.child("Thumbnail").getValue().toString();
                    postUserID = dataSnapshot.child("UID").getValue().toString();
                    postAttachment = dataSnapshot.child("Attachment").getValue().toString();
                    authorized = currentUserID.equals(postUserID);
                    postThumbnailRef = FirebaseStorage.getInstance().getReferenceFromUrl(postThumbnail);
                    postAttachmentRef = FirebaseStorage.getInstance().getReferenceFromUrl(postAttachment);

                    titleText.setText(postTitle);
                    descriptionText.setText(postDescription);
                    Picasso.get().load(postThumbnail).placeholder(R.drawable.placeholder_image).into(attachmentImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {/*do nothing*/}
        });

        if (!authorized || !postIsEditable)
        {
            disableEditText(titleText);
            disableEditText(descriptionText);
            updateButton.setVisibility(View.INVISIBLE);
            deleteButton.setVisibility(View.INVISIBLE);

            attachmentImage.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    sendToVideoActivity(postAttachment);
                }
            });
        }
        else
        {
            updateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateCurrentPost();
                }
            });

            deleteButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    deleteCurrentPost();
                }
            });

            attachmentImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO: Video selector function
                }
            });
        }
    }

    private void updateCurrentPost()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(PostEditorActivity.this);
        builder.setCancelable(true);
        builder.setTitle(R.string.post_editor_confirm_update_title);
        builder.setMessage(R.string.post_editor_confirm_alert_message);
        builder.setPositiveButton(R.string.post_editor_confirm_alert_positive,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        //postAttachmentRef.delete();
                        //postThumbnailRef.delete();
                        //TODO: upload new attachment
                        postedVideosRef.child("Title").setValue(titleText.getText().toString());
                        postedVideosRef.child("Description").setValue(descriptionText.getText().toString());
                        Toast.makeText(PostEditorActivity.this, PostEditorActivity.this.getString(R.string.post_editor_confirm_update_finished), Toast.LENGTH_SHORT).show();
                        PostEditorActivity.this.finish();
                    }
                });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {/*do nothing*/}
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteCurrentPost()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(PostEditorActivity.this);
        builder.setCancelable(true);
        builder.setTitle(R.string.post_editor_confirm_delete_title);
        builder.setMessage(R.string.post_editor_confirm_alert_message);
        builder.setPositiveButton(R.string.post_editor_confirm_alert_positive,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        postAttachmentRef.delete();
                        postThumbnailRef.delete();
                        postedVideosRef.removeValue();
                        Toast.makeText(PostEditorActivity.this, PostEditorActivity.this.getString(R.string.post_editor_confirm_delete_finished), Toast.LENGTH_SHORT).show();
                        PostEditorActivity.this.finish();
                    }
                });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {/*do nothing*/}
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if (id==android.R.id.home) finish();

        return super.onOptionsItemSelected(item);
    }

    private void disableEditText(EditText editText)
    {
        editText.setFocusable(false);
        editText.setEnabled(false);
        editText.setCursorVisible(false);
        editText.setKeyListener(null);
    }

    private void sendToVideoActivity(String videoURL)
    {
        Intent videoIntent = new Intent(PostEditorActivity.this, VideoActivity.class);
        videoIntent.putExtra("EXTRA_VIDEO_URL", videoURL);
        startActivity(videoIntent);
    }
}
