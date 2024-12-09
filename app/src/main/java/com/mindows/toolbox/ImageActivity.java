package com.mindows.toolbox;

import android.app.Activity;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageView;


public class ImageActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        ImageView myImageView = new ImageView(this);
        String str = getIntent().getStringExtra("pic");
        if (str == null || str.equals("1"))
            myImageView.setImageResource(R.drawable.abreboot);
        else
            myImageView.setImageResource(R.drawable.sharedspace_mount);
        myImageView.setScaleType(ImageView.ScaleType.MATRIX);
        Matrix matrix = new Matrix();

        matrix.setTranslate(0, 0);
        matrix.postScale(0.5f, 0.5f, 200, 200);
        myImageView.setImageMatrix(matrix);
        myImageView.setOnTouchListener(new TouchListener(myImageView, matrix));
        setContentView(myImageView);
    }


    public class TouchListener implements View.OnTouchListener {

        private final ImageView imageView;
        private final Matrix matrix;

        private final Matrix savedMatrix = new Matrix();
        private final PointF start = new PointF();
        private final PointF mid = new PointF();
        private float oldDist = 1f;
        private boolean scaled = false;

        public TouchListener(ImageView imageView, Matrix matrix) {
            this.imageView = imageView;
            this.matrix = matrix;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            Log.d("TAG", "onTouch: " + event.getActionMasked());
            switch (event.getAction() & MotionEvent.ACTION_MASK) {

                case MotionEvent.ACTION_DOWN:
                    savedMatrix.set(matrix);
                    start.set(event.getX(), event.getY());
                    break;

                case MotionEvent.ACTION_POINTER_DOWN:
                    oldDist = spacing(event);
                    if (oldDist > 10f) {
                        savedMatrix.set(matrix);
                        midPoint(mid, event);
                    }
                    break;

                case MotionEvent.ACTION_MOVE:
                    if (event.getPointerCount() == 1) {
                        if (scaled) return false;
                        matrix.set(savedMatrix);
                        matrix.postTranslate(event.getX() - start.x, event.getY() - start.y);
                    } else if (event.getPointerCount() == 2) {
                        scaled = true;
                        float newDist = spacing(event);
                        if (newDist > 10f) {
                            matrix.set(savedMatrix);
                            float scale = newDist / oldDist;
                            matrix.postScale(scale, scale, mid.x, mid.y);
                        }
                    }

                    break;

                case MotionEvent.ACTION_UP:
                    savedMatrix.set(matrix);
                    scaled = false;
                case MotionEvent.ACTION_POINTER_UP:

                    break;
            }
            imageView.setImageMatrix(matrix);
            return true;
        }

        private float spacing(MotionEvent event) {
            float x = event.getX(0) - event.getX(1);
            float y = event.getY(0) - event.getY(1);
            return (float) Math.sqrt(x * x + y * y);
        }

        private void midPoint(PointF point, MotionEvent event) {
            float x = event.getX(0) + event.getX(1);
            float y = event.getY(0) + event.getY(1);
            point.set(x / 2, y / 2);
        }
    }


}
