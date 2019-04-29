package natekamp.ideas;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class PostActivity extends AppCompatActivity
{
    private final static int Gallery_Media = 1;

    //extras
    boolean postTypeIsVideo;
    String subjectName;

    //firebase
    private FirebaseAuth mAuth;
    String currentUserID;
    private StorageReference postAttachmentsRef;
    private DatabaseReference usersRef, postedVideosRef, postedEventsRef;

    //toolbar
    private Toolbar mToolbar;

    //views
    private Button finishButton;
    private ImageButton attachmentButton;
    private EditText titleText, descriptionText;

    //post info
    private Uri attachmentUri;
    private Bitmap thumbnailBitmap;
    private String postTitle, postDescription;
    String attachmentURL, thumbnailURL, currentDate, currentTime, postName, currentTimestamp;

    //progress dialog
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        //extras
        postTypeIsVideo = getIntent().getExtras().getBoolean("EXTRA_IS_VIDEO", true);
        subjectName = getIntent().getExtras().getString("EXTRA_SUBJECT_NAME", "placeholder_name");

        //firebase
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        postAttachmentsRef = FirebaseStorage.getInstance().getReference();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        postedVideosRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(subjectName).child("Videos");
        postedEventsRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(subjectName).child("Events");

        //toolbar
        mToolbar = (Toolbar) findViewById(R.id.post_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(postTypeIsVideo ? R.string.post_video_toolbar_title : R.string.post_event_toolbar_title);

        //views
        attachmentButton = (ImageButton) findViewById(R.id.post_attachment);
        finishButton = (Button) findViewById(R.id.post_finish);
        titleText = (EditText) findViewById(R.id.post_title);
        descriptionText = (EditText) findViewById(R.id.post_description);

        //progress dialog
        loadingBar = new ProgressDialog(this);


        attachmentButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (postTypeIsVideo) getVideo();
                else getImage();
            }
        });

        finishButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                validatePost();
            }
        });

    }

    public void getVideo()
    {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("video/*");
        startActivityForResult(galleryIntent, Gallery_Media);
    }
    public void getImage()
    {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, Gallery_Media);
    }

    private void validatePost()
    {
        Calendar cal = Calendar.getInstance();
        currentTimestamp = new SimpleDateFormat("'Posted on' MMMM dd, yyyy 'at' h:mm a").format(cal.getTime());
        currentDate = new SimpleDateFormat("yyyy-MMMM-dd").format(cal.getTime());
        currentTime = new SimpleDateFormat("HH:mm:ss").format(cal.getTime());
        postTitle = titleText.getText().toString();
        postDescription = descriptionText.getText().toString();

        if (TextUtils.isEmpty(postTitle))
            Toast.makeText(this, R.string.error_missing_title, Toast.LENGTH_SHORT).show();
        else if (attachmentUri==null && postTypeIsVideo)
            Toast.makeText(this, R.string.error_missing_vid, Toast.LENGTH_SHORT).show();
        else
        {
            loadingBar.setTitle(PostActivity.this.getString(R.string.progress_title));
            loadingBar.setMessage(
                    PostActivity.this.getString(postTypeIsVideo ?
                    R.string.progress_post_video_msg :
                    R.string.progress_post_event_msg
            ));
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            uploadAttachment(!postTypeIsVideo && attachmentUri==null);
        }
    }

    private void uploadAttachment(boolean postingEventWithoutAttachment)
    {
        postName = currentUserID+"_"+currentDate+"_"+currentTime;

        if (postingEventWithoutAttachment)
        {
            attachmentURL = "NO_ATTACHMENT";
            savePostInfo();
        }
        else
        {
            StorageReference attachmentPath = postAttachmentsRef.child("post_attachments").child(currentUserID).child(
                    currentDate+"_"+currentTime+"_attached"+(postTypeIsVideo ? "Video.mp4" : "Image.png")
            );

            if (postTypeIsVideo)
            {
                StorageReference thumbnailPath = postAttachmentsRef.child("post_thumbnails").child(currentUserID).child(
                        currentDate+"_"+currentTime+"_videoThumbnail.png"
                );

                //convert thumbnail bitmap to byte array
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumbnailBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] thumbnailBytes = baos.toByteArray();

                //upload thumbnail to database
                thumbnailPath.putBytes(thumbnailBytes).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful())
                        {
                            Task<Uri> result = task.getResult().getMetadata().getReference().getDownloadUrl();
                            result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    thumbnailURL = uri.toString();
                                }
                            });
                        }
                        else Toast.makeText(PostActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            //upload attachment to database
            attachmentPath.putFile(attachmentUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful())
                    {
                        Task<Uri> result = task.getResult().getMetadata().getReference().getDownloadUrl();
                        result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                attachmentURL = uri.toString();
                                savePostInfo();
                            }
                        });
                    }
                    else Toast.makeText(PostActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void savePostInfo()
    {
        usersRef.child(currentUserID).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                    String currentUsername = dataSnapshot.child("Username").getValue().toString();
                    String currentProfilePicture = dataSnapshot.child("Profile Picture").getValue().toString();

                    HashMap postMap = new HashMap();
                        postMap.put("UID", currentUserID);
                        postMap.put("Timestamp", currentTimestamp);
                        postMap.put("Title", postTitle);
                        postMap.put("Description", postDescription);
                        postMap.put("Attachment", attachmentURL);
                        postMap.put("Profile_Picture", currentProfilePicture);
                        postMap.put("Username", currentUsername);
                    if (postTypeIsVideo)
                    {
                        postMap.put("Thumbnail", thumbnailURL);
                        postedVideosRef.child(postName).updateChildren(postMap)
                                .addOnCompleteListener(new OnCompleteListener()
                                {
                                    @Override
                                    public void onComplete(@NonNull Task task)
                                    {
                                        String resultMsg = task.isSuccessful() ?
                                                PostActivity.this.getString(R.string.success_post_video_msg) :
                                                "Error: " + task.getException().getMessage();

                                        loadingBar.dismiss();
                                        Toast.makeText(PostActivity.this, resultMsg, Toast.LENGTH_SHORT).show();

                                        if (task.isSuccessful()) finish();
                                    }
                                });
                    }
                    else
                    {
                        postedEventsRef.child(postName).updateChildren(postMap)
                                .addOnCompleteListener(new OnCompleteListener()
                                {
                                    @Override
                                    public void onComplete(@NonNull Task task)
                                    {
                                        String resultMsg = task.isSuccessful() ?
                                                PostActivity.this.getString(R.string.success_post_event_msg) :
                                                "Error: " + task.getException().getMessage();

                                        loadingBar.dismiss();
                                        Toast.makeText(PostActivity.this, resultMsg, Toast.LENGTH_SHORT).show();

                                        if (task.isSuccessful()) finish();
                                    }
                                });
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {/*do nothing*/}
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode==RESULT_OK && data!=null)
        {
            if (requestCode==Gallery_Media)
            {
                attachmentUri = data.getData();
                thumbnailBitmap = generateThumbnail(attachmentUri);
                attachmentButton.setImageBitmap(thumbnailBitmap);
            }
        }
    }

    private Bitmap generateThumbnail(Uri videoUri)
    {
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = PostActivity.this.getContentResolver().query(videoUri, filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String picturePath = cursor.getString(columnIndex);
        cursor.close();

        return ThumbnailUtils.createVideoThumbnail(picturePath, MediaStore.Video.Thumbnails.MINI_KIND);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if (id==android.R.id.home) finish();

        return super.onOptionsItemSelected(item);
    }
}
