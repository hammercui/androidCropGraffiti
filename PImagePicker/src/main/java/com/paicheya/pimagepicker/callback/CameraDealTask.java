package com.paicheya.pimagepicker.callback;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.AsyncTask;

import com.paicheya.pimagepicker.util.BitmapUtil;
import com.paicheya.pimagepicker.util.MyLog;
import com.paicheya.pimagepicker.view.camera.CameraActivity;

import java.io.IOException;

/**
 * 相机获得bitmap并处理的异步任务
 * Created by cly on 17/2/10.
 */

public  class CameraDealTask extends AsyncTask<byte[],Integer,Bitmap> {

    private CameraDealCallback cameraDealCallback;
    private CameraActivity.Direction currDirect = CameraActivity.Direction.Vertical;
    private float aspectRatio = 4f/3f;
    private String outPutPath ;
    public CameraDealTask(CameraDealCallback cameraDealCallback,CameraActivity.Direction currDirect,float aspectRatio){
        super();
        this.cameraDealCallback = cameraDealCallback;
        this.currDirect = currDirect;
        this.aspectRatio = aspectRatio;
    }
    public void setOutPutPath(String outPutPath){
        this.outPutPath = outPutPath;
    }

    /**
     * 子线程执行操作
     * @param params
     * @return
     */
    @Override
    protected Bitmap doInBackground(byte[]... params) {
        // 相机获得图像
        MyLog.log("拍照获得图片大小："+params[0].length+"b");
        Bitmap bitmap = BitmapUtil.byteToBitmap(params[0]);
        //Bitmap bitmap = BitmapFactory.decodeByteArray(params[0], 0, params[0].length);
//        int width = bitmap.getWidth();
//        int height = bitmap.getHeight();
        //MyLog.log("拍照获得图片分辨率：width_"+width+"  height_"+ height);

        //竖屏情况
        if(currDirect == CameraActivity.Direction.Vertical){
            //统一进行裁切处理
            bitmap = BitmapUtil.cropWithAspect(bitmap, aspectRatio);
        }
        //横屏情况
        else{
            //统一进行旋转
            bitmap = BitmapUtil.cropWithRotation(bitmap,-90);
        }

        MyLog.log("处理后图片分辨率：width_"+bitmap.getWidth()+"  height_"+bitmap.getHeight());

        return bitmap;
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        MyLog.log("CameraDealTask被主动取消");
        cameraDealCallback = null;
    }


    //执行结束
    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        cameraDealCallback.onSuccess(bitmap);

    }

    /**
     * 将图片的旋转角度置为0  ，此方法可以解决某些机型拍照后图像，出现了旋转情况
     *
     * @Title: setPictureDegreeZero
     * @param path
     * @return void
     * @date 2012-12-10 上午10:54:46
     */
    private void setPictureDegreeZero(String path) {
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            // 修正图片的旋转角度，设置其不旋转。这里也可以设置其旋转的角度，可以传值过去，
            // 例如旋转90度，传值ExifInterface.ORIENTATION_ROTATE_90，需要将这个值转换为String类型的
            exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION, "no");
            exifInterface.saveAttributes();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /** 从给定的路径加载图片，并指定是否自动旋转方向 */
    public Bitmap loadBitmap(String imagePath) {
            Bitmap bm = BitmapFactory.decodeFile(imagePath);
            int degree =  BitmapUtil.getDegree(imagePath);

            if (degree != 0) {
                // 旋转图片
                Matrix m = new Matrix();
                m.postRotate(degree);
                bm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(),
                        bm.getHeight(), m, true);
            }
            return bm;
        }


}
