package pro.schmid.android.firedrawer;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

public class ChildInput extends Fragment {

	public ChildInput() {
	}

	public static final ChildInput newInstance() {
		return new ChildInput();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.child_input, container, false);

		Button b = (Button) v.findViewById(R.id.connect_button);
		b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v2) {
				EditText et = (EditText) v.findViewById(R.id.child_name);

				String childName = et.getText().toString();

				InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(et.getWindowToken(), 0);

				((DrawerActivity) getActivity()).connect(childName);
			}
		});

		return v;
	}
}
