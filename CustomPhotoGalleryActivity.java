package com.naturefinder.classes;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Toast;

import com.naturefinder.naturefinder.R;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class CustomPhotoGalleryActivity extends AppCompatActivity {

    private RecyclerView grdImages;
    private Button btnSelect;
    private ImageView imagePreview;
    private ArrayList <thumbnailClass> thumbnailClassList  = new ArrayList<>();

    private ImageAdapter1 imageAdapter;
    private ArrayList<Integer> thumbnailsSelectionList = new ArrayList<>();
    private int count;
    public final static int PICK_PHOTO_CODE = 1046;
    String profileEdit;
    private static int imagePreviewId = -1;
    private static SparseIntArray hashMap = new SparseIntArray();
    final String orderBy = MediaStore.Images.Media.DATE_TAKEN;
    private int limit;
    private SparseBooleanArray loading = new SparseBooleanArray();


    /**
     * Overrides methods
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_photo_gallery);
        Utils.isUpdated(this);

        Intent intent = getIntent();
        profileEdit = intent.getStringExtra("profile_edit");
        if(profileEdit!=null){
            limit=1;
        } else{
            limit=5;
        }

        grdImages= findViewById(R.id.grdImages);
        grdImages.setLayoutManager(new GridLayoutManager(this, 4));
        btnSelect= (Button) findViewById(R.id.btnSelect);
        imagePreview=(ImageView)findViewById(R.id.image_preview);

        final String[] columns = { MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID };
        Cursor imagecursor = this.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, null, orderBy  + " DESC");
        int image_column_index = imagecursor.getColumnIndex(MediaStore.Images.Media._ID);
        this.count = imagecursor.getCount();

        for (int i = 0; i < this.count; i++) {
            thumbnailClass thumb = new thumbnailClass();
            imagecursor.moveToPosition(i);
            thumb.ids = imagecursor.getInt(image_column_index);
            int dataColumnIndex = imagecursor.getColumnIndex(MediaStore.Images.Media.DATA);
            thumb.arrPath = imagecursor.getString(dataColumnIndex);
            thumbnailClassList.add(thumb);
        }

        imageAdapter = new ImageAdapter1(this, thumbnailClassList);
        grdImages.setAdapter(imageAdapter);

        imagecursor.close();
        btnSelect.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                String selectImages = "";
                String selectImagesIds = "";
                for (int selection: thumbnailsSelectionList) {
                    selectImages = selectImages + thumbnailClassList.get(selection).getArrPath() + "|";
                    selectImagesIds = selectImagesIds + thumbnailClassList.get(selection).getIds() + "|";
                }
                if (thumbnailsSelectionList.size()<1) {
                    Toast.makeText(getApplicationContext(), "Please select at least one image", Toast.LENGTH_LONG).show();
                } else if(profileEdit!=null) {
                    Intent i = new Intent();
                    i.putExtra("data", selectImages);
                    setResult(RESULT_OK, i);
                    finish();
                } else {
                    Log.d("SelectedImages", selectImages);
                    Intent i = new Intent(CustomPhotoGalleryActivity.this, PicSummary.class);
                    i.putExtra("data", selectImages);
                    i.putExtra("ids", selectImagesIds);
                    startActivityForResult(i, PICK_PHOTO_CODE);
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("----customPhotoGallery1", "--------------");
        if (resultCode == RESULT_OK) {
            Log.d("----customPhotoGallery2", "--------------");
            if (requestCode == PICK_PHOTO_CODE) {
                Log.d("----customPhotoGallery3", "--------------");

                Intent i = new Intent();
                String selectImages = data.getStringExtra("data");
                String summary = data.getStringExtra("summary");
                String selectImagesIds = data.getStringExtra("ids");
                i.putExtra("data", selectImages);
                i.putExtra("summary", summary);
                i.putExtra("ids", selectImagesIds);

                setResult(RESULT_OK, i);
                    finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_CANCELED);
        super.onBackPressed();
    }

    @Override
    protected void onResume()    {
        super.onResume();
        Utils.isUpdated(this);
    }

    /**
     * Class method
     */



    private void setBitmap(final ImageAdapter1.ViewHolder holder, final int holderPos, final int id, final String path) {
        BackgroundStuff backgroundStuff = new BackgroundStuff();
        backgroundStuff.setHolder(holder);
        backgroundStuff.setHolderPos(holderPos);
        backgroundStuff.setId(id);
        backgroundStuff.setPath(path);
        
         new bitmap1(CustomPhotoGalleryActivity.this).execute(backgroundStuff);
    }

    private static class bitmap1 extends AsyncTask<BackgroundStuff, Void, BackgroundStuff> {
        WeakReference<CustomPhotoGalleryActivity> activityReference;

        // only retain a weak reference to the activity
        bitmap1(CustomPhotoGalleryActivity context) {
            activityReference = new WeakReference<>(context);
        }
        @Override
        protected BackgroundStuff doInBackground(BackgroundStuff... paths) {
            CustomPhotoGalleryActivity activity = activityReference.get();

            if(hashMap.get(paths[0].getHolder().hashCode())!=(paths[0].getHolderPos())){
                return null;
            }
            Bitmap bitmap =  MediaStore.Images.Thumbnails.getThumbnail(activity.getApplicationContext().getContentResolver(), paths[0].getId(), MediaStore.Images.Thumbnails.MICRO_KIND, null);
            try {
                paths[0].setBitmap( modifyOrientation(bitmap, paths[0].getPath()));
                return paths[0];
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;

        }
        @Override
        protected void onPostExecute(BackgroundStuff result) {
            super.onPostExecute(result);
            CustomPhotoGalleryActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;

            if(result!=null&&hashMap.get(result.getHolder().hashCode())==(result.getHolderPos())){
                Log.v("-----hash1","-------"+String.valueOf(result.getHolder().hashCode()));
                Log.v("-----hash2","-------"+result.getHolderPos());
                result.getHolder().imgThumb.setImageBitmap(result.getBitmap());
            } else if(result!=null){
                Log.v("-----hash3","-------"+String.valueOf(result.getHolder().hashCode()));
                Log.v("-----hash4","-------"+result.getHolderPos());
            }
        }
    }
    private void imagePreview1(final ImageView ip,final int id, final String path){
        BackgroundStuff backgroundStuff = new BackgroundStuff();
        backgroundStuff.setView(ip);
        backgroundStuff.setId(id);
        backgroundStuff.setPath(path);
        loading.put(id, true);
        
        new ip2(CustomPhotoGalleryActivity.this).execute(backgroundStuff);
        new ip1(CustomPhotoGalleryActivity.this).execute(backgroundStuff);

    }
    private static class ip1 extends AsyncTask<BackgroundStuff, Void, BackgroundStuff>{
        WeakReference<CustomPhotoGalleryActivity> activityReference;

        // only retain a weak reference to the activity
        ip1(CustomPhotoGalleryActivity context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected BackgroundStuff doInBackground(BackgroundStuff... farts) {

            CustomPhotoGalleryActivity activity = activityReference.get();

            if(imagePreviewId!=farts[0].getId()){
                return null;
            }
            
            Uri uri = MediaStore.Images.Media.getContentUri(farts[0].getPath());
            try {
                Log.v("---ipCheck4 "+uri,"4 "+farts[0].getPath());
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(activity.getApplicationContext().getContentResolver(), Uri.fromFile(new File(farts[0].getPath())));
                farts[0].setBitmap(modifyOrientation(bitmap, farts[0].getPath()));
                return farts[0];
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        @Override
        protected void onPostExecute(BackgroundStuff reoreinted) {
            super.onPostExecute(reoreinted);
            CustomPhotoGalleryActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;

            Log.v("---imagePreviewId ","-------------------------- "+imagePreviewId);
            if(reoreinted!=null&&imagePreviewId==reoreinted.getId()&activity.thumbnailsSelectionList.size()>0){
                Log.v("---id ","---------------------------- "+reoreinted.getId());
                activity.loading.put(reoreinted.getId(), false);
                reoreinted.getView().animate().alpha(1);
                reoreinted.getView().setImageBitmap(reoreinted.getBitmap());
            }
        }

    }
    private static class ip2 extends AsyncTask<BackgroundStuff, Void, BackgroundStuff>{
        WeakReference<CustomPhotoGalleryActivity> activityReference;

        // only retain a weak reference to the activity
        ip2(CustomPhotoGalleryActivity context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected BackgroundStuff doInBackground(BackgroundStuff... farts) {

            CustomPhotoGalleryActivity activity = activityReference.get();

            if(imagePreviewId!=farts[0].getId()){
                return null;
            }

            try {
//                Log.v("---ipCheck4 "+uri,"4 "+farts[0].getPath());
                Bitmap bitmap =  MediaStore.Images.Thumbnails.getThumbnail(activity.getApplicationContext().getContentResolver(), farts[0].getId(), MediaStore.Images.Thumbnails.MINI_KIND, null);
                farts[0].setBitmap(modifyOrientation(bitmap, farts[0].getPath()));
                return farts[0];
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        @Override
        protected void onPostExecute(BackgroundStuff reoreinted) {
            super.onPostExecute(reoreinted);
            CustomPhotoGalleryActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;

            Log.v("---imagePreviewId ","-------------------------- "+imagePreviewId);
            if(reoreinted!=null&&imagePreviewId==reoreinted.getId()&activity.thumbnailsSelectionList.size()>0&activity.loading.get(reoreinted.getId())){
                Log.v("---thumb ","---------------------------- "+reoreinted.getId());
                reoreinted.getView().animate().alpha(1);
                reoreinted.getView().setImageBitmap(reoreinted.getBitmap());
            } else {
                Log.v("---thumb to slow ","---------------------------- ");
            }
        }
    }
    
    public class ImageAdapter1 extends RecyclerView.Adapter<ImageAdapter1.ViewHolder> {

        private List<thumbnailClass> mData;
        private LayoutInflater mInflater;
        private ImageAdapter.ItemClickListener mClickListener;
        private Context context;

        // data is passed into the constructor
        ImageAdapter1(Context context, List<thumbnailClass> data) {
            this.context = context.getApplicationContext();
            this.mInflater = LayoutInflater.from(context);
            this.mData = data;
        }


        @NonNull
        @Override
        public ImageAdapter1.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

            View view = mInflater.inflate(R.layout.custom_gallery_item, viewGroup, false);
            return new ImageAdapter1.ViewHolder(view);        }

        @Override
        public void onBindViewHolder(@NonNull ImageAdapter1.ViewHolder holder, int position) {
            final int pos = position;
            final ImageAdapter1.ViewHolder mHolder = holder;

            Log.v("-------holder1","-----"+holder.hashCode());
            Log.v("-------holder2","-----"+holder.getAdapterPosition());
            
            hashMap.put(holder.hashCode(), holder.getAdapterPosition());
            holder.imgThumb.setImageResource(R.drawable.grey_square);

            holder.chkImage.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                    if (thumbnailsSelectionList.indexOf(pos) != -1) {
                        int indexNum = thumbnailsSelectionList.indexOf(pos);

                        mHolder.chkImage.setChecked(false);
                        thumbnailsSelectionList.remove(indexNum);
                        int size = thumbnailsSelectionList.size();

                        Log.v("---ipCheck22", "-----" + indexNum);
                        Log.v("---listSize2", "----- " + size);

                        if (size > 0) {
                            int sizeMinusOne = thumbnailsSelectionList.get(thumbnailsSelectionList.size() - 1);
                            Log.v("---ipCheck2", "2" + sizeMinusOne);

                            imagePreviewId = thumbnailClassList.get(sizeMinusOne).ids;
                            imagePreview.animate().alpha(0);
                            imagePreview1(imagePreview, thumbnailClassList.get(sizeMinusOne).getIds(), thumbnailClassList.get(sizeMinusOne).getArrPath());
                        } else {
                            imagePreview.animate().alpha(0);
                        }

                    } else if (thumbnailsSelectionList.size() < limit) {

                        Log.v("---ipCheck3", "3");
                        imagePreviewId = thumbnailClassList.get(pos).ids;
                        imagePreview.animate().alpha(0);
                        imagePreview1(imagePreview, thumbnailClassList.get(pos).getIds(), thumbnailClassList.get(pos).getArrPath());
                        mHolder.chkImage.setChecked(true);
                        thumbnailsSelectionList.add(pos);

                    } else {
                        mHolder.chkImage.setChecked(false);
                        Toast.makeText(CustomPhotoGalleryActivity.this, "Limit is: "+limit, Toast.LENGTH_SHORT).show();
                    }

                }
            });
            holder.imgThumb.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {

                    Log.v("---ipCheck1", "------ " + pos);
                    Log.v("---listSize1", "----- " + thumbnailsSelectionList.size());
                    
                    if (thumbnailsSelectionList.indexOf(pos) != -1) {
                        int indexNum = thumbnailsSelectionList.indexOf(pos);

                        mHolder.chkImage.setChecked(false);
                        thumbnailsSelectionList.remove(indexNum);
                        int size = thumbnailsSelectionList.size();
                        Log.v("---ipCheck22", "-----" + indexNum);
                        Log.v("---listSize2", "----- " + size);

                        if (size > 0) {
                            int sizeMinusOne = thumbnailsSelectionList.get(thumbnailsSelectionList.size() - 1);
                            Log.v("---ipCheck2", "----" + sizeMinusOne);
                            imagePreviewId = thumbnailClassList.get(sizeMinusOne).getIds();
                            imagePreview.animate().alpha(0);
                            imagePreview1(imagePreview, thumbnailClassList.get(sizeMinusOne).getIds(), thumbnailClassList.get(sizeMinusOne).getArrPath());
                        } else {
                            Log.v("---ipCheck222", "----");
                            imagePreview.animate().alpha(0);
                        }

                    } else if (thumbnailsSelectionList.size() < limit) {
                        Log.v("---ipCheck3", "3");
                        imagePreviewId = thumbnailClassList.get(pos).ids;
                        imagePreview.animate().alpha(0);
                        imagePreview1(imagePreview, thumbnailClassList.get(pos).getIds(), thumbnailClassList.get(pos).getArrPath());
                        mHolder.chkImage.setChecked(true);
                        thumbnailsSelectionList.add(pos);

                    } else {
                        Toast.makeText(CustomPhotoGalleryActivity.this, "Limit is: "+limit, Toast.LENGTH_SHORT).show();
                    }

                }
            });
            try {
                setBitmap(holder, holder.getAdapterPosition(), thumbnailClassList.get(position).getIds(), thumbnailClassList.get(position).getArrPath());
            } catch (Throwable e) {
            }

            holder.chkImage.setChecked(thumbnailsSelectionList.indexOf(position)!=-1);
        }

        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            if(mData==null){
                return 0;
            } else {
                return mData.size();
            }
        }

        // stores and recycles views as they are scrolled off screen
        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            ImageView imgThumb;
            CheckBox chkImage;

            ViewHolder(View itemView) {
                super(itemView);

                imgThumb = (ImageView) itemView.findViewById(R.id.imgThumb);
                chkImage = (CheckBox) itemView.findViewById(R.id.chkImage);

                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
            }
        }

    }


    public static Bitmap modifyOrientation(Bitmap bitmap, String picPath) throws IOException {
        ExifInterface ei;
        ei = new ExifInterface(picPath);

        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotate(bitmap, 90);

            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotate(bitmap, 180);

            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotate(bitmap, 270);

            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                return flip(bitmap, true, false);

            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                return flip(bitmap, false, true);

            default:
                return bitmap;
        }
    }
    public static Bitmap rotate(Bitmap bitmap, float degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public static Bitmap flip(Bitmap bitmap, boolean horizontal, boolean vertical) {
        Matrix matrix = new Matrix();
        matrix.preScale(horizontal ? -1 : 1, vertical ? -1 : 1);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private class thumbnailClass{
        private String arrPath;
        private int ids;

        public String getArrPath() {
            return arrPath;
        }

        public void setArrPath(String arrPath) {
            this.arrPath = arrPath;
        }

        public int getIds() {
            return ids;
        }

        public void setIds(int ids) {
            this.ids = ids;
        }

    }
    private class BackgroundStuff{
        private int id;
        private int holderPos;
        private String path;
        private ImageAdapter1.ViewHolder holder;
        private ImageView view;
        private Bitmap bitmap;


        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getHolderPos() {
            return holderPos;
        }

        public void setHolderPos(int holderPos) {
            this.holderPos = holderPos;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public ImageAdapter1.ViewHolder getHolder() {
            return holder;
        }

        public void setHolder(ImageAdapter1.ViewHolder holder) {
            this.holder = holder;
        }

        public ImageView getView() {
            return view;
        }

        public void setView(ImageView view) {
            this.view = view;
        }

        public Bitmap getBitmap() {
            return bitmap;
        }

        public void setBitmap(Bitmap bitmap) {
            this.bitmap = bitmap;
        }
    }
}
