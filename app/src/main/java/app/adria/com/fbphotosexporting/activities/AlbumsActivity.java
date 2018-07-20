package app.adria.com.fbphotosexporting.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.facebook.Profile;
import com.facebook.login.LoginManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import app.adria.com.fbphotosexporting.R;
import app.adria.com.fbphotosexporting.adapters.MAdapter;
import app.adria.com.fbphotosexporting.entities.Photo;

public class AlbumsActivity extends AppCompatActivity {

    private MAdapter adapter;
    private SwipeRefreshLayout srlRefresh;
    private RecyclerView rvAlbums;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_albums);

        //We can use ButterKnife instead(is a more efficient way)
        srlRefresh = findViewById(R.id.srl_refresh);
        rvAlbums = findViewById(R.id.rv_albums);

        adapter = new MAdapter(new MAdapter.ItemClickedListener() {
            @Override
            public void clicked(@NonNull MAdapter.MViewHolder holder, @NonNull Photo photo) {
                Intent intent = new Intent(AlbumsActivity.this, PhotosActivity.class);
                intent.putExtra(PhotosActivity.ALBUM_ID_ARG, photo.getId());
                startActivity(intent);
            }
        });
        rvAlbums.setLayoutManager(new GridLayoutManager(this, 2));
        rvAlbums.setAdapter(adapter);

        srlRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchAlbums();
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

        fetchAlbums();
    }

    private void fetchAlbums() {
        // Start refreshing
        srlRefresh.setRefreshing(true);

        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/" + Profile.getCurrentProfile().getId() + "/albums?fields=name",
                null,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {
                        try {
                            ArrayList<Photo> albums = new ArrayList<>();
                            JSONArray data = (JSONArray) response.getJSONObject().get("data");
                            for (int i = 0; i < data.length(); i++) {
                                if (data.get(i) instanceof JSONObject) {
                                    JSONObject photo = (JSONObject) data.get(i);
                                    albums.add(new Photo(photo.getString("id"), photo.getString("name")));
                                }
                            }

                            // Sort the albums by name
                            Collections.sort(albums, new Comparator<Photo>() {
                                @Override
                                public int compare(Photo o1, Photo o2) {
                                    return o1.getName().compareTo(o2.getName());
                                }
                            });

                            // Update adapter
                            adapter.setData(albums);

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

    private void logOut() {
        LoginManager.getInstance().logOut();
        startActivity(new Intent(AlbumsActivity.this, AuthenticationActivity.class));
        finish();
//        new GraphRequest(
//                AccessToken.getCurrentAccessToken(),
//                "/" + Profile.getCurrentProfile().getId() + "/permissions",
//                null,
//                HttpMethod.DELETE,
//                new GraphRequest.Callback() {
//                    @Override
//                    public void onCompleted(GraphResponse response) {
//                        LoginManager.getInstance().logOut();
//                        startActivity(new Intent(AlbumsActivity.this, AuthenticationActivity.class));
//                        finish();
//    }
//                }
//        ).executeAsync();
    }
}
