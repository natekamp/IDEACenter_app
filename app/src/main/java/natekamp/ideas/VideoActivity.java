package natekamp.ideas;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;

import static android.view.View.SYSTEM_UI_FLAG_FULLSCREEN;
import static android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

public class VideoActivity extends AppCompatActivity
{
    //extras
    String videoURL;

    //views
    private VideoView videoView;

    //controller
    private MediaController mediaController;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        //extras
        videoURL = getIntent().getExtras().getString("EXTRA_VIDEO_URL", "https://youtu.be/dQw4w9WgXcQ");

        //views
        videoView = findViewById(R.id.video_player);
        videoView.setVideoPath(videoURL);

        //controller
        mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);

        videoView.setMediaController(mediaController);
        videoView.requestFocus();
        videoView.start();
    }
}
