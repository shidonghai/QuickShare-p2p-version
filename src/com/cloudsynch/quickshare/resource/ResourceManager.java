package com.cloudsynch.quickshare.resource;

import static com.cloudsynch.quickshare.resource.module.ResourceCategory.Category.APK;
import static com.cloudsynch.quickshare.resource.module.ResourceCategory.Category.AUDIO;
import static com.cloudsynch.quickshare.resource.module.ResourceCategory.Category.DOC;
import static com.cloudsynch.quickshare.resource.module.ResourceCategory.Category.PHOTO;
import static com.cloudsynch.quickshare.resource.module.ResourceCategory.Category.VIDEO;
import static com.cloudsynch.quickshare.resource.module.ResourceCategory.Category.ZIP;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;

import com.cloudsynch.quickshare.QuickShareApplication;
import com.cloudsynch.quickshare.R;
import com.cloudsynch.quickshare.resource.logic.IContentChanged;
import com.cloudsynch.quickshare.resource.module.FileItem;
import com.cloudsynch.quickshare.resource.module.ResourceCategory;
import com.cloudsynch.quickshare.resource.ui.ResourceFileAdapter;
import com.cloudsynch.quickshare.utils.FileUtil;
import com.cloudsynch.quickshare.utils.TimeUtils;
import com.cloudsynch.quickshare.utils.thumbnail.ImageWorker;

/**
 * Created by Xiaohu on 13-5-28.
 */
public class ResourceManager {

    public static String RESOURCE_CATEGORY = "resource_category";

    public static String ZipFileMimeType = "application/zip";

    public static HashSet<String> sDocMimeTypesSet = new HashSet<String>() {
        {
            add("text/plain");
            add("text/plain");
            add("application/pdf");
            add("application/msword");
            add("application/vnd.ms-excel");
            add("application/vnd.ms-excel");
        }
    };

    private ImageWorker mWorker;

    private Map<ResourceCategory.Category, ResourceCategory> mResourceCategoryMap = new HashMap<ResourceCategory.Category, ResourceCategory>();

    private static ResourceManager mInstance;

    private List<IContentChanged> mListener = new ArrayList<IContentChanged>();

    private ResourceManager(Context context) {
        mWorker = ((QuickShareApplication) context.getApplicationContext())
                .getImageWorker();
    }

    public static ResourceManager getInstance(Context context) {
        if (null == mInstance) {
            mInstance = new ResourceManager(context);
        }

        return mInstance;
    }

    public void initResource(Context context,
                             ResourceCategory category) {
        if (null == context || null == category) {
            return;
        }

        int count = 0;
        long size = 0;
        String selection = null;
        Uri uri = null;
        switch (category.category) {
            case VIDEO:
                uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                selection = MediaStore.Audio.Media.SIZE + " >0";
                break;
            case AUDIO:
                uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                selection = MediaStore.Audio.Media.SIZE + " >0";
                break;
            case PHOTO:
                uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                selection = MediaStore.Audio.Media.SIZE + " >0";
                break;
            case APK:
                if (Build.VERSION.SDK_INT > 11) {
                    uri = MediaStore.Files.getContentUri("external");
                    selection = MediaStore.Files.FileColumns.DATA + " LIKE '%.apk' and " +
                            MediaStore.Files.FileColumns.SIZE + " > 0";
                }
                break;
            case DOC:
                if (Build.VERSION.SDK_INT > 11) {
                    uri = MediaStore.Files.getContentUri("external");
                    selection = buildDocSelection();
                }
                break;
            case QUICK_SHARE:
                String home = Environment.getExternalStorageDirectory()
                        .getAbsolutePath() + "/quickshare";
                scanFolder(home, category);
                return;
            case BLUETOOTH:
                String path = Environment.getExternalStorageDirectory()
                        .getAbsolutePath() + "/Bluetooth";
                scanFolder(path, category);
                return;
            case ZIP:
                if (Build.VERSION.SDK_INT > 11) {
                    uri = MediaStore.Files.getContentUri("external");
                    selection = "(" + MediaStore.Files.FileColumns.MIME_TYPE
                            + " == '" + ZipFileMimeType + "') and " +
                            MediaStore.Files.FileColumns.SIZE + " > 0";
                }
                break;
            default:
                break;
        }
        if (null != uri) {
            String[] columns = new String[]{"COUNT(*)", "SUM(_size)"};

            Cursor cursor = context.getContentResolver().query(uri, columns,
                    selection, null, null);
            if (null != cursor && cursor.moveToFirst()) {
                try {
                    count = cursor.getInt(0);
                    size = cursor.getLong(1);
                } finally {
                    cursor.close();
                }
            }
        }

        category.count = count;
        category.size = size;

    }

    public static long getSDAvailableSize(File path) {
        // File path = Environment.getExternalStorageDirectory();
        // File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return availableBlocks * blockSize;
    }

    public static long getSDAllSize(File path) {
        // File path = Environment.getExternalStorageDirectory();
        // File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getBlockCount();
        return availableBlocks * blockSize;
    }

    public static String buildDocSelection() {
        StringBuilder selection = new StringBuilder();
        Iterator<String> iter = sDocMimeTypesSet.iterator();
        while (iter.hasNext()) {
            selection.append("(" + MediaStore.Files.FileColumns.MIME_TYPE
                    + "=='" + iter.next() + "') OR ");
        }
        return selection.substring(0, selection.lastIndexOf(")") + 1);
    }

    private static int scanFolder(String path, ResourceCategory category) {
        if (TextUtils.isEmpty(path)) {
            return 0;
        }

        File file = new File(path);
        if (null == file || !file.exists()) {
            return 0;
        }
        List<File> list = new ArrayList<File>();
        category.files.clear();
        category.size = 0;
        category.count = 0;

        scan(file, category);
        return list.size();
    }

    private static void scan(File file, List<File> list) {
        File[] files = file.listFiles();

        for (File f : files) {
            if (f.isDirectory()) {
                scan(f, list);
            } else {
                list.add(f);
            }
        }
    }

    private static void scan(File file, ResourceCategory category) {
        File[] files = file.listFiles();

        for (File f : files) {
            if (f.isDirectory()) {
                scan(f, category);
            } else {
                if (f.length() > 0) {
                    category.count = category.count + 1;
                    category.size = category.size + f.length();
                    category.files.add(new FileItem(-1, false, f));
                }
            }
        }
    }

    public void bindView(View view, Cursor cursor, ResourceCategory category) {
        if (null == view || null == cursor || null == category) {
            return;
        }
        ImageView image = (ImageView) view.findViewById(R.id.image);
        TextView name = (TextView) view.findViewById(R.id.name);
        TextView size = (TextView) view.findViewById(R.id.size);
        ImageView playIcon = (ImageView) view.findViewById(R.id.play_icon);
        ImageView translucent = (ImageView) view.findViewById(R.id.translucent);
        translucent.setAlpha(100);
        String data = cursor.getString(cursor
                .getColumnIndex(MediaStore.Audio.Media.DATA));
        File file = new File(data);
        name.setText(file.exists() ? file.getName() : cursor.getString(cursor
                .getColumnIndex(MediaStore.Audio.Media.TITLE)));
        if (VIDEO == category.category) {
            size.setText(TimeUtils.parseSec(cursor.getInt(cursor.
                    getColumnIndex(MediaStore.Audio.Media.DURATION))));
            playIcon.setVisibility(View.VISIBLE);
        } else {
            size.setText(FileUtil.formatFromByte(cursor.getLong(cursor
                    .getColumnIndex(MediaStore.Audio.Media.SIZE))));
            playIcon.setVisibility(View.GONE);
        }

        getThumbnail(image, data);
    }

    public void bindView(ResourceFileAdapter.ViewHolder holder, File file) {
        holder.name.setText(file.getName());
        holder.size.setText(FileUtil.formatFromByte(file.length()));
        holder.date.setText(FileUtil.getLastModifyDateString(file));

        ResourceCategory.Category category = getFileType(file.getAbsolutePath());

        switch (category) {
            case AUDIO:
                holder.icon.setImageResource(R.drawable.category_music_icon);
                break;
            case DOC:
                holder.icon.setImageResource(R.drawable.category_file_icon);
                break;
            case ZIP:
                holder.icon.setImageResource(R.drawable.category_compress_icon);
                break;
            case APK:
                mWorker.loadImage(file.getAbsolutePath(), holder.icon, BitmapFactory
                        .decodeResource(holder.icon.getContext().getResources(),
                                R.drawable.category_install_icon), mAppIconLoadMethod);
                break;
            case PHOTO:
                mWorker.loadImage(file.getAbsolutePath(), holder.icon, BitmapFactory
                        .decodeResource(holder.icon.getContext().getResources(),
                                R.drawable.photo_default_icon), mPicLoader);
                break;
            case VIDEO:
                mWorker.loadImage(
                        queryVideoId(holder.icon.getContext(), file.getAbsolutePath()), holder.icon,
                        BitmapFactory.decodeResource(holder.icon.getContext()
                                .getResources(), R.drawable.video_default_icon),
                        mVideoLoadMethod);
                break;
            default:
                break;
        }
    }

    public void registerListener(IContentChanged contentChanged) {
        if (!mListener.contains(contentChanged)) {
            mListener.add(contentChanged);
        }
    }

    public void removeListener(IContentChanged contentChanged) {
        if (mListener.contains(contentChanged)) {
            mListener.remove(contentChanged);
        }
    }

    public void notifyContentChanged() {
        for (IContentChanged contentChanged : mListener) {
            contentChanged.onContentChanged();
        }
    }

    private ResourceCategory.Category getFileType(String path) {
        ResourceCategory.Category category;
        String type = getMimeType(path);
        if (null != type) {
            if (type.startsWith("image")) {
                category = PHOTO;
            } else if (type.startsWith("audio")) {
                category = AUDIO;
            } else if (type.startsWith("video")) {
                category = VIDEO;
            } else if (type.startsWith("application/zip")) {
                category = ZIP;
            } else if (type
                    .startsWith("application/vnd.android.package-archive")) {
                category = APK;
            } else {
                category = DOC;
            }
        } else {
            category = DOC;
        }

        return category;
    }

    public String getMimeType(String url) {
        if (null == url) {
            return null;
        }

        String type = null;
        int index = url.lastIndexOf(".");
        if (index > 0) {
            String extension = url.substring(index + 1);
            if (extension != null) {
                MimeTypeMap mime = MimeTypeMap.getSingleton();
                type = mime.getMimeTypeFromExtension(extension.toLowerCase());
            }

        }

        return type;
    }

    private void bindImageView(ImageView image, Cursor cursor) {
        mWorker.loadImage(cursor.getString(cursor
                .getColumnIndex(MediaStore.Audio.Media.DATA)), image,
                BitmapFactory.decodeResource(image.getContext().getResources(),
                        R.drawable.photo_default_icon), mPicLoader);

    }

    private ImageWorker.LoadMethod mPicLoader = new ImageWorker.LoadMethod() {
        @Override
        public Bitmap processBitmap(Object obj, Context context) {
            String path = (String) obj;
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, options);

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, 100, 100);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            options.outWidth = 40;
            options.outHeight = 40;
            return BitmapFactory.decodeFile(path, options);
        }
    };

    private ImageWorker.LoadMethod mVideoLoadMethod = new ImageWorker.LoadMethod() {

        @Override
        public Bitmap processBitmap(Object obj, Context context) {
            Long id = (Long) obj;
            if (id > 0) {
                Bitmap bitmap = MediaStore.Video.Thumbnails.getThumbnail(
                        context.getContentResolver(), id,
                        MediaStore.Video.Thumbnails.MICRO_KIND, null);
                return bitmap;
            }
            return null;
        }
    };

    private ImageWorker.LoadMethod mAppIconLoadMethod = new ImageWorker.LoadMethod() {

        @Override
        public Bitmap processBitmap(Object obj, Context context) {
            String path = (String) obj;
            Drawable drawable = loadApkFileIcon(context, path);
            if (null != drawable) {
                return ((BitmapDrawable) drawable).getBitmap();
            }
            return null;
        }
    };

    public int calculateInSampleSize(BitmapFactory.Options options,
                                     int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and
            // width
            final int heightRatio = Math.round((float) height
                    / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will
            // guarantee a final image
            // with both dimensions larger than or equal to the requested height
            // and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;

            // This offers some additional logic in case the image has a strange
            // aspect ratio. For example, a panorama may have a much larger
            // width than height. In these cases the total pixels might still
            // end up being too large to fit comfortably in memory, so we should
            // be more aggressive with sample down the image (=larger
            // inSampleSize).

            final float totalPixels = width * height;

            // Anything more than 2x the requested pixels we'll sample down
            // further
            final float totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                inSampleSize++;
            }
        }
        return inSampleSize;
    }

    private long queryVideoId(Context context, String path) {
        if (null != path) {
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        new String[]{MediaStore.MediaColumns._ID},
                        MediaStore.Video.Media.DATA + "=?", new String[]{path},
                        null);
                if (null != cursor && cursor.moveToFirst()) {
                    long id = cursor.getLong(cursor
                            .getColumnIndex(MediaStore.Video.Media._ID));
                    cursor.close();
                    return id;
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (null != cursor) {
                    cursor.close();
                }
            }
        }
        return -1;
    }

    private Drawable loadApkFileIcon(Context ctx, String filePath) {
        PackageManager pm = ctx.getPackageManager();
        PackageInfo pInfo = pm.getPackageArchiveInfo(filePath, PackageManager.GET_ACTIVITIES);
        Drawable icon = null;
        if (pInfo != null) {
            ApplicationInfo aInfo = pInfo.applicationInfo;
            if (aInfo != null) {
                aInfo.publicSourceDir = filePath;
                aInfo.sourceDir = filePath;
                icon = aInfo.loadIcon(pm);
            }
        }
        return icon;
    }

    public void loadAppIcon(String path, ImageView view) {
        mWorker.loadImage(path, view, BitmapFactory.decodeResource(
                view.getContext().getResources(), R.drawable.category_install_icon),
                mAppIconLoadMethod);
    }

    public void getThumbnail(ImageView icon, String path) {
        ResourceCategory.Category category = getFileType(path);

        switch (category) {
            case AUDIO:
                icon.setImageResource(R.drawable.category_music_icon);
                break;
            case DOC:
                icon.setImageResource(R.drawable.category_file_icon);
                break;
            case ZIP:
                icon.setImageResource(R.drawable.category_compress_icon);
                break;
            case APK:
                mWorker.loadImage(path, icon, BitmapFactory
                        .decodeResource(icon.getContext().getResources(),
                                R.drawable.category_install_icon), mAppIconLoadMethod);
                break;
            case PHOTO:
                mWorker.loadImage(path, icon, BitmapFactory
                        .decodeResource(icon.getContext().getResources(),
                                R.drawable.photo_default_icon), mPicLoader);
                break;
            case VIDEO:
                mWorker.loadImage(
                        queryVideoId(icon.getContext(), path), icon,
                        BitmapFactory.decodeResource(icon.getContext()
                                .getResources(), R.drawable.video_default_icon),
                        mVideoLoadMethod);
                break;
            default:
                break;
        }
    }

}
