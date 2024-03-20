package com.example.taskwiserebirth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import androidx.fragment.app.Fragment;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import com.bin4rybros.demo.GLRenderer;
import com.bin4rybros.demo.LAppDelegate;

public class Live2DFragment extends Fragment implements View.OnTouchListener {
    private GLSurfaceView glSurfaceView;
    private GLRenderer glRenderer;
    private ImageButton collapseButton;
    private View bottomNavigationView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_live2d, container, false);

        collapseButton = view.findViewById(R.id.fullscreen_button);
        bottomNavigationView = getActivity().findViewById(R.id.bottomNavigationView);
        glSurfaceView = view.findViewById(R.id.gl_surface_view);

        glSurfaceView.setEGLContextClientVersion(2); // Using OpenGL ES 2.0
        glRenderer = new GLRenderer();
        glSurfaceView.setRenderer(glRenderer);
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        glSurfaceView.setOnTouchListener(this);

        collapseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleBottomNavigationView();
            }
        });

        setSystemUIVisibility();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        LAppDelegate.getInstance().onStart(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        glSurfaceView.onResume();
        View decor = getActivity().getWindow().getDecorView();
        decor.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
    }

    @Override
    public void onPause() {
        super.onPause();
        glSurfaceView.onPause();
        LAppDelegate.getInstance().onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        LAppDelegate.getInstance().onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LAppDelegate.getInstance().onDestroy();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        float pointX = event.getX();
        float pointY = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                LAppDelegate.getInstance().onTouchBegan(pointX, pointY);
                break;
            case MotionEvent.ACTION_UP:
                LAppDelegate.getInstance().onTouchEnd(pointX, pointY);
                break;
            case MotionEvent.ACTION_MOVE:
                LAppDelegate.getInstance().onTouchMoved(pointX, pointY);
                break;
        }
        return true; // Consume the touch event
    }

    private void toggleBottomNavigationView() {
        if (bottomNavigationView.getVisibility() == View.VISIBLE) {
            bottomNavigationView.setVisibility(View.GONE);
        } else {
            bottomNavigationView.setVisibility(View.VISIBLE);
        }
    }

    private void setSystemUIVisibility() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            getActivity().getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT
                            ? View.SYSTEM_UI_FLAG_LOW_PROFILE
                            : View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
            );
        } else {
            getActivity().getWindow().getInsetsController().hide(WindowInsets.Type.navigationBars() | WindowInsets.Type.statusBars());
            getActivity().getWindow().getInsetsController().setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        }
    }
}

