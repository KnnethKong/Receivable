package gjcm.kxf.provide;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;

/**
 * Created by kxf on 2017/6/30.
 */
public class MyContentProvider extends ContentProvider {
    private MyDBHELP myDBHELP;

    @Override
    public boolean onCreate() {
        myDBHELP = new MyDBHELP(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final SQLiteDatabase db = myDBHELP.getReadableDatabase();

        Cursor cursor = null;
        switch (buildUriMatcher().match(uri)) {
            case TEST:
                cursor = db.query(ContentTract.MyConEntry.TABLE_NAME, projection, selection, selectionArgs, sortOrder, null, null);
                break;
        }

        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = myDBHELP.getWritableDatabase();
        Uri returnUri;
        long _id;
        switch (buildUriMatcher().match(uri)) {
            case TEST:
                _id = db.insert(ContentTract.MyConEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = ContentTract.MyConEntry.buildUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            default:
                throw new android.database.SQLException("Unknown uri: " + uri);
        }
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        final SQLiteDatabase db = myDBHELP.getWritableDatabase();
        return db.delete(ContentTract.MyConEntry.TABLE_NAME, s, strings);
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        final SQLiteDatabase db = myDBHELP.getWritableDatabase();
        return db.update(ContentTract.MyConEntry.TABLE_NAME, contentValues, s, strings);
    }

    private final static int TEST = 100;

    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = ContentTract.CONTENT_AUTHORITY;
        matcher.addURI(authority, ContentTract.PATH_TEST, TEST);

        return matcher;
    }
}
