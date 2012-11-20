package pro.schmid.android.firedrawer;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Space;

import com.google.gson.JsonPrimitive;

public class DrawingFragment extends Fragment {

	private static final String TAG = DrawingFragment.class.getSimpleName();
	private static final int SIZE = 48;
	private static final String[] COLOR_STRINGS = new String[] { "ffffff", "000000", "ff0000", "00ff00", "0000ff", "8888ff", "ff88dd", "ff8888", "ff0055", "ff8800", "00ff88", "ccff00", "0088ff", "440088", "ffff88", "88ffff" };
	private static final Map<String, Integer> STRING_TO_COLOR = new LinkedHashMap<String, Integer>();

	private final String[][] mPixels = new String[SIZE][SIZE];
	private final Firebase mFirebase;

	private String mCurrentColor = "000000";

	private PixelsView mPixelsView;

	static {
		for (String color : COLOR_STRINGS) {
			int c = Color.parseColor("#" + color);
			STRING_TO_COLOR.put(color, c);
		}
	}

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

		LinearLayout.LayoutParams lpp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f);
		Space s1 = new Space(getActivity());
		s1.setLayoutParams(lpp);
		Space s2 = new Space(getActivity());
		s2.setLayoutParams(lpp);

		mPixelsView = new PixelsView(getActivity());
		mPixelsView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

		LinearLayout buttons1 = new LinearLayout(getActivity());
		buttons1.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		buttons1.setOrientation(LinearLayout.HORIZONTAL);

		LinearLayout buttons2 = new LinearLayout(getActivity());
		buttons2.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		buttons2.setOrientation(LinearLayout.HORIZONTAL);

		int i = 0;
		LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f);
		for (Entry<String, Integer> c : STRING_TO_COLOR.entrySet()) {
			Button b = new Button(getActivity());
			b.setBackgroundColor(c.getValue());
			b.setLayoutParams(lp);
			b.setTag(c.getKey());
			b.setOnClickListener(mColorButtonClickListener);

			if (++i % 2 == 0) {
				buttons2.addView(b);
			} else {
				buttons1.addView(b);
			}
		}

		LinearLayout rl = new LinearLayout(getActivity());
		rl.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
		rl.setOrientation(LinearLayout.VERTICAL);

		rl.addView(s1);
		rl.addView(mPixelsView);
		rl.addView(s2);
		rl.addView(buttons1);
		rl.addView(buttons2);

		return rl;
	}

	private final OnClickListener mColorButtonClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			mCurrentColor = (String) v.getTag();
		}
	};

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

		private final Map<String, Paint> mColors = new HashMap();

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

			for (Entry<String, Integer> c : STRING_TO_COLOR.entrySet()) {
				Paint tmp = new Paint(mPaint);
				tmp.setColor(c.getValue());
				mColors.put(c.getKey(), tmp);
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

						Paint paint = mColors.get(pixelColor);

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
			Firebase child = mFirebase.child(name);

			if ("ffffff".equalsIgnoreCase(mCurrentColor)) {
				child.remove();
			} else {
				child.set(new JsonPrimitive(mCurrentColor));
			}

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
