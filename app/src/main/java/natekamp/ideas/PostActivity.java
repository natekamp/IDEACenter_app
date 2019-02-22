package natekamp.ideas;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

public class PostActivity extends AppCompatActivity
{
    private Toolbar mToolbar;
    final static int Gallery_Vid = 1;
    String postType = getIntent().getStringExtra("EXTRA_POST_TYPE");

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mToolbar = (Toolbar) findViewById(R.id.post_toolbar);
            setSupportActionBar(mToolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(R.string.post_toolbar_title);
    }

    public void getVideo()
    {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("video/*");
        startActivityForResult(
                Intent.createChooser(galleryIntent,PostActivity.this.getString(R.string.post_video_choose)),
                Gallery_Vid);
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
