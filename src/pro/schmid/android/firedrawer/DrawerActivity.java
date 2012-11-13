package pro.schmid.android.firedrawer;

import pro.schmid.android.androidonfire.Firebase;
import pro.schmid.android.androidonfire.FirebaseEngine;
import pro.schmid.android.androidonfire.FirebaseEngine.FirebaseLoaded;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;

public class DrawerActivity extends Activity {

	private FirebaseEngine mEngine;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mEngine = FirebaseEngine.getInstance();
		mEngine.setLoadedListener(new FirebaseLoaded() {
			@Override
			public void firebaseLoaded() {
				addFragment();
			}
		});
		mEngine.loadEngine(this);
	}

	private void addFragment() {
		Firebase f = mEngine.newFirebase("https://neqo.firebaseio.com").child("drawing");

		FragmentManager fm = getFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		ft.add(R.id.fragment_holder, DrawingFragment.newInstance(f));
		ft.commit();
	}

}
