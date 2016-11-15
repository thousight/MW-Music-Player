package com.example.minb.programmingassignment2hint;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static android.os.Environment.getExternalStorageDirectory;

public class MainActivity extends AppCompatActivity {

    ListView listViewMP3;
    ArrayList<String> mp3List;
    String selectedMP3;
    Button playPauseButon, stopButton;
    SeekBar seekBar;
    MediaPlayer music;
    TextView musicTime;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setElevation(0);
        setTitle("MW Music Player");
        final boolean hasPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

        if (!hasPermission) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }

        mp3List = new ArrayList<>();
        playPauseButon = (Button) findViewById(R.id.playPauseButton);
        stopButton = (Button) findViewById(R.id.stopButton);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        musicTime = (TextView) findViewById(R.id.musicTime);
        playPauseButon.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_play_arrow_white_48px, 0, 0, 0);

        File[] listFiles = new File("sdcard/Music").listFiles();
        String fileName, extName;

        if (listFiles == null) {
            listFiles = new File[0];
        } else {
            for (File file : listFiles) {
                fileName = file.getName();
                extName = fileName.substring(fileName.length() - 3);
                if (extName.equals("mp3"))
                    mp3List.add(fileName);
            }
        }


        listViewMP3 = (ListView) findViewById(R.id.listViewMP3);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_single_choice, mp3List);
        listViewMP3.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listViewMP3.setAdapter(adapter);
        listViewMP3.setItemChecked(0, true);

        // Set initially selected music

        listViewMP3.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                        music.stop();
                        selectedMP3 = mp3List.get(arg2);
                        music = MediaPlayer.create(getApplicationContext(), Uri.parse(getExternalStorageDirectory().getPath()+ "/Music/" + selectedMP3));
                        music.setLooping(true);
                        seekBar.setMax(music.getDuration());
                        playMusic();
                    }
                });
        if (mp3List.size() > 0) {
            music = MediaPlayer.create(getApplicationContext(), Uri.parse(getExternalStorageDirectory().getPath()+ "/Music/" + mp3List.get(0)));
            selectedMP3 = mp3List.get(0);
            updateTextProgress();
        }

        playPauseButon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (hasPermission) {
                    if (music.isPlaying()){
                        pauseMusic();
                    } else {
                        playMusic();
                    }
                } else {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                }
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (hasPermission) {
                    music.stop();
                    try {
                        music.prepare();
                    } catch (IllegalStateException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    music.seekTo(0);
                    seekBar.setProgress(0);
                    playPauseButon.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_play_arrow_white_48px, 0, 0, 0);
                } else {
                    ActivityCompat.requestPermissions(getParent(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                }
            }
        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                // TODO Auto-generated method stub

                if(fromUser)
                    music.seekTo(progress);
            }
        });
    }

    public void Thread(){
        Runnable task = new Runnable(){
            public void run(){

                while(music.isPlaying()){
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    seekBar.setProgress(music.getCurrentPosition());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateTextProgress();
                        }
                    });
                }
            }
        };
        Thread thread = new Thread(task);
        thread.start();
    }

    public void playMusic() {
        music.start();
        music.setLooping(true);
        seekBar.setMax(music.getDuration());
        Thread();
        playPauseButon.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pause_white_48px, 0, 0, 0);
    }

    public void pauseMusic() {
        music.pause();
        Thread();
        playPauseButon.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_play_arrow_white_48px, 0, 0, 0);
    }

    public void updateTextProgress() {
        long totalMinute = TimeUnit.MILLISECONDS.toMinutes(music.getDuration());
        String totalSecond = String.format("%02d", TimeUnit.MILLISECONDS.toSeconds(music.getDuration()) % 60);
        long currentMinute = TimeUnit.MILLISECONDS.toMinutes(music.getCurrentPosition());
        String currentSecond = String.format("%02d", TimeUnit.MILLISECONDS.toSeconds(music.getCurrentPosition()) % 60);
        musicTime.setText(currentMinute + ":" + currentSecond + "/" + totalMinute + ":" + totalSecond);
    }
}
