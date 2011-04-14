package ch.amana.android.cputuner.helper;

import java.io.File;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import ch.almana.android.backupDb.ExportDataTask;
import ch.amana.android.cputuner.provider.db.DB;
import ch.amana.android.cputuner.provider.db.DB.OpenHelper;

public class BackupRestoreHelper {

	public static void backup(Context ctx, File storagePath) {
		if (!storagePath.isDirectory()) {
			storagePath.mkdir();
		}
		SQLiteDatabase db = new OpenHelper(ctx).getWritableDatabase();
		ExportDataTask exportDataTask = new ExportDataTask(ctx, db, storagePath, ExportDataTask.ExportType.JSON);
		exportDataTask.execute(new String[] { DB.DATABASE_NAME });
	}

	public static File getStoragePath(Context ctx, String directroy) {
		return new File(Environment.getExternalStorageDirectory(), ctx.getPackageName() + "/" + directroy);
	}

}
