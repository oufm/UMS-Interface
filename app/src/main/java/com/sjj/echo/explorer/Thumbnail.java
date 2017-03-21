package com.sjj.echo.explorer;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.util.Log;
import android.widget.ImageView;

import com.sjj.echo.routine.FileTool;
import com.sjj.echo.umsinterface.R;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantLock;

import static android.provider.MediaStore.Images.Thumbnails.FULL_SCREEN_KIND;
import static android.provider.MediaStore.Images.Thumbnails.MICRO_KIND;
import static android.provider.MediaStore.Images.Thumbnails.MINI_KIND;

/**
 * Created by SJJ on 2016/12/25.
 */
/**
 * provide and set the thumbnail.
 * <b>it use multi-thread to get the thumbnail for image or video and so on</b>
 * */
public class Thumbnail {

    private static String[] videoExtension = {"avi","rmvb","rm","asf","divx","mpg","mpeg","mpe","wmv","mp4","mkv","vob"};
    private static String[] imageExtension = {"pcx","emf","gif","bmp","tga","jpg","tif","jpeg","png","rle"};
    //public Thumbnail(Context context){mContext = context;}
    private Thumbnail(){}
    public static boolean isStringIn(String str,String[] strs)
    {
        int count = strs.length;
        for(int i=0;i<count;i++)
        {
            if(strs[i].equals(str))
                return true;
        }
        return false;
    }
    /**
     * 根据指定的图像路径和大小来获取缩略图
     * 此方法有两点好处：
     *     1. 使用较小的内存空间，第一次获取的bitmap实际上为null，只是为了读取宽度和高度，
     *        第二次读取的bitmap是根据比例压缩过的图像，第三次读取的bitmap是所要的缩略图。
     *     2. 缩略图对于原图像来讲没有拉伸，这里使用了2.2版本的新工具ThumbnailUtils，使
     *        用这个工具生成的图像不会被拉伸。
     * @param imagePath 图像的路径
     * @param width 指定输出图像的宽度
     * @param height 指定输出图像的高度
     * @return 生成的缩略图
     */
    private static Bitmap getImageThumbnail(String imagePath, int width, int height) {
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        // 获取这个图片的宽和高，注意此处的bitmap为null
        bitmap = BitmapFactory.decodeFile(imagePath, options);
        options.inJustDecodeBounds = false; // 设为 false
        // 计算缩放比
        int h = options.outHeight;
        int w = options.outWidth;
        int beWidth = w / width;
        int beHeight = h / height;
        int be = 1;
        if (beWidth < beHeight) {
            be = beWidth;
        } else {
            be = beHeight;
        }
        if (be <= 0) {
            be = 1;
        }
        options.inSampleSize = be;
        // 重新读入图片，读取缩放后的bitmap，注意这次要把options.inJustDecodeBounds 设为 false
        bitmap = BitmapFactory.decodeFile(imagePath, options);
        // 利用ThumbnailUtils来创建缩略图，这里要指定要缩放哪个Bitmap对象
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
                ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        return bitmap;
    }
    private static int mCacheSize = 0;
    public static final int MAX_CACHE_SIZE = 5*1024*1024;
    private static class CacheItem{
        Bitmap mBitmap;
        String mPath;
        int mSize;
        int mItemSize;
        public CacheItem(Bitmap bitmap,String path,int size,int itemSize)
        {
            mBitmap = bitmap;
            mPath = path;
            mSize = size;
            mItemSize = itemSize;
        }
    }

    private static ReentrantLock mCacheLock = new ReentrantLock();
    private static Queue<CacheItem> mCache = new LinkedList<>();
    /**
     * set this field first before set/get thumbnail
     * */
    public static Context mContext;


    private static class DefaultIconItem{
        String[] mExtensions;
        int mRes;
        public DefaultIconItem(String[] extensions,int res)
        {
            mExtensions = extensions;
            mRes = res;
        }
    }

    private static DefaultIconItem[] mDefaultIcons = {
            new DefaultIconItem(new String[]{"apk"} ,R.mipmap.format_apk),
            new DefaultIconItem(new String[]{"xls","xlsx"} , R.mipmap.format_excel),
            new DefaultIconItem(new String[]{"html","htm","mhtml","shtml","asp","xml","xhtml","php","jsp","css"} ,R.mipmap.format_html),
            new DefaultIconItem(videoExtension ,R.mipmap.format_media),
            new DefaultIconItem(imageExtension ,R.mipmap.format_picture),
            new DefaultIconItem(new String[]{"pdf"},R.mipmap.format_pdf),
            new DefaultIconItem(new String[]{"txt"},R.mipmap.format_text),
            new DefaultIconItem(new String[]{"torrent"},R.mipmap.format_torrent),
            new DefaultIconItem(new String[]{"doc","docx"},R.mipmap.format_word),
            new DefaultIconItem(new String[]{"zip","tar","7z","rar","gz","bz"},R.mipmap.format_zip),
            new DefaultIconItem(new String[]{"swf"},R.mipmap.format_flash),
            new DefaultIconItem(new String[]{"ppt","pptx"},R.mipmap.format_ppt),
            new DefaultIconItem(new String[]{"mp3","ogg","wav","ape","cda","au","midi","mac","aac"},R.mipmap.format_music),
    };

    /**
     * get a bitmap from a drawable
     * */
    public static Bitmap drawableToBitamp(Drawable drawable)
    {
        Bitmap bitmap;
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        Bitmap.Config config =
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                        : Bitmap.Config.RGB_565;
        bitmap = Bitmap.createBitmap(w,h,config);
        //注意，下面三行代码要用到，否在在View或者surfaceview里的canvas.drawBitmap会看不到图
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);
        return bitmap;
    }


    /**
     * get the drawable from the path of the apk <br/>
	 * 采用了新的办法获取APK图标，之前的失败是因为android中存在的一个BUG,通过
	 * appInfo.publicSourceDir = apkPath;来修正这个问题，详情参见:
	 * http://code.google.com/p/android/issues/detail?id=9151
	 **/
    public static Drawable getApkIcon(Context context, String apkPath) {
        PackageManager pm = context.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(apkPath,
                PackageManager.GET_ACTIVITIES);
        if (info != null) {
            ApplicationInfo appInfo = info.applicationInfo;
            appInfo.sourceDir = apkPath;
            appInfo.publicSourceDir = apkPath;
            try {
                return appInfo.loadIcon(pm);
            } catch (OutOfMemoryError e) {
                Log.e("ApkIconLoader", e.toString());
            }
        }
        return null;
    }

    /**
     * get the thumbnail for image video and so on
     * <b>note that it will take much mTime and block</b>
     * */
    public static Bitmap getThumbnail(String path,int size)
    {
        String extensionString = FileTool.getExtension(path);
        Bitmap thumbnailBitmap = null;
        if(isStringIn(extensionString,videoExtension))
        {
            thumbnailBitmap = ThumbnailUtils.createVideoThumbnail(path,size);
        }
        if(isStringIn(extensionString,imageExtension))
        {
            int width=0,height=0;
            switch (size)
            {
                case MICRO_KIND:
                    width = 96;
                    height = 96;
                    break;
                case MINI_KIND:
                    width = 512;
                    height = 384;
                    break;
                case FULL_SCREEN_KIND:
                    width = 700;
                    height = 1200;
                    break;
            }
            thumbnailBitmap = getImageThumbnail(path,width,height);
        }
        if(extensionString.equalsIgnoreCase("apk"))
        {
            if(mContext!=null) {
                Drawable drawable = getApkIcon(mContext, path);
                if(drawable!=null)
                {
                    thumbnailBitmap = drawableToBitamp(drawable);
                }
            }
        }
        return thumbnailBitmap;
    }

    private static ThumbnailThread[] mThreads = new ThumbnailThread[3];
    static {
        int count = mThreads.length;
        for(int i=0;i<count;i++)
        {
            mThreads[i] = new ThumbnailThread();
        }
    }
    /**
     * set the thumbnail according to the file.
     * it will first search from the cache,
     * if not shot, it will set the default icon first and
     * send the task to work thread to get the definite thumbnail
     * */
    public static void setThumbnail(final String path, final int size, ImageView imageView,boolean isDir)
    {

        for(ThumbnailThread thumbnailThread :mThreads) {
            Iterator<ThumbnailThread.ThumbnailTask> iterator = thumbnailThread.mTaskQueue.iterator();
            if (thumbnailThread.mTaskDoing != null && thumbnailThread.mTaskDoing.mImageView.equals(imageView))
                thumbnailThread.mTaskDoing.mVisibility = false;
            synchronized (mThreads) {
                while (iterator.hasNext()) {
                    ThumbnailThread.ThumbnailTask thumbnailTask = iterator.next();
                    if (thumbnailTask.mImageView.equals(imageView)) {
                        iterator.remove();
                    }
                }
            }
        }

        if(isDir)
        {
            imageView.setImageResource(R.mipmap.ic_folder_gray_48dp);
            //imageView.setImageResource(R.mipmap.format_folder);
        }else
        {
            Bitmap bitmap = null;
            mCacheLock.lock();
            for(CacheItem cacheItem:mCache)
            {
                if(cacheItem.mPath.equals(path)&&cacheItem.mSize==size)
                {
                    bitmap = cacheItem.mBitmap;
                    break;
                }
            }
            mCacheLock.unlock();

            if(bitmap!=null)
                imageView.setImageBitmap(bitmap);
            else
            {
                //int res = R.mipmap.ic_file_gray_48dp;
                int res = R.mipmap.format_unkown;
                String extension = FileTool.getExtension(path);
                for(DefaultIconItem icon:mDefaultIcons)
                {
                    boolean match = false;
                    for(String str:icon.mExtensions)
                    {
                        if(str.equalsIgnoreCase(extension))
                        {
                            match = true;
                            break;
                        }
                    }
                    if(match)
                    {
                        res = icon.mRes;
                    }
                }
                imageView.setImageResource(res);
                ThumbnailThread idlerThread = null;
                int minTask = Integer.MAX_VALUE;
                for(ThumbnailThread thread:mThreads)
                {
                    int taskNum = thread.getTaskNum();
                    if(taskNum<minTask)
                    {
                        idlerThread = thread;
                        minTask = taskNum;
                    }
                }
                idlerThread.addTask(path,size,imageView);
            }
        }

    }
    private static class ThumbnailThread implements Runnable
    {
        class ThumbnailTask
        {
            String mPath;
            int mSize;
            ImageView mImageView;
            boolean mVisibility = true;
            public ThumbnailTask(String path,int size,ImageView imageView)
            {
                mPath = path;
                mSize = size;
                mImageView = imageView;
            }
        }
        private ThumbnailTask mTaskDoing;
        private Thread mThread;
        private Queue<ThumbnailTask> mTaskQueue = new LinkedList<>();
        public int getTaskNum()
        {
            return mTaskQueue.size();
        }
        public static int getBitmapSize(Bitmap bitmap)
        {
            int size = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                size = bitmap.getAllocationByteCount();
            }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1)
            {
                size = bitmap.getByteCount();
            }else
            {
                size = bitmap.getWidth()*bitmap.getHeight()*3;
            }
            return size;
        }

        @Override
        public void run() {
            while(true)
            {

                ThumbnailTask thumbnailTask = null;
                synchronized (this) {//在此段间隙中,可能poll==null后,add才执行,并在"mThread = null"前错误判断"mThread != null",导致不启动线程
                    if(mTaskQueue.size()==0)
                    {
                        mThread = null;
                        return;
                    }
                    thumbnailTask = mTaskQueue.poll();
                    mTaskDoing = thumbnailTask;
                    if (thumbnailTask == null)
                    {
                        mThread = null;
                        return;
                    }
                }

                Bitmap bitmap = null;

                if(bitmap == null) {
                    bitmap = getThumbnail(thumbnailTask.mPath, thumbnailTask.mSize);
                    if(bitmap!=null) {
                        int itemSize = getBitmapSize(bitmap);
                        mCacheLock.lock();
                        mCache.add(new CacheItem(bitmap, thumbnailTask.mPath, thumbnailTask.mSize, itemSize));
                        mCacheSize += itemSize;
                        while (mCacheSize > MAX_CACHE_SIZE) {
                            CacheItem _cacheItem = mCache.poll();
                            if (_cacheItem == null)
                                break;
                            mCacheSize -= _cacheItem.mItemSize;
                        }
                        mCacheLock.unlock();
                    }

                }
                if (bitmap != null&&thumbnailTask.mVisibility) {
                    final ImageView imageView = thumbnailTask.mImageView;
                    final Bitmap finalBitmap = bitmap;
                    imageView.post(new Runnable() {
                        @Override
                        public void run() {
                            imageView.setImageBitmap(finalBitmap);
                        }
                    });
                }
            }

        }

        protected void addTask(String path,int size,ImageView imageView)
        {
            synchronized (this) {
                mTaskQueue.add(new ThumbnailTask(path, size, imageView));
                if (mThread == null) {
                    mThread = new Thread(this);
                    mThread.start();
                }
            }

        }
    }

}
