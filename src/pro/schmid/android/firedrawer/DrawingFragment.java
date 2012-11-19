package pro.schmid.android.firedrawer;

import java.util.HashMap;
import java.util.Map;

import pro.schmid.android.androidonfire.DataSnapshot;
import pro.schmid.android.androidonfire.Firebase;
import pro.schmid.android.androidonfire.callbacks.DataEvent;
import pro.schmid.android.androidonfire.callbacks.EventType;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.JsonPrimitive;

public class DrawingFragment extends Fragment {

	private static final String TAG = DrawingFragment.class.getSimpleName();
	private static final int SIZE = 48;
	private static final String[] COLOR_STRINGS = new String[] { "ffffff", "000000", "ff0000", "00ff00", "0000ff", "8888ff", "ff88dd", "ff8888", "ff0055", "ff8800", "00ff88", "ccff00", "0088ff", "440088", "ffff88", "88ffff" };

	private final String[][] mPixels = new String[SIZE][SIZE];
	private final Firebase mFirebase;

	private PixelsView mPixelsView;

	public DrawingFragment(Firebase firebase) {
		this.mFirebase = firebase;

		this.mFirebase.on(EventType.child_added, mPixelListener);
		this.mFirebase.on(EventType.child_changed, mPixelListener);
		this.mFirebase.on(EventType.child_removed, mPixelRemovedListener);
	}

	public static final DrawingFragment newInstance(Firebase firebase) {
		return new DrawingFragment(firebase);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mPixelsView = new PixelsView(getActivity());
		return mPixelsView;
	}

	private final DataEvent mPixelListener = new DataEvent() {
		@Override
		public void callback(DataSnapshot snapshot, String prevChildName) {
			try {
				String name = snapshot.name();
				String[] coordinates = name.split(":");

				int x = Integer.parseInt(coordinates[0]);
				int y = Integer.parseInt(coordinates[1]);

				mPixels[x][y] = snapshot.val().getAsString();

				mPixelsView.postInvalidate(); // TODO invalidate only the rectangle
			} catch (Exception e) {
				Log.e(TAG, "Could not deserialize child", e);
			}
		}
	};

	private final DataEvent mPixelRemovedListener = new DataEvent() {
		@Override
		public void callback(DataSnapshot snapshot, String prevChildName) {
			try {
				String name = snapshot.name();
				String[] coordinates = name.split(":");

				int x = Integer.parseInt(coordinates[0]);
				int y = Integer.parseInt(coordinates[1]);

				mPixels[x][y] = null;

				mPixelsView.postInvalidate(); // TODO invalidate only the rectangle
			} catch (Exception e) {
				Log.e(TAG, "Could not deserialize child", e);
			}
		}
	};

	private class PixelsView extends View {

		private final Map<String, Paint> colors = new HashMap();

		private int mSurfaceW, mSurfaceH;
		private int mPixelSize;

		public PixelsView(Context ctx) {
			super(ctx);

			Paint mPaint = new Paint();
			mPaint.setAntiAlias(true);
			mPaint.setDither(true);
			mPaint.setStyle(Paint.Style.FILL);
			mPaint.setStrokeJoin(Paint.Join.BEVEL);
			mPaint.setStrokeCap(Paint.Cap.SQUARE);
			mPaint.setStrokeWidth(12);

			for (String color : COLOR_STRINGS) {
				Paint tmp = new Paint(mPaint);
				int c = Color.parseColor("#" + color);
				tmp.setColor(c);
				colors.put(color, tmp);
			}
		}

		@Override
		protected void onMeasure(int w, int h) {

			int squareSide = w > h ? h : w;

			setMeasuredDimension(squareSide, squareSide);
		}

		@Override
		protected void onSizeChanged(int w, int h, int oldw, int oldh) {
			super.onSizeChanged(w, h, oldw, oldh);

			mSurfaceW = w;
			mSurfaceH = h;
			int squareSide = w > h ? h : w;
			mPixelSize = squareSide / SIZE;
		}

		@Override
		protected void onDraw(Canvas canvas) {
			canvas.drawColor(0x0A000000);

			for (int x = 0; x < mPixels.length; x++) {
				for (int y = 0; y < mPixels[x].length; y++) {
					String pixelColor = mPixels[x][y];

					if (pixelColor != null) {
						int beginX = x * mPixelSize;
						int beginY = y * mPixelSize;
						int endX = (x + 1) * mPixelSize;
						int endY = (y + 1) * mPixelSize;

						Paint paint = colors.get(pixelColor);

						if (paint != null) {
							canvas.drawRect(beginX, beginY, endX, endY, paint);
						}
					}
				}
			}
		}

		private float mX, mY;
		private static final float TOUCH_TOLERANCE = 4;

		private void touch_start(float x, float y) {
			// mPath.reset();
			// mPath.moveTo(x, y);
			mX = x;
			mY = y;
		}

		private void touch_move(float x, float y) {
			float dx = Math.abs(x - mX);
			float dy = Math.abs(y - mY);
			if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
				// mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
				mX = x;
				mY = y;
			}
		}

		private void touch_up() {
			// mPath.lineTo(mX, mY);
			// // commit the path to our offscreen
			// // mCanvas.drawPath(mPath, mPaint);
			// // kill this so we don't double draw
			// mPath.reset();
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {
			float x = event.getX();
			float y = event.getY();

			int pixelX = (int) (x / mPixelSize);
			int pixelY = (int) (y / mPixelSize);
			String name = pixelX + ":" + pixelY;

			mFirebase.child(name).set(new JsonPrimitive("000000"));

			// switch (event.getAction()) {
			// case MotionEvent.ACTION_DOWN:
			// touch_start(x, y);
			// invalidate();
			// break;
			// case MotionEvent.ACTION_MOVE:
			// touch_move(x, y);
			// invalidate();
			// break;
			// case MotionEvent.ACTION_UP:
			// touch_up();
			// invalidate();
			// break;
			// }
			return true;
		}
	}

}
