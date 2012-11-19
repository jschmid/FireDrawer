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
	private DrawingFragment mFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mEngine = FirebaseEngine.getInstance();
		mEngine.setLoadedListener(new FirebaseLoaded() {
			@Override
			public void firebaseLoaded() {
				FragmentManager fm = getFragmentManager();
				FragmentTransaction ft = fm.beginTransaction();
				ft.add(R.id.fragment_holder, ChildInput.newInstance());
				ft.commit();
			}
		});
		mEngine.loadEngine(this);
	}

	protected void connect(String childName) {
		addFragment(childName);
	}

	private void addFragment(String childName) {
		Firebase f = mEngine.newFirebase("https://neqo.firebaseio.com").child("drawing").child(childName);

		mFragment = DrawingFragment.newInstance(f);

		FragmentManager fm = getFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		ft.replace(R.id.fragment_holder, mFragment);
		ft.addToBackStack(null);
		ft.commit();
	}

}
