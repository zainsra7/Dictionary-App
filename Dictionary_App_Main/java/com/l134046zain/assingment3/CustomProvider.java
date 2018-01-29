package com.l134046zain.assingment3;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

/**
 * Created by Zan on 11/17/2016.
 */
public class CustomProvider extends ContentProvider {



    static final String PROVIDER_NAME = "com.l134046zain.assingment3.CustomProvider";
    static final String URL = "content://" + PROVIDER_NAME + "/words";
    static final Uri CONTENT_URI = Uri.parse(URL);

    private static HashMap<String, String> WORDS_PROJECTION_MAP;

    static final int WORDS = 1;
    static final int SINGLE_WORD = 2;

    static final UriMatcher uriMatcher;
    static{
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "words", WORDS);
        uriMatcher.addURI(PROVIDER_NAME, "words/#", SINGLE_WORD);
    }

    //Related to DB

    private  SQLiteDatabase db;
     static final String DATABASE_NAME= "CustomDictionary";
     static final String TABLE_NAME= "DICTIONARY";
     static final String UID="_id";
    static final String WORD="word";
    static final String MEANING="meaning";
    static final int DATABASE_VERSION=1;
    static final String CREATE_TABLE="CREATE TABLE "+TABLE_NAME+"( _id INTEGER PRIMARY KEY AUTOINCREMENT,"
            +WORD+" VARCHAR(255) , "+MEANING+" VARCHAR(255));";
    static final String DROP_TABLE="DROP TABLE IF EXISTS "+TABLE_NAME;

    private static class DbHelper extends SQLiteOpenHelper {

        private Context con;

        DbHelper(Context context)
        {
            super(context,DATABASE_NAME,null,DATABASE_VERSION);
            con=context;
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase)
        {
            try
            {
                sqLiteDatabase.execSQL(CREATE_TABLE);
                loadDictionary(sqLiteDatabase);
            }
            catch(SQLException e)
            {
                Toast.makeText(con,e.toString(),Toast.LENGTH_SHORT).show();

            }

        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

            try {
                sqLiteDatabase.execSQL(DROP_TABLE);
                onCreate(sqLiteDatabase);
            }
            catch(SQLException e)
            {

                Toast.makeText(con,e.toString(),Toast.LENGTH_SHORT).show();

            }

        }


        /**
         * Starts a thread to load the database table with words
         */
        private void loadDictionary(final SQLiteDatabase sqlite) {
            new Thread(new Runnable() {
                public void run() {
                    try {
                        loadWords(sqlite);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();
        }

        private void loadWords(SQLiteDatabase sqlite) throws IOException {

            final Resources resources = con.getResources();
            InputStream inputStream = resources.openRawResource(R.raw.sample_dictionary);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] strings = TextUtils.split(line, "-");
                    if (strings.length < 2) continue;

                    ContentValues initialValues = new ContentValues();
                    initialValues.put(WORD, strings[0].trim());
                    initialValues.put(MEANING, strings[1].trim());

                    long id=sqlite.insert(TABLE_NAME,null,initialValues);

                    if (id < 0) {
                       //Unable to add word
                    }
                }
            } finally {
                reader.close();
            }

        }

    }


    @Override
    public boolean onCreate() {
        DbHelper dbHelper=new DbHelper(getContext());
        db = dbHelper.getWritableDatabase();
        return (db == null)? false:true;
    }


    //strings==Projection, s=Selection , strings1==selectionArgs , s1==sortOrder
    @Nullable
    @Override
    public Cursor query(Uri uri, String[] strings, String s, String[] strings1, String s1)
    {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TABLE_NAME);

        switch (uriMatcher.match(uri)) {
            case WORDS:
                qb.setProjectionMap(WORDS_PROJECTION_MAP);
                break;

            case SINGLE_WORD:
                qb.appendWhere( UID + "=" + uri.getPathSegments().get(1));
                break;

            default:
        }
        if (s1 == null || s1.equals("")){
            /**
             * By default sort on Words
             */
            s1 = WORD;
        }


        Cursor c = qb.query(db,	strings,	s,
                strings1,null, null, s1);
        /**
         * register to watch a content URI for changes
         */
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)){
            /**
             * Get all student records
             */
            case WORDS:
                return "vnd.android.cursor.dir/words";
            /**
             * Get a particular student
             */
            case SINGLE_WORD:
                return "vnd.android.cursor.item/words";
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues)
    {
        long rowID = db.insert(	TABLE_NAME, "", contentValues);
        /**
         * If record is added successfully
         */
        if (rowID > 0) {
            Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
            getContext().getContentResolver().notifyChange(_uri, null);
            return _uri;
        }

        throw new SQLException("Failed to add a record into " + uri);
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }
















}
