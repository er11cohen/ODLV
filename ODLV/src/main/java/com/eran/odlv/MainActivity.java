package com.eran.odlv;

import android.R.drawable;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;
import android.widget.Toast;

import com.eran.utils.Utils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class MainActivity extends Activity {
    String[] menu;
    DrawerLayout dLayout;
    ListView dList;
    ArrayAdapter<String> adapter;

    private ActionBarDrawerToggle mDrawerToggle;
    private String mActivityTitle;

    private ListView lv;
    private ArrayList<Halach> alHalach;
    private ArrayList<Halach> alHalachFilter;

    String shareStr = "ילקוט יוסף - אוצר דינים לאישה ולבת  - Yalkut Yosef - Otzar Dinim L'Ishah UleBas https://play.google.com/store/apps/details?id=com.eran.odlv";
    WeakReference<Activity> weakReferenceActivity;

    Boolean DrawerLayoutOpen = false;
    SearchView searchView;
    SharedPreferences odlvPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        odlvPreferences = getSharedPreferences("odlvPreferences", MODE_PRIVATE);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        // get the parent view of home (app icon) imageview
        ViewGroup home = (ViewGroup) findViewById(android.R.id.home).getParent();
        // get the first child (up imageview)
        // change the icon according to your needs
        ((ImageView) home.getChildAt(0)).setImageResource(R.drawable.ic_drawer);

        mActivityTitle = getTitle().toString();

        menu = getResources().getStringArray(R.array.menu_array);
        dLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        dList = (ListView) findViewById(R.id.left_drawer);

        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, menu);

        dList.setAdapter(adapter);
        //dList.setSelector(android.R.color.holo_blue_dark);

        dList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View v, int position,
                                    long id) {
                dLayout.closeDrawers();

                switch (position) {
                    case 0:
                        LastLocation();
                        break;
                    case 1:
                        SelectHistory();
                        break;
                    case 2:
                        OpenSettings();
                        break;
                    case 3:
                        OpenHelp();
                        break;
                    case 4:
                        OpenAbout();
                        break;
                    default:
                        break;
                }

            }

        });

        setupDrawer();
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        dLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState(); //set the back icon on title

        lv = (ListView) findViewById(R.id.ListViewHlach);
        alHalachFilter = new ArrayList<Halach>();
        alHalach = UtilODLV.getHalachArray(getApplicationContext());

        ArrayAdapter<Halach> adapter = new ArrayAdapter<Halach>(this,
                android.R.layout.simple_list_item_1, alHalach);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View currView,
                                    int position, long id) {
                Halach selected = (Halach) lv.getItemAtPosition(position);

                Intent intent = new Intent(getApplicationContext(),
                        WebActivity.class);
                intent.putExtra("halachHe", selected.getHalachHe());
                intent.putExtra("htmlPageIndex", selected.getHtmlPageIndex());
                intent.putExtra("href", selected.getHref());
                startActivity(intent);
            }
        });

        weakReferenceActivity = new WeakReference<Activity>(this);


        String version = odlvPreferences.getString("version", "-1");
        if (!version.equals("1.0.5")) {
            String message = Utils.ReadTxtFile("files/newVersion.txt", getApplicationContext());
            ((TextView) new AlertDialog.Builder(this)
                    .setTitle("חדשות ללומדות ילקוט יוסף הלכות לאשה ולבת")
                    .setIcon(android.R.drawable.ic_menu_info_details)
                    .setIcon(drawable.ic_input_add)
                    .setMessage(Html.fromHtml(message))
                    .setPositiveButton("אשריכן תזכו למצוות", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .show()
                    .findViewById(android.R.id.message))
                    .setMovementMethod(LinkMovementMethod.getInstance());

            SharedPreferences.Editor editor = odlvPreferences.edit();
            editor.putString("version", "1.0.5");
            editor.commit();
        }
    }

    private void setupDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, dLayout,
                // R.drawable.ic_drawer,
                R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                DrawerLayoutOpen = true;
                getActionBar().setTitle("בחרי פעולה");
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                DrawerLayoutOpen = false;
                getActionBar().setTitle(mActivityTitle);
            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.main, menu);

        searchView = (SearchView) menu.findItem(R.id.menu_item_search).getActionView();
        searchView.setQueryHint("חיפוש במפתח");

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new OnQueryTextListener() {

            @Override
            public boolean onQueryTextChange(String query) {

                // Toast.makeText(getApplicationContext(),"onQueryTextChange " +query ,Toast.LENGTH_LONG).show();

                int textlength = query.length();
                alHalachFilter.clear();
                for (int i = 0; i < alHalach.size(); i++) {
                    if (textlength <= alHalach.get(i).getHalachHe().length()) {
                        if (alHalach.get(i).getHalachHe().contains(query)) {
                            alHalachFilter.add(alHalach.get(i));
                        }
                    }
                }
                lv.setAdapter(new ArrayAdapter<Halach>(MainActivity.this, android.R.layout.simple_list_item_1, alHalachFilter));

                return true;

            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                // TODO Auto-generated method stub
                return false;
            }

        });

        return true;
    }

    //for voice search
    @Override
    protected void onNewIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            searchView.setQuery(query, false);

            //close the keyboard
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (!searchView.isIconified()) {
            searchView.setIconified(true);// clear the searchView
            searchView.onActionViewCollapsed();//close the searchView
        } else {
            super.onBackPressed();
        }

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
            case R.id.menu_item_score:
                scoreInGooglePlay();
                break;
            case R.id.menu_item_share:
                Utils.shareApp(weakReferenceActivity, shareStr);
                break;
            default:
                break;
        }

        // TODO Auto-generated method stub
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onKeyDown(int keycode, KeyEvent e) {
        switch (keycode) {
            case KeyEvent.KEYCODE_MENU:

                if (!DrawerLayoutOpen) {
                    dLayout.openDrawer(/*Gravity.START*/ 8388611);
                } else {
                    dLayout.closeDrawers();
                }
                return true;
        }

        return super.onKeyDown(keycode, e);
    }

    public void LastLocation() {
        SharedPreferences preferences = getSharedPreferences("Locations",
                MODE_PRIVATE);
        String preferencesLocationsJson = preferences.getString(
                "preferencesLocationsJson", null);
        if (preferencesLocationsJson != null) {
            Intent intent = new Intent(getApplicationContext(),
                    WebActivity.class);
            intent.putExtra("requiredFileName", "-1"/* lastLocation */);
            startActivity(intent);
        }
    }

    public void SelectHistory() {
        Intent intent = new Intent(getApplicationContext(), GalleryOdlv.class);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1/* from gallery */) {
            if (data != null && data.getExtras().containsKey("fileName")) {
                String fileName = data.getStringExtra("fileName");

                Intent intent = new Intent(getApplicationContext(),
                        WebActivity.class);
                intent.putExtra("requiredFileName", fileName);
                startActivity(intent);
            }
        }
    }

    public void OpenSettings() {
        Intent intent = new Intent(getApplicationContext(),
                SettingsActivity.class);
        startActivity(intent);
    }

    public void OpenHelp() {
        Utils.alertDialogShow(weakReferenceActivity, getApplicationContext(),
                "עזרה", android.R.drawable.ic_menu_help, "files/help.txt",
                "הבנתי", "זכי את הרבים", shareStr);
    }

    public void OpenAbout() {
        Utils.alertDialogShow(weakReferenceActivity, getApplicationContext(),
                "אודות", android.R.drawable.ic_menu_info_details,
                "files/about.txt", "אשריכן תזכו למצוות", "זכי את הרבים",
                shareStr);
    }


    private void scoreInGooglePlay() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.eran.odlv"));
        startActivity(browserIntent);

        String text = "צדיקה דרגי אותנו  5 כוכבים וטלי חלק בזיכוי הרבים.";
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }
}