package pro.schmid.android.firedrawer;

import pro.schmid.android.androidonfire.Firebase;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class DrawingFragment extends Fragment {

	private final Firebase mFirebase;

	private DrawingFragment(Firebase firebase) {
		this.mFirebase = firebase;
	}

	public static final DrawingFragment newInstance(Firebase firebase) {
		return new DrawingFragment(firebase);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.frag_test, container, false);
	}

}
