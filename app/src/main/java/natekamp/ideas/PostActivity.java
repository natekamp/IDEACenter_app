package natekamp.ideas;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

public class PostActivity extends AppCompatActivity
{
    private Toolbar mToolbar;
    private Button finishButton;
    private ImageButton attachmentButton;
    private EditText titleText, descriptionText;
    private final static int Gallery_Img = 1, Gallery_Pic = 2;
    boolean postTypeIsVideo = getIntent().getBooleanExtra("EXTRA_POST_TYPE", true);

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mToolbar = (Toolbar) findViewById(R.id.post_toolbar);
            setSupportActionBar(mToolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(
                    postTypeIsVideo ? R.string.post_video_toolbar_title : R.string.post_event_toolbar_title
            );
        attachmentButton = (ImageButton) findViewById(R.id.post_attachment);
        titleText = (EditText) findViewById(R.id.post_title);
        descriptionText = (EditText) findViewById(R.id.post_description);

        attachmentButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (postTypeIsVideo) getVideo();
                else getImage();
            }
        });

    }

    public void getVideo()
    {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("video/*");
        startActivityForResult(galleryIntent, Gallery_Img);
    }
    public void getImage()
    {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, Gallery_Pic);
//        startActivityForResult(
//                Intent.createChooser(galleryIntent,PostActivity.this.getString(R.string.post_image_choose)),
//                Gallery_Pic);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if (id==android.R.id.home) sendToMainActivity();

        return super.onOptionsItemSelected(item);
    }

    private void sendToMainActivity()
    {
        Intent mainIntent = new Intent(PostActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
}
