package natekamp.ideas;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
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
    private FirebaseAuth mAuth;
    String currentUserID;
    private DatabaseReference usersRef;
    private StorageReference userImageRef;

    private EditText userName, userGrade;
    private Button saveButton;
    private CircleImageView profileImage;

    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

    //firebase authentication
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
    //database and storage references
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        userImageRef = FirebaseStorage.getInstance().getReference().child("profile_pictures");
    //ImageViews, Buttons, and EditTexts
        profileImage = (CircleImageView) findViewById(R.id.setup_picture);
        userName = (EditText) findViewById(R.id.setup_name);
        userGrade = (EditText) findViewById(R.id.setup_grade);
        saveButton = (Button) findViewById(R.id.setup_save);

        loadingBar = new ProgressDialog(this);


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

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                    if (dataSnapshot.hasChild("Profile Picture"))
                    {
                        String profile_picture = dataSnapshot.child("Profile Picture").getValue().toString();
                        Picasso.get().load(profile_picture).placeholder(R.drawable.profile_picture).into(profileImage);
                    }
                    else
                        Toast.makeText(SetupActivity.this, SetupActivity.this.getString(R.string.error_missing_pfp_msg_b), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
                //do nothing
            }
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
                StorageReference filePath = userImageRef.child(currentUserID+".png");

                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
                    {
                        if (task.isSuccessful())
                        {
                            Toast.makeText(SetupActivity.this, SetupActivity.this.getString(R.string.success_pfp_msg_a), Toast.LENGTH_SHORT).show();

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

                                                    if (task.isSuccessful()) {
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
            usersRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task)
                {
                    String resultMsg = task.isSuccessful() ?
                            SetupActivity.this.getString(R.string.success_setup_msg) :
                            "Error: " + task.getException().getMessage();

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
