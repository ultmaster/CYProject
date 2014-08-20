package net.ultech.cyproject.ui.fragment;

import net.ultech.cyproject.R;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class AboutUs extends Fragment {

	@SuppressLint("InflateParams")
    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.aboutus_layout, null);
	}

}
