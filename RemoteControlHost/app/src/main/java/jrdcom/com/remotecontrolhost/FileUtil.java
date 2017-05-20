package jrdcom.com.remotecontrolhost;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ryze on 2016-5-26.
 */
public class FileUtil {

  //系统保存截图的路径
  public static final String SCREENCAPTURE_PATH = "ScreenCapture" + File.separator + "Screenshots" + File.separator;
//  public static final String SCREENCAPTURE_PATH = "ZAKER" + File.separator + "Screenshots" + File.separator;

  public static final String SCREENSHOT_NAME = "Screenshot";

  public static String getAppPath(Context context) {

    if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {


      return Environment.getExternalStorageDirectory().toString();

    } else {

      return context.getFilesDir().toString();
    }

  }


  public static String getScreenShots(Context context) {

    StringBuffer stringBuffer = new StringBuffer(getAppPath(context));
    stringBuffer.append(File.separator);

    stringBuffer.append(SCREENCAPTURE_PATH);

    File file = new File(stringBuffer.toString());

    if (!file.exists()) {
      file.mkdirs();
    }

    return stringBuffer.toString();

  }

  public static String getScreenShotsName(Context context) {

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");

    String date = simpleDateFormat.format(new Date());

    StringBuffer stringBuffer = new StringBuffer(getScreenShots(context));
    stringBuffer.append(SCREENSHOT_NAME);
    stringBuffer.append("_");
    stringBuffer.append(date);
    stringBuffer.append(".png");

    return stringBuffer.toString();

  }

    //将图片保存进SD卡
    public static void saveToSD(Bitmap bmp, Context context,String dirName, String fileName) throws IOException {
        // 判断sd卡是否存在
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            File file;
            if(dirName == null){
                File externalFilesDir = context.getExternalFilesDir(null);
                file = new File(externalFilesDir.getPath().toString() + "/" + fileName);
            }else{
                file = new File(dirName+"/"+fileName);
            }
            //String currentDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Screenshot";
            // 判断文件是否存在，不存在则创建
            if (!file.exists()) {
                file.createNewFile();
            }

            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(file);
                if (fos != null) {
                    // 第一参数是图片格式，第二个是图片质量，第三个是输出流
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    // 用完关闭
                    fos.flush();
                    fos.close();
                }
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    //从SD卡读取图片到bitmap
    public static Bitmap readFromSD(Context context, String filename){
        File externalFilesDir = context.getExternalFilesDir(null);

        String filePath = externalFilesDir.getPath() +"/" +filename;
        Bitmap bitmap1 = BitmapFactory.decodeFile(filePath);
        return bitmap1;
    }
}
