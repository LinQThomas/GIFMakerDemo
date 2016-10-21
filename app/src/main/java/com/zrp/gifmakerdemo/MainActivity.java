package com.zrp.gifmakerdemo;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.zrp.gifmakerdemo.gifmaker.AnimatedGifEncoder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView delay_text;
    private GifImageView gif_image;
    private EditText file_text;

    public static final String TAG = "MainActivity";
    public static final int START_ALBUM_CODE = 0x21;

    private List<String> pics = new ArrayList<>();
    private PhotoAdapter adapter;
    private int delayTime;//帧间隔
    private AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        alertDialog = new AlertDialog.Builder(this).setView(new ProgressBar(this))
                .setMessage("正在生成gif图片").create();
    }

    private void initView() {
        GridView grid_view = (GridView) findViewById(R.id.grid_view);
        file_text = (EditText) findViewById(R.id.file_text);
        SeekBar delay_bar = (SeekBar) findViewById(R.id.delay_bar);
        gif_image = (GifImageView) findViewById(R.id.gif_image);
        delay_text = (TextView) findViewById(R.id.delay_text);
        findViewById(R.id.generate).setOnClickListener(this);
        findViewById(R.id.clear).setOnClickListener(this);

        adapter = new PhotoAdapter(this, null);
        grid_view.setAdapter(adapter);

        file_text.setText("demo");
        delayTime = delay_bar.getProgress();
        delay_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                delayTime = progress;
                delay_text.setText("帧间隔时长：" + progress + "ms");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.generate://生成gif图
                alertDialog.show();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String file_name = file_text.getText().toString();
                        createGif(TextUtils.isEmpty(file_name) ? "demo" : file_name, delayTime);

                        alertDialog.dismiss();
                    }
                }).start();
                break;
            case R.id.clear:
                clearData();
                break;
        }
    }

    /**
     * 清除当前的数据内容
     */
    private void clearData() {
        pics.clear();
        adapter.setList(null);
        gif_image.setImageDrawable(null);
    }

    /**
     * 生成gif图
     *
     * @param delay 图片之间间隔的时间
     */
    private void createGif(String file_name, int delay) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        AnimatedGifEncoder localAnimatedGifEncoder = new AnimatedGifEncoder();
        localAnimatedGifEncoder.start(baos);//start
        localAnimatedGifEncoder.setRepeat(0);//设置生成gif的开始播放时间。0为立即开始播放
        localAnimatedGifEncoder.setDelay(delay);

        //【注意1】开始生成gif的时候，是以第一张图片的尺寸生成gif图的大小，后面几张图片会基于第一张图片的尺寸进行裁切
        //所以要生成尺寸完全匹配的gif图的话，应先调整传入图片的尺寸，让其尺寸相同
        //【注意2】如果传入的单张图片太大的话会造成OOM，可在不损失图片清晰度先对图片进行质量压缩
        if (pics.isEmpty()) {
            localAnimatedGifEncoder.addFrame(BitmapFactory.decodeResource(getResources(), R.drawable.pic_1));
            localAnimatedGifEncoder.addFrame(BitmapFactory.decodeResource(getResources(), R.drawable.pic_2));
            localAnimatedGifEncoder.addFrame(BitmapFactory.decodeResource(getResources(), R.drawable.pic_3));
        } else {
            for (int i = 0; i < pics.size(); i++) {
                // Bitmap localBitmap = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(pics.get(i)), 512, 512);
                localAnimatedGifEncoder.addFrame(BitmapFactory.decodeFile(pics.get(i)));
            }
        }
        localAnimatedGifEncoder.finish();//finish

        File file = new File(Environment.getExternalStorageDirectory().getPath() + "/GIFMakerDemo");
        if (!file.exists()) file.mkdir();
        final String path = Environment.getExternalStorageDirectory().getPath() + "/GIFMakerDemo/" + file_name + ".gif";
        Log.d(TAG, "createGif: ---->" + path);

        try {
            FileOutputStream fos = new FileOutputStream(path);
            baos.writeTo(fos);
            baos.flush();
            fos.flush();
            baos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    GifDrawable gifDrawable = new GifDrawable(path);
                    gif_image.setImageDrawable(gifDrawable);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Toast.makeText(MainActivity.this, "Gif已生成。保存路径：\n" + path, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * 打开系统图库选择图片
     */
    public void photoPick() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, START_ALBUM_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == -1) {
            Uri localUri = data.getData();
            String[] arrayOfString = {"_data"};
            Cursor localCursor = getContentResolver().query(localUri, arrayOfString, null, null, null);
            localCursor.moveToFirst();
            String str = localCursor.getString(localCursor.getColumnIndex(arrayOfString[0]));
            localCursor.close();
            pics.add(str);

            Log.d(TAG, "onActivityResult: ----->" + pics.toString());
            adapter.setList(pics);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        alertDialog.dismiss();
    }
}
