package natekamp.ideas;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

public class SubjectActivity extends AppCompatActivity
{
    String subjectName = getIntent().getStringExtra("EXTRA_SUBJECT_NAME");

    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject);

        mToolbar = (Toolbar) findViewById(R.id.post_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(subjectName);
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
        Intent mainIntent = new Intent(SubjectActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
}
