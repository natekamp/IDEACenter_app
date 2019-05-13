package natekamp.ideas;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
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
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity
{
    //extras
    private String profileUid;
    private boolean fromRegisterActivity, profileIsEditable;

    //firebase
    private FirebaseAuth mAuth;
    String currentUserID, profilePicture, username, bio;
    private DatabaseReference usersRef;
    private StorageReference userImageRef;

    //toolbar
    private Toolbar mToolbar;

    //views
    private EditText userName, userBio;
    private Button saveButton;
    private CircleImageView profileImage;

    //progress dialog
    private ProgressDialog loadingBar;

    //other
    private String defaultProfilePictureURL = "https://firebasestorage.googleapis.com/v0/b/ideas-5e85b.appspot.com/o/profile_pictures%2Fprofile_picture.jpg?alt=media&token=be5f8d6d-7450-4bf1-ae44-40dac0bde461";
    private boolean authorized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        //firebase
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(profileUid);
        usersRef.child("Profile Picture").setValue(defaultProfilePictureURL);
        userImageRef = FirebaseStorage.getInstance().getReference().child("profile_pictures").child(profileUid+".png");

        //extras
        profileUid = getIntent().getExtras().getString("EXTRA_PROFILE_UID", currentUserID);
        fromRegisterActivity = getIntent().getExtras().getBoolean("EXTRA_FROM_REGISTER", false);
        profileIsEditable = getIntent().getExtras().getBoolean("EXTRA_IS_EDITABLE", false);

        //toolbar
        mToolbar = (Toolbar) findViewById(R.id.setup_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.setup_view_toolbar_title);

        //views
        profileImage = (CircleImageView) findViewById(R.id.setup_picture);
        userName = (EditText) findViewById(R.id.setup_name);
        userBio = (EditText) findViewById(R.id.setup_bio);
        saveButton = (Button) findViewById(R.id.setup_save);

        //progress dialog
        loadingBar = new ProgressDialog(this);

        //other
        authorized = currentUserID.equals(profileUid);


        applyModeFeatures();

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                    profilePicture = dataSnapshot.hasChild("Profile Picture") ?
                            dataSnapshot.child("Profile Picture").getValue().toString() : defaultProfilePictureURL;
                    Picasso.get().load(profilePicture).placeholder(R.drawable.profile_picture).into(profileImage);
                    if (dataSnapshot.hasChild("Username"))
                    {
                        username = dataSnapshot.child("Username").getValue().toString();
                        userName.setText(username);
                    }
                    if (dataSnapshot.hasChild("Bio"))
                    {
                        bio = dataSnapshot.child("Bio").getValue().toString();
                        userBio.setText(bio);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {/*do nothing*/}
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result  = CropImage.getActivityResult(data);
            if (resultCode==RESULT_OK && data!=null)
            {
                loadingBar.setTitle(SetupActivity.this.getString(R.string.progress_title));
                loadingBar.setMessage(SetupActivity.this.getString(R.string.progress_pfp_msg));
                loadingBar.show();
                loadingBar.setCanceledOnTouchOutside(true);

                Uri resultUri = result.getUri();

                userImageRef.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
                    {
                        if (task.isSuccessful())
                        {
                            Task<Uri> result = task.getResult().getMetadata().getReference().getDownloadUrl();
                            result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri)
                                {
                                    final String downloadUrl = uri.toString();
                                    usersRef.child("Profile Picture").setValue(downloadUrl)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task)
                                                {
                                                    String resultMsg = task.isSuccessful() ?
                                                            SetupActivity.this.getString(R.string.success_pfp_msg_b) :
                                                            "Error: " + task.getException().getMessage();

                                                    loadingBar.dismiss();
                                                    Toast.makeText(SetupActivity.this, resultMsg, Toast.LENGTH_SHORT).show();

                                                    if (task.isSuccessful())
                                                    {
                                                        Intent selfIntent = new Intent(SetupActivity.this, SetupActivity.class);
                                                        startActivity(selfIntent);
                                                    }
                                                }
                                            });
                                }
                            });
                        }
                    }
                });
            }
            else
            {
                Toast.makeText(SetupActivity.this, SetupActivity.this.getString(R.string.error_retry_msg), Toast.LENGTH_SHORT).show();
                loadingBar.dismiss(); //not sure if this is necessary but whatever
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if (id==android.R.id.home) finish();

        return super.onOptionsItemSelected(item);
    }

    private void applyModeFeatures()
    {
        if (!authorized || !profileIsEditable)
        {
            disableEditText(userName);
            disableEditText(userBio);
            saveButton.setVisibility(View.INVISIBLE);
            userBio.setHint(R.string.setup_no_bio_hint);
        }
        else
        {
            mToolbar.setVisibility(View.GONE);

            saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    saveSetupInfo();
                }
            });

            profileImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    CropImage.activity()
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .setAspectRatio(1, 1)
                            .start(SetupActivity.this);
                }
            });
        }
    }

    private void saveSetupInfo()
    {
        username = userName.getText().toString();
        bio = userBio.getText().toString();

        if (TextUtils.isEmpty(username))
            Toast.makeText(this, this.getString(R.string.error_username_msg), Toast.LENGTH_SHORT).show();
        else
        {
            loadingBar.setTitle(SetupActivity.this.getString(R.string.progress_title));
            loadingBar.setMessage(SetupActivity.this.getString(R.string.progress_setup_msg));
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            HashMap userMap = new HashMap();
                userMap.put("Username", username);
                userMap.put("Bio", bio);
            usersRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task)
                {
                    String resultMsg = task.isSuccessful() ?
                            SetupActivity.this.getString(R.string.success_setup_msg) :
                            "Error: " + task.getException().getMessage();

                    loadingBar.dismiss();
                    Toast.makeText(SetupActivity.this, resultMsg, Toast.LENGTH_SHORT).show();

                    if (task.isSuccessful())
                    {
                        if (fromRegisterActivity) sendToMainActivity();
                        else finish();
                    }
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

    private void disableEditText(EditText editText)
    {
        editText.setFocusable(false);
        editText.setEnabled(false);
        editText.setCursorVisible(false);
        editText.setKeyListener(null);
    }
}
