package com.zrp.gifmakerdemo;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.zrp.gifmakerdemo.gifmaker.AnimatedGifEncoder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pl.droidsonroids.gif.GifImageView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private GridView grid_view;
    private GifImageView gif_image;
    private EditText file_text;
    private SeekBar delay_bar;

    public static final String TAG = "MainActivity";
    public static final int START_ALBUM_CODE = 0x21;

    private List<String> pics = new ArrayList<>();
    private PhotoAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    private void initView() {
        grid_view = (GridView) findViewById(R.id.grid_view);
        file_text = (EditText) findViewById(R.id.file_text);
        delay_bar = (SeekBar) findViewById(R.id.delay_bar);
        gif_image = (GifImageView) findViewById(R.id.gif_image);
        findViewById(R.id.generate).setOnClickListener(this);
        findViewById(R.id.clear).setOnClickListener(this);

        adapter = new PhotoAdapter(this, null);
        grid_view.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.generate://生成gif图
                Toast.makeText(MainActivity.this, "开始生成Gif图", Toast.LENGTH_SHORT).show();

                String file_name = file_text.getText().toString();
                createGif(TextUtils.isEmpty(file_name) ? "demo1" : file_name, delay_bar.getProgress());
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
        String path = Environment.getExternalStorageDirectory().getPath() + "/GIFMakerDemo/" + file_name + ".gif";
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

        gif_image.setImageURI(Uri.parse(path));
        Toast.makeText(MainActivity.this, "Gif已生成。保存路径：\n" + path, Toast.LENGTH_LONG).show();
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
}
