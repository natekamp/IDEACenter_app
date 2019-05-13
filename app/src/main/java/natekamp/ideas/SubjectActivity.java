package natekamp.ideas;

import android.content.Intent;
import android.content.pm.ActivityInfo;
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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class SubjectActivity extends AppCompatActivity
{
    //extras
    String subjectName;
    int subjectImage;

    //firebase
    private FirebaseAuth mAuth;
    String currentUserID;
    private DatabaseReference postedVideosRef;

    //toolbar
    private Toolbar mToolbar;

    //recycler
    private RecyclerView subjectVideosList;

    //views
    private ImageView postButton;

    //cards
    private RelativeLayout calendarCard, tourCard;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //extras
        subjectName = getIntent().getExtras().getString("EXTRA_SUBJECT_NAME", "placeholder_name");
        subjectImage = getIntent().getExtras().getInt("EXTRA_SUBJECT_IMAGE", R.drawable.placeholder_image);

        //firebase
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        postedVideosRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(subjectName).child("Videos");

        //toolbar
        mToolbar = (Toolbar) findViewById(R.id.subject_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(subjectName);

        //recycler
        subjectVideosList = (RecyclerView) findViewById(R.id.subject_video_post_list);
        subjectVideosList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        subjectVideosList.setLayoutManager(linearLayoutManager);

        //views
        postButton = (ImageView) findViewById(R.id.subject_post_button);

        //cards
        calendarCard = (RelativeLayout) findViewById(R.id.subject_calendar_card);
        ((TextView) calendarCard.findViewById(R.id.card_text)).setText(R.string.subject_calendar);
        ((ImageView) calendarCard.findViewById(R.id.card_image)).setImageResource(R.drawable.calendar_thumbnail);
        tourCard = (RelativeLayout) findViewById(R.id.subject_tour_card);
        ((TextView) tourCard.findViewById(R.id.card_text)).setText(R.string.subject_tour);
        ((ImageView) tourCard.findViewById(R.id.card_image)).setImageResource(subjectImage);


        displayVideoPosts();

        postButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendToPostActivity();
            }
        });
        calendarCard.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendToCalendarActivity();
            }
        });
        tourCard.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendToTourActivity();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if (id==android.R.id.home) finish();

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
                        final String PostKey = getRef(position).getKey();
                        final String VideoPostURL = model.getAttachment();
                        final boolean isUsersPost = PostKey.contains(currentUserID);

                        holder.setUsername(model.getUsername());
                        holder.setTimestamp(model.getTimestamp());
                        holder.setTitle(model.getTitle());
                        holder.setDescription(model.getDescription());
                        holder.setProfile_Picture(model.getProfile_Picture());
                        holder.setThumbnail(model.getThumbnail());
                        if (!isUsersPost) holder.removeEditor();

                        holder.mProfile_Picture.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                Toast.makeText(SubjectActivity.this, "TODO: Send to user profile", Toast.LENGTH_SHORT).show();
                            }
                        });
                        holder.mEditor.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                if (isUsersPost) sendToPostEditorActivity(PostKey, true);
                            }
                        });
                        holder.mTitle.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                sendToPostEditorActivity(PostKey, false);
                            }
                        });
                        holder.mDescription.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                sendToPostEditorActivity(PostKey, false);
                            }
                        });
                        holder.mThumbnail.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                sendToVideoActivity(VideoPostURL);
                            }
                        });
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

    //holder class for firebase recycler
    public static class PostsViewHolder extends RecyclerView.ViewHolder
    {
        View mView;
        CircleImageView mProfile_Picture;
        ImageView mThumbnail, mEditor;
        TextView mUsername, mTimestamp, mTitle, mDescription;


        public PostsViewHolder(@NonNull View itemView)
        {
            super(itemView);

            mView = itemView;
            mProfile_Picture = (CircleImageView) mView.findViewById(R.id.videoPost_profile_picture);
            mThumbnail = (ImageView) mView.findViewById(R.id.videoPost_video_thumbnail);
            mEditor = (ImageView) mView.findViewById(R.id.videoPost_editor_button);
            mUsername = (TextView) mView.findViewById(R.id.videoPost_username);
            mTimestamp = (TextView) mView.findViewById(R.id.videoPost_timestamp);
            mTitle = (TextView) mView.findViewById(R.id.videoPost_title);
            mDescription = (TextView) mView.findViewById(R.id.videoPost_description);
        }

        public void removeEditor()
        {
            mEditor.setVisibility(View.INVISIBLE);
        }

        public void setUsername(String username)
        {
            mUsername.setText(username);
        }

        public void setTimestamp(String timestamp)
        {
            mTimestamp.setText(timestamp);
        }

        public void setTitle(String title)
        {
            mTitle.setText(title);
        }

        public void setDescription(String description)
        {
            mDescription.setText(description);
        }

        public void setProfile_Picture(String Profile_Picture)
        {
            Picasso.get().load(Profile_Picture).placeholder(R.drawable.profile_picture).into(mProfile_Picture);
        }

        public void setThumbnail(String thumbnail)
        {
            Picasso.get().load(thumbnail).placeholder(R.drawable.placeholder_image).into(mThumbnail);
        }
    }

    private void sendToPostActivity()
    {
        Intent postIntent = new Intent(SubjectActivity.this, PostActivity.class);
        postIntent.putExtra("EXTRA_IS_VIDEO", true);
        postIntent.putExtra("EXTRA_SUBJECT_NAME", subjectName);
        startActivity(postIntent);
    }

    private void sendToCalendarActivity()
    {
        //TODO: this
    }

    private void sendToTourActivity()
    {
        //TODO: this
    }

    private void sendToPostEditorActivity(String key, boolean isEditable)
    {
        Intent editorIntent = new Intent(SubjectActivity.this, PostViewActivity.class);
        editorIntent.putExtra("EXTRA_POST_KEY", key);
        editorIntent.putExtra("EXTRA_IS_EDITABLE", isEditable);
        editorIntent.putExtra("EXTRA_SUBJECT_NAME", subjectName);
        startActivity(editorIntent);
    }

    private void sendToVideoActivity(String videoURL)
    {
        Intent videoIntent = new Intent(SubjectActivity.this, VideoActivity.class);
        videoIntent.putExtra("EXTRA_VIDEO_URL", videoURL);
        startActivity(videoIntent);
    }
}
