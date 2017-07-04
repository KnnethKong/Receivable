package gjcm.kxf.provide;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by kxf on 2017/6/30.
 */
public class MyDBHELP extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "huifu.db";

    public MyDBHELP(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public MyDBHELP(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public MyDBHELP(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_CONTACT_TABLE = "CREATE TABLE " + ContentTract.MyConEntry.TABLE_NAME + "( "
                + ContentTract.MyConEntry._ID + " TEXT PRIMARY KEY, "
                + ContentTract.MyConEntry.COLUMN_NAME + " TEXT NOT NULL,"
                + ContentTract.MyConEntry.COLUMN_SDRESS + " TEXT NOT NULL," +ContentTract.MyConEntry.COLUMN_ADRESS +  " TEXT NOT NULL );";

        db.execSQL(SQL_CREATE_CONTACT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ContentTract.MyConEntry.TABLE_NAME);
        onCreate(db);
    }
}
