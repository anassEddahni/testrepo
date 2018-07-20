package app.adria.com.fbphotosexporting.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import app.adria.com.fbphotosexporting.R;
import app.adria.com.fbphotosexporting.adapters.MAdapter;
import app.adria.com.fbphotosexporting.entities.Photo;

public class PhotosActivity extends AppCompatActivity {

    public static final String ALBUM_ID_ARG = "album_id";
    private MAdapter adapter;
    private SwipeRefreshLayout srlRefresh;
    private RecyclerView rvPhotos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photos);

        // Allow Up navigation with the app icon in the action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // We can use ButterKnife instead(is a more efficient way)
        srlRefresh = findViewById(R.id.srl_refresh);
        rvPhotos = findViewById(R.id.rv_photos);

        adapter = new MAdapter();
        rvPhotos.setLayoutManager(new GridLayoutManager(this, 2));
        rvPhotos.setAdapter(adapter);

        srlRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchPhotos();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout_action:
                logOut();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        fetchPhotos();
    }

    private void fetchPhotos() {
        if (getIntent().hasExtra(ALBUM_ID_ARG)) {
            String albumId = getIntent().getExtras().getString(ALBUM_ID_ARG);
            if (albumId != null && !albumId.isEmpty()) {
                // Start refreshing
                srlRefresh.setRefreshing(true);

                new GraphRequest(
                        AccessToken.getCurrentAccessToken(),
                        "/" + albumId + "/photos/uploaded?fields=picture&size=large",
                        null,
                        HttpMethod.GET,
                        new GraphRequest.Callback() {
                            @Override
                            public void onCompleted(GraphResponse response) {
                                try {
                                    ArrayList<Photo> photos = new ArrayList<>();
                                    JSONArray data = (JSONArray) response.getJSONObject().get("data");
                                    for (int i = 0; i < data.length(); i++) {
                                        if (data.get(i) instanceof JSONObject) {
                                            JSONObject photo = (JSONObject) data.get(i);
                                            photos.add(new Photo(photo.getString("picture")));
                                        }
                                    }

                                    //Update adapter
                                    adapter.setData(photos);

                                    // Stop refreshing
                                    srlRefresh.setRefreshing(false);

                                } catch (JSONException e) {
                                    e.printStackTrace();

                                    // Stop refreshing
                                    srlRefresh.setRefreshing(false);
                                }
                            }
                        }).executeAsync();
            }
        }
    }

    private void logOut() {
        LoginManager.getInstance().logOut();
        startActivity(new Intent(this, AuthenticationActivity.class));
        finish();
    }
}
