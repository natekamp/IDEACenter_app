package natekamp.ideas;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.VideoView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class SubjectActivity extends AppCompatActivity
{
    String subjectName = getIntent().getStringExtra("EXTRA_SUBJECT_NAME");
    int subjectImage = getIntent().getIntExtra("EXTRA_SUBJECT_IMAGE", R.drawable.calendar_thumbnail);

    private Toolbar mToolbar;
    private RecyclerView subjectVideosList;
    private ImageButton postButton;

    private DatabaseReference postedVideosRef;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject);

    //toolbar
        mToolbar = (Toolbar) findViewById(R.id.post_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(subjectName);
    //buttons
        postButton = (ImageButton) findViewById(R.id.subject_post_button);
    //video list recycler
        subjectVideosList = (RecyclerView) findViewById(R.id.subject_video_post_list);
        subjectVideosList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        subjectVideosList.setLayoutManager(linearLayoutManager);
    //database reference for posted videos
        postedVideosRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(subjectName).child("Videos");

        displayVideoPosts();

        postButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendToPostActivity();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if (id==android.R.id.home) sendToMainActivity();

        return super.onOptionsItemSelected(item);
    }

    private void displayVideoPosts()
    {
        FirebaseRecyclerOptions<Posts> firebaseRecyclerOptions =
                new FirebaseRecyclerOptions.Builder<Posts>().setQuery(postedVideosRef, Posts.class).build();

        FirebaseRecyclerAdapter<Posts, PostsViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Posts, PostsViewHolder>(firebaseRecyclerOptions)
                {
                    @Override
                    protected void onBindViewHolder(@NonNull PostsViewHolder holder, int position, @NonNull Posts model)
                    {
                        holder.setUsername(model.getUsername());
                        holder.setTime(model.getTime());
                        holder.setDate(model.getDate());
                        holder.setDescription(model.getDescription());
                        holder.setProfile_Picture(model.getProfile_Picture());
                        holder.setAttachment(model.getAttachment());
                        //maybe need getApplicationContext() as the first parameter for the pfp and attachment
                        //setOnClickListener here?
                    }

                    @NonNull
                    @Override
                    public PostsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
                    {
                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.general_video_post_layout,viewGroup,false);
                        return new PostsViewHolder(view);
                    }
                };

        subjectVideosList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public static class PostsViewHolder extends RecyclerView.ViewHolder
    {
        View mView;

        public PostsViewHolder(@NonNull View itemView)
        {
            super(itemView);

            mView = itemView;
        }

        public void setUsername(String username)
        {
            TextView postUsername = (TextView) mView.findViewById(R.id.videoPost_username);
            postUsername.setText(username);
        }

        public void setDate(String date)
        {
            TextView postDate = (TextView) mView.findViewById(R.id.videoPost_date);
            postDate.setText(date);
        }

        public void setTime(String time)
        {
            TextView postTime = (TextView) mView.findViewById(R.id.videoPost_time);
            postTime.setText(time);
        }

        public void setTitle(String title)
        {
            TextView postTitle = (TextView) mView.findViewById(R.id.videoPost_title);
            postTitle.setText(title);
        }

        public void setDescription(String description)
        {
            TextView postDescription = (TextView) mView.findViewById(R.id.videoPost_description);
            postDescription.setText(description);
        }

        public void setProfile_Picture(String Profile_Picture)
        {
            CircleImageView profilePicture = (CircleImageView) mView.findViewById(R.id.videoPost_profile_picture);
            Picasso.get().load(Profile_Picture).placeholder(R.drawable.profile_picture).into(profilePicture);
        }

        public void setAttachment(String attachment)
        {
            VideoView video = (VideoView) mView.findViewById(R.id.videoPost_video);
            //TODO: get the video to load
        }
    }

    private void sendToPostActivity()
    {
        Intent postIntent = new Intent(SubjectActivity.this, PostActivity.class);
        postIntent.putExtra("EXTRA_IS_VIDEO", true);
        postIntent.putExtra("EXTRA_SUBJECT_NAME", subjectName);
        startActivity(postIntent);
    }

    private void sendToMainActivity()
    {
        Intent mainIntent = new Intent(SubjectActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
}
