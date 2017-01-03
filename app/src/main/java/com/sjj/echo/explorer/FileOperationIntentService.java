package com.sjj.echo.explorer;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class FileOperationIntentService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_MOVE = "com.sjj.echo.explorer.action.MOVE";
    private static final String ACTION_DELETE = "com.sjj.echo.explorer.action.DELETE";
    private static final String ACTION_COPY = "com.sjj.echo.explorer.action.COPY";
    private static final String ACTION_ZIP = "com.sjj.echo.explorer.action.ZIP";
    private static final String ACTION_TAR = "com.sjj.echo.explorer.action.TAR";


    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "com.sjj.echo.explorer.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "com.sjj.echo.explorer.extra.PARAM2";

    public FileOperationIntentService() {
        super("FileOperationIntentService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startAction(String actionName ,Context context, String param1, String param2,ServiceConnection serviceConnection) {
        Intent intent = new Intent(context, FileOperationIntentService.class);
        intent.setAction(actionName);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        //context.startService(intent);
        context.bindService(intent,serviceConnection , Context.BIND_AUTO_CREATE);
    }

    boolean mCancel = false;
    boolean mPause = false;
    boolean mResume = false;

    public static void startActionTar(Context context,ServiceConnection serviceConnection, String param1, String param2)
    {
        startAction(ACTION_TAR,context,param1,param2,serviceConnection);
    }
    public static void startActionZip(Context context,ServiceConnection serviceConnection, String param1, String param2)
    {
        startAction(ACTION_ZIP,context,param1,param2,serviceConnection);
    }
    public static void startActionMove(Context context,ServiceConnection serviceConnection, String param1, String param2)
    {
        startAction(ACTION_MOVE,context,param1,param2,serviceConnection);
    }
    public static void startActionCopy(Context context,ServiceConnection serviceConnection, String param1, String param2)
    {
        startAction(ACTION_COPY,context,param1,param2,serviceConnection);
    }
    public static void startActionDelete(Context context,ServiceConnection serviceConnection, String param1, String param2)
    {
        startAction(ACTION_DELETE,context,param1,param2,serviceConnection);
    }

    private FileOperationBinder mBinder = new FileOperationBinder();

    public class FileOperationBinder extends Binder
    {
        void pauseOperation()
        {
            mPause = true;
        }
        void resumeOperation()
        {
            mResume = true;
        }
        void cancelOperation()
        {
            mCancel = true;
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_MOVE.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionMove(param1, param2);
            } else if (ACTION_DELETE.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionDelete(param1, param2);
            }
            else if (ACTION_COPY.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionCopy(param1, param2);
            }
            else if (ACTION_TAR.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionTar(param1, param2);
            }
            else if (ACTION_ZIP.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionZip(param1, param2);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionRename(String param1, String param2) {
        // TODO: Handle action Foo
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionDelete(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void handleActionCopy(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void handleActionMove(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void handleActionTar(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void handleActionZip(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }

}
