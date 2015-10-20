package com.mobileproto.jovanduy.scavengerhunt;

import android.app.ProgressDialog;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.VideoView;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Jordan on 10/6/15.
 */
public class VideoFragment extends Fragment {

    private View view;
    private VideoView videoView;
    private ProgressDialog pDialog;
    private Button leftButton;
    private Button rightButton;
    private Button checkGps;

    private int currStage;
    private int stageFinal;
    private Server server;
    private ArrayList<Double> latitudes;
    private ArrayList<Double> longitudes;
    private ArrayList<String> videos;
    private ArrayList<String> images;
    private String urlBase = "https://s3.amazonaws.com/olin-mobile-proto/";
    private boolean onLastStage;

    private Uri video;


    public VideoFragment() {
        this.stageFinal = 0;
        this.currStage = 0;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_video, container, false);
        videoView = (VideoView) view.findViewById(R.id.video_view);
        leftButton = (Button) view.findViewById(R.id.left_btn);
        rightButton = (Button) view.findViewById(R.id.right_btn);
        checkGps = (Button) view.findViewById(R.id.gps_check_btn);
        server = new Server(getContext());
        latitudes = new ArrayList<>();
        longitudes = new ArrayList<>();
        videos = new ArrayList<>();
        setUpButton(leftButton);
        setUpButton(rightButton);
        setUpButton(checkGps);

        loadNext(currStage);

        return view;
    }

    public void setUpButton(final Button button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (button == leftButton) {
                    currStage -= 1;
                    loadNext(currStage);
                }
                else if (button == rightButton) {
                    currStage += 1;
                    loadNext(currStage);
                } else {
                    stageFinal += 1;
                    currStage += 1;
                    if(!onLastStage) {
                        loadNext(stageFinal);
                    } else {
                        Log.d("YOU'RE DONE!!", "YOU'RE DONE!!");
                    }
                }
            }
        });
    }

    public void getNext() {
        //TODO: update
    }

    public void loadNext (final int stage) {
        server.getNextInfo(stage, new Callback() {
            @Override
            public void callback(boolean success, double lat, double longi, String vid, boolean isLast) {
                latitudes.add(stage, lat);
                longitudes.add(stage, longi);
                videos.add(stage, vid);
                onLastStage = isLast;
                updateView(stage);

            }
        });
    }

    public void updateView(int stage) {
        video = Uri.parse(urlBase + videos.get(stage));
        if (currStage == stageFinal) {
            rightButton.setEnabled(false);
        } else {
            rightButton.setEnabled(true);
        }
        if (currStage == 0) {
            leftButton.setEnabled(false);
        } else {
            leftButton.setEnabled(true);
        }

        pDialog = new ProgressDialog(getContext());
        pDialog.setTitle("Stage " + currStage + " video");
        pDialog.setMessage("Buffering...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();
        MediaController mediaController = new MediaController(getContext());
        videoView.setMediaController(mediaController);
        videoView.setVideoURI(video);
        videoView.requestFocus();
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            // Close the progress bar and play the video
            public void onPrepared(MediaPlayer mp) {
                pDialog.dismiss();
                videoView.start();

            }
        });
    }
}
