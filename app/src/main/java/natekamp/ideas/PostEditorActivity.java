package natekamp.ideas;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class PostEditorActivity extends AppCompatActivity
{//TODO: Merge common functions (with post activity) into separate class? And also add event posting.
    private final static int Gallery_Media = 1;

    //extras
    private String postKey, subjectName;
    private Boolean postTypeIsVideo, postIsEditable;

    //firebase
    private DatabaseReference postedVideosRef;
    private StorageReference postThumbnailRef, postAttachmentRef, allPostAttachmentsRef;
    private FirebaseAuth mAuth;
    private String currentUserID;

    //post info
    private Uri attachmentUri;
    private Bitmap thumbnailBitmap;
    private String postUserID, postTitle, postDescription, postThumbnail, postAttachment, attachmentURL, thumbnailURL, currentDateNum, currentTime;
    boolean thumbnailUploaded;

    //toolbar
    private Toolbar mToolbar;

    //views
    private Button updateButton, deleteButton;
    private EditText titleText, descriptionText;
    private ImageView attachmentImage;

    //other
    private boolean authorized;

    //progress dialog
    private ProgressDialog loadingBar;

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
                    getVideo();
                }
            });
        }
    }

    public void getVideo()
    {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("video/*");
        startActivityForResult(galleryIntent, Gallery_Media);
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
                        loadingBar.setTitle(PostEditorActivity.this.getString(R.string.progress_title));
                        loadingBar.setMessage(
                                PostEditorActivity.this.getString(postTypeIsVideo ?
                                        R.string.progress_update_video_msg :
                                        R.string.progress_update_event_msg
                                ));
                        loadingBar.show();
                        loadingBar.setCanceledOnTouchOutside(true);

                        postAttachmentRef.delete();
                        postThumbnailRef.delete();
                        uploadAttachment();
                        postedVideosRef.child("Thumbnail").setValue(thumbnailURL);
                        postedVideosRef.child("Attachment").setValue(attachmentURL);
                        postedVideosRef.child("Title").setValue(titleText.getText().toString());
                        postedVideosRef.child("Description").setValue(descriptionText.getText().toString());
                        loadingBar.dismiss();
                        Toast.makeText(PostEditorActivity.this, PostEditorActivity.this.getString(R.string.post_editor_confirm_update_finished), Toast.LENGTH_SHORT).show();
                        finish();
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
                        finish();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode==RESULT_OK && data!=null)
        {
            if (requestCode==Gallery_Media)
            {
                attachmentUri = data.getData();

                //create thumbnail of video
                MediaMetadataRetriever mMMR = new MediaMetadataRetriever();
                mMMR.setDataSource(this, attachmentUri);
                thumbnailBitmap = mMMR.getFrameAtTime();

                attachmentImage.setImageBitmap(thumbnailBitmap);
            }
        }
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

    private void uploadAttachment()
    {
        Calendar cal = Calendar.getInstance();
        currentDateNum = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
        currentTime = new SimpleDateFormat("HH:mm:ss").format(cal.getTime());

        StorageReference attachmentPath = allPostAttachmentsRef.child("post_attachments").child(currentUserID).child(
                currentDateNum+"_"+currentTime+"_attached"+(postTypeIsVideo ? "Video.mp4" : "Image.png")
        );

        if (postTypeIsVideo)
        {
            StorageReference thumbnailPath = allPostAttachmentsRef.child("post_thumbnails").child(currentUserID).child(
                    currentDateNum+"_"+currentTime+"_videoThumbnail.png"
            );

            //convert thumbnail bitmap to byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            thumbnailBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] thumbnailBytes = baos.toByteArray();

            //upload thumbnail to database
            thumbnailPath.putBytes(thumbnailBytes).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    thumbnailUploaded = task.isSuccessful();
                    if (thumbnailUploaded)
                    {
                        Task<Uri> result = task.getResult().getMetadata().getReference().getDownloadUrl();
                        result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                thumbnailURL = uri.toString();
                            }
                        });
                    }
                    else Toast.makeText(PostEditorActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (thumbnailUploaded || !postTypeIsVideo)
        {
            //upload attachment to database
            attachmentPath.putFile(attachmentUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        Task<Uri> result = task.getResult().getMetadata().getReference().getDownloadUrl();
                        result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                attachmentURL = uri.toString();
                            }
                        });
                    } else
                        Toast.makeText(PostEditorActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
