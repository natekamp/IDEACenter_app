package natekamp.ideas;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements SubjectListRecyclerAdapter.ItemClickListener
{
    //firebase
    private FirebaseAuth mAuth;
    String currentUserID;
    private DatabaseReference usersRef;

    //toolbar
    private Toolbar mToolbar;

    //drawer menu
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    //inside drawer
    private NavigationView navigationView;
    private CircleImageView headerProfilePicture;
    private TextView headerUsername;

    //subject list
    private RecyclerView subjectList;
    private ArrayList<String> subjectNames;
    private ArrayList<Integer> subjectThumbnails;
    private SubjectListRecyclerAdapter sLAdapter;

    //cards
    private RelativeLayout calendarCard;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //firebase
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        //toolbar
        mToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(R.string.nav_home_title);

        //drawer menu
        drawerLayout = (DrawerLayout) findViewById(R.id.drawable_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //inside drawer
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        View navView = navigationView.inflateHeaderView(R.layout.navigation_header);
        headerProfilePicture = (CircleImageView) navView.findViewById(R.id.nav_profile_picture);
        headerUsername = (TextView) navView.findViewById(R.id.nav_username);

        //subject list
        subjectList = (RecyclerView) findViewById(R.id.main_subjects_list);
        subjectList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        subjectList.setLayoutManager(linearLayoutManager);

        //cards
        calendarCard = (RelativeLayout) findViewById(R.id.main_calendar_card);
        ((TextView) calendarCard.findViewById(R.id.card_text)).setText(R.string.subject_master_calendar);
        ((ImageView) calendarCard.findViewById(R.id.card_image)).setImageResource(R.drawable.calendar_thumbnail);


        displaySubjectList();

        calendarCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                sendToCalendarActivity();
            }
        });

        //put username and profile picture from database into drawer header
        usersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                    if (dataSnapshot.hasChild("Username"))
                    {
                        String username = dataSnapshot.child("Username").getValue().toString();
                        headerUsername.setText(username);
                    }
                    else
                        Toast.makeText(MainActivity.this, MainActivity.this.getString(R.string.error_missing_name_msg), Toast.LENGTH_SHORT).show();

                    if (dataSnapshot.hasChild("Profile Picture"))
                    {
                        String pfpLink = dataSnapshot.child("Profile Picture").getValue().toString();
                        Picasso.get().load(pfpLink).placeholder(R.drawable.profile_picture).into(headerProfilePicture);
                    }
                    else
                        Toast.makeText(MainActivity.this, MainActivity.this.getString(R.string.error_missing_pfp_msg_a), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
                //do nothing
            }
        });

        //listener for menu items in drawer
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener()
        {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item)
            {
                UserMenuSelector(item);
                return false;
            }
        });
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        FirebaseUser currentUser_check = mAuth.getCurrentUser();

        if (currentUser_check == null) sendToLoginActivity();
        else checkIfUserExists();
    }

    private void checkIfUserExists()
    {
        final String currentUserID_check = mAuth.getCurrentUser().getUid();

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (!dataSnapshot.hasChild(currentUserID_check)) sendToSetupActivity();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {/*do nothing*/}
        });
    }

    private void sendToSetupActivity(boolean fromRegister, boolean isEditable, String profileUID)
    {
        Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        setupIntent.putExtra("EXTRA_PROFILE_UID", profileUID);
        setupIntent.putExtra("EXTRA_FROM_REGISTER", fromRegister);
        setupIntent.putExtra("EXTRA_IS_EDITABLE", isEditable);
        startActivity(setupIntent);
        finish();
    }

    private void sendToSetupActivity()
    {
        Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        setupIntent.putExtra("EXTRA_FROM_REGISTER", true);
        setupIntent.putExtra("EXTRA_IS_EDITABLE", true);
        startActivity(setupIntent);
        finish();
    }

    private void sendToLoginActivity()
    {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    private void sendToSubjectActivity(String subjectName, int subjectThumbnail)
    {
        Intent subjectIntent = new Intent(MainActivity.this, SubjectActivity.class);
        subjectIntent.putExtra("EXTRA_SUBJECT_NAME", subjectName);
        subjectIntent.putExtra("EXTRA_SUBJECT_IMAGE", subjectThumbnail);
        startActivity(subjectIntent);
    }

    private void sendToAboutActivity()
    {
        Intent aboutIntent = new Intent(MainActivity.this, AboutActivity.class);
        startActivity(aboutIntent);
    }

    private void sendToCalendarActivity()
    {
        //TODO: this
    }

    private void displaySubjectList()
    {
        subjectNames = new ArrayList<>();
        subjectThumbnails = new ArrayList<>();

        //add subject cards to RecyclerView
        subjectNames.add(this.getString(R.string.subject_name_art));
        subjectThumbnails.add(R.drawable.art_thumbnail);

        subjectNames.add(this.getString(R.string.subject_name_business));
        subjectThumbnails.add(R.drawable.business_thumbnail);

        subjectNames.add(this.getString(R.string.subject_name_culinary));
        subjectThumbnails.add(R.drawable.culinary_thumbnail);

        subjectNames.add(this.getString(R.string.subject_name_electronics));
        subjectThumbnails.add(R.drawable.electronics_thumbnail);

        subjectNames.add("Subject 5");
        subjectThumbnails.add(R.drawable.placeholder_image);

        sLAdapter = new SubjectListRecyclerAdapter(this, subjectNames, subjectThumbnails);
        sLAdapter.setClickListener(this);
        subjectList.setAdapter(sLAdapter);
    }

    //RecyclerView subject card click
    @Override
    public void onItemClick(View view, int position)
    {
        sendToSubjectActivity(sLAdapter.getString(position), sLAdapter.getThumbnail(position));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) return true;
        return super.onOptionsItemSelected(item);
    }

    //drawer menu item click
    private void UserMenuSelector(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_profile:
                sendToSetupActivity(false, true, currentUserID);
                break;
            case R.id.nav_home:
                Toast.makeText(this, "Home Selected", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_about:
                sendToAboutActivity();
                break;
            case R.id.nav_messages:
                Toast.makeText(this, "Messages Selected", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_settings:
                Toast.makeText(this, "Settings Selected", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_logout:
                mAuth.signOut();
                Toast.makeText(this, this.getString(R.string.success_logout_msg), Toast.LENGTH_SHORT).show();
                sendToLoginActivity();
                break;
        }
    }
}
