package gjcm.kxf.provide;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by kxf on 2017/6/30.
 */
public class ContentTract {

    protected static final String CONTENT_AUTHORITY = "gjcm.kxf.huifucenter";
    protected static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    protected static final String PATH_TEST = "huifu";

    public static final class MyConEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_TEST).build();

        protected static Uri buildUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        protected static final String TABLE_NAME = "gjcmprint";

        public static final String COLUMN_NAME = "bname";
        public static final String COLUMN_ADRESS = "badress";//print
        public static final String COLUMN_SDRESS = "sadress";//sound
    }
}
