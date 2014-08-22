package net.ultech.cyproject.ui.fragment;

import net.ultech.cyproject.R;
import net.ultech.cyproject.utils.Constants.UpdateRelated;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class AboutUs extends Fragment {

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View about = inflater.inflate(R.layout.aboutus_layout, null);
        TextView textVersion = (TextView) about.findViewById(R.id.text_version);
        TextView textDate = (TextView) about.findViewById(R.id.text_release);
        PackageInfo info;
        try {
            info = getActivity().getPackageManager().getPackageInfo(
                    getActivity().getPackageName(), 0);
        } catch (NameNotFoundException e) {
            throw new AssertionError("Are you kidding me?");
        }
        textVersion.setText(getString(R.string.contact_program_version)
                + info.versionName + "/" + info.versionCode);
        textDate.setText(getString(R.string.contact_release_date)
                + UpdateRelated.RELEASE_DATE);
        return about;
    }
}
