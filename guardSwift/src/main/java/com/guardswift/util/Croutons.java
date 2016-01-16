//package com.guardswift.util;
//
//import android.app.ActionBar.LayoutParams;
//import android.app.Activity;
//import android.util.Log;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.view.ViewGroup;
//
//import com.guardswift.R;
//
//import java.lang.ref.WeakReference;
//
//import de.keyboardsurfer.android.widget.crouton.Configuration;
//import de.keyboardsurfer.android.widget.crouton.Crouton;
//import de.keyboardsurfer.android.widget.crouton.Style;
//
//public class Croutons {
//
//	public static class CroutonText {
//		public String text;
//		public final Style style;
//
//		public CroutonText(String text, Style style) {
//			super();
//			this.text = text;
//			this.style = style;
//		}
//
//	}
//
//	private final WeakReference<Activity> activityReference;
//	private final ViewGroup viewGroup;
//
//	private final Sounds sounds;
//
//	// private Crouton mCrouton; // currently showing
//
//	public Croutons(Activity activity, Sounds sounds) {
//		this.activityReference = new WeakReference<Activity>(activity);
//		this.viewGroup = null;
//		this.sounds = sounds;
//	}
//
//	public Croutons(Activity activity, ViewGroup viewGroup, Sounds sounds) {
//		this.activityReference = new WeakReference<Activity>(activity);
//		this.viewGroup = viewGroup;
//		this.sounds = sounds;
//	}
//
//	public void showCrouton(String text, Style baseStyle) {
//		Activity activity = activityReference.get();
//		if (activity == null) {
//			Log.e("Croutons", "Activity was null 1");
//			return;
//		}
//
//		Crouton crouton = Crouton.makeText(activity, text,
//				createStyle(baseStyle));
//
//		if (viewGroup != null)
//			crouton = Crouton.makeText(activity, text, createStyle(baseStyle),
//					viewGroup);
//
//		showCrouton(crouton);
//	}
//
//	private void showCrouton(final Crouton crouton) {
//		if (activityReference.get() == null) { // just to be safe
//			Log.e("Croutons", "Activity was null 2");
//			return;
//		}
//
//		sounds.playNotification(R.raw.checkpoint);
//
//		// mCrouton = crouton;
//		crouton.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				Crouton.hide(crouton);
//			}
//		});
//		crouton.show();
//	}
//
//	private Style createStyle(Style baseStyle) {
//		Style style = new Style.Builder(baseStyle)
//				.setTextAppearance(R.style.MediumBoldCroutonText)
//				.setHeight(
//						(viewGroup != null) ? LayoutParams.MATCH_PARENT
//								: LayoutParams.WRAP_CONTENT)
//				.setConfiguration(
//						new Configuration.Builder().setDuration(1000 * 10)
//								.build()).build();
//
//		return style;
//	}
//}
