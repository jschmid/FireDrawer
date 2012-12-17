package pro.schmid.android.firedrawer;

import pro.schmid.android.androidonfire.DataSnapshot;
import pro.schmid.android.androidonfire.Firebase;
import pro.schmid.android.androidonfire.FirebaseEngine;
import pro.schmid.android.androidonfire.callbacks.Transaction;
import pro.schmid.android.androidonfire.callbacks.TransactionComplete;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class DrawerActivity extends Activity {

	private FirebaseEngine mEngine;
	private DrawingFragment mFragment;
	private Firebase mFirebase;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mEngine = FirebaseEngine.getInstance();
		mEngine.setLoadedListener(new pro.schmid.android.androidonfire.callbacks.FirebaseLoaded() {
			@Override
			public void firebaseLoaded() {
				FragmentManager fm = getFragmentManager();
				FragmentTransaction ft = fm.beginTransaction();
				ft.add(R.id.fragment_holder, ChildInput.newInstance());
				ft.commit();

				mFirebase = mEngine.newFirebase("https://neqo.firebaseio.com").child("drawing");
			}
		});
		mEngine.loadEngine(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mFirebaseEngine.onDestroy();
	}

	protected void connect(String childName) {
		if (childName == null || childName.length() == 0) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					generateBoard();
				}
			});
		} else {
			addFragment(childName);
		}
	}

	private void generateBoard() {
		Firebase counter = mFirebase.child("count");
		counter.transaction(new Transaction() {
			@Override
			public JsonElement transaction(JsonElement obj) {
				if (obj.isJsonNull()) {
					return new JsonPrimitive(1);
				} else {
					int count = obj.getAsInt();
					return new JsonPrimitive(1 + count);
				}
			}
		}, new TransactionComplete() {
			@Override
			public void onComplete(boolean success, final DataSnapshot snapshot, String reason) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						addFragment(snapshot.val().getAsString());
					}
				});
			}
		});
	}

	private void addFragment(String childName) {
		Firebase f = mFirebase.child("drawings").child(childName);

		mFragment = DrawingFragment.newInstance(f);

		FragmentManager fm = getFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		ft.replace(R.id.fragment_holder, mFragment);
		ft.addToBackStack(null);
		ft.commit();

		setTitle(getResources().getString(R.string.windows_title, childName));
	}

}
