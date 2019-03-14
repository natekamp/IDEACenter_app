package natekamp.ideas;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SubjectActivity extends AppCompatActivity
{
    String subjectName = getIntent().getStringExtra("EXTRA_SUBJECT_NAME");

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject);
    }
}
