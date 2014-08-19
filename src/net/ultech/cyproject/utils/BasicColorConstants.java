package net.ultech.cyproject.utils;

import net.ultech.cyproject.R;
import android.content.Context;

public class BasicColorConstants {

	public static String stringColorRed;
	public static String stringColorBlue;
	public static int colorRed;
	public static int colorBlue;
	public static int colorFocused;
	public static int colorBackground;
	public static int colorDefault;
	public static int colorGrey;

	public static void setColor(Context context, int id) {
		switch (id) {
		case R.style.BlueAndGreenTheme:
			stringColorBlue = context.getResources().getString(
					R.string.bg_string_color_blue);
			stringColorRed = context.getResources().getString(
					R.string.bg_string_color_red);
			colorBlue = context.getResources().getColor(R.color.bg_color_blue);
			colorRed = context.getResources().getColor(R.color.bg_color_red);
			colorFocused = context.getResources().getColor(
					R.color.bg_background_focused);
			colorBackground = context.getResources().getColor(
					R.color.bg_background);
			colorDefault = context.getResources().getColor(R.color.bg_black);
			colorGrey = context.getResources().getColor(R.color.bg_grey);
			break;
		case R.style.YellowAndOrangeTheme:
			stringColorBlue = context.getResources().getString(
					R.string.yo_string_color_blue);
			stringColorRed = context.getResources().getString(
					R.string.yo_string_color_red);
			colorBlue = context.getResources().getColor(R.color.yo_color_blue);
			colorRed = context.getResources().getColor(R.color.yo_color_red);
			colorFocused = context.getResources().getColor(
					R.color.yo_background_focused);
			colorBackground = context.getResources().getColor(
					R.color.yo_background);
			colorDefault = context.getResources().getColor(R.color.yo_black);
			colorGrey = context.getResources().getColor(R.color.yo_grey);
			break;
		case R.style.DarkOceanTheme:
			stringColorBlue = context.getResources().getString(
					R.string.dk_string_color_blue);
			stringColorRed = context.getResources().getString(
					R.string.dk_string_color_red);
			colorBlue = context.getResources().getColor(R.color.dk_color_blue);
			colorRed = context.getResources().getColor(R.color.dk_color_red);
			colorFocused = context.getResources().getColor(
					R.color.dk_background_focused);
			colorBackground = context.getResources().getColor(
					R.color.dk_background);
			colorDefault = context.getResources().getColor(R.color.dk_black);
			colorGrey = context.getResources().getColor(R.color.dk_grey);
			break;
		}
	}
}
