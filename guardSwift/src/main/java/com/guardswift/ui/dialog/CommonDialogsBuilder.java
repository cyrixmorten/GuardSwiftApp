package com.guardswift.ui.dialog;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.codetroopers.betterpickers.numberpicker.NumberPickerBuilder;
import com.codetroopers.betterpickers.numberpicker.NumberPickerDialogFragment;
import com.guardswift.R;
import com.guardswift.persistence.parse.data.client.Client;

/**
 * Created by cyrix on 2/24/15.
 */
public class CommonDialogsBuilder {

    public static class BetterPicks {

        private final FragmentManager fragmentManager;

        public BetterPicks(FragmentManager fragmentManager) {
            this.fragmentManager = fragmentManager;
        }

        public NumberPickerBuilder enterEventAmount(String eventType, NumberPickerDialogFragment.NumberPickerDialogHandler numberPickerDialogHandler) {

    //        String eventName = eventType.getName().toLowerCase().substring(0, 8);
    //        eventName += (eventName.length() == 7) ? "&#8230;" : "";

            return new NumberPickerBuilder()
                    .setFragmentManager(fragmentManager)
                    .setStyleResId(R.style.BetterPickersDialogFragment_Light)
                    .addNumberPickerDialogHandler(numberPickerDialogHandler)
                    .setLabelText(eventType.toLowerCase())
                    .setPlusMinusVisibility(View.INVISIBLE)
                    .setDecimalVisibility(View.INVISIBLE);
        }


    }

    public static class MaterialDialogs {

        private Context context;

        public MaterialDialogs(Context activityContext) {
            this.context = activityContext;
        }

        public MaterialDialog.Builder ok(int title, String content, MaterialDialog.SingleButtonCallback onPositive) {
            return new MaterialDialog.Builder(context)
                    .title(title)
                    .content(content)
                    .positiveText(android.R.string.ok)
                    .onPositive(onPositive);
        }

        public MaterialDialog.Builder okCancel(int title, String content, MaterialDialog.SingleButtonCallback onPositive) {
            return new MaterialDialog.Builder(context)
                    .title(title)
                    .content(content)
                    .positiveText(android.R.string.ok)
                    .negativeText(android.R.string.cancel)
                    .onPositive(onPositive);
        }

        public MaterialDialog.Builder okCancel(int title, String content, MaterialDialog.SingleButtonCallback onPositive, final MaterialDialog.SingleButtonCallback onCancel) {
            return new MaterialDialog.Builder(context)
                    .title(title)
                    .content(content)
                    .positiveText(android.R.string.ok)
                    .negativeText(android.R.string.cancel)
                    .onPositive(onPositive)
                    .onNegative(onCancel)
                    .cancelable(false);

        }

        public MaterialDialog.Builder clientContacts(Client client) {

            LinearLayout layout = client.createContactsList(context);

            TextView callContactTip = new TextView(context);
            callContactTip.setText(context.getString(R.string.tip_click_contact_to_call));
            layout.addView(callContactTip, 0);

            return new MaterialDialog.Builder(context)
                    .title(client.getName())
                    .customView(layout, true);

        }

        public MaterialDialog.Builder infoDialog(int  title, int content) {
            return infoDialog(title, context.getString(content));
        }

        public MaterialDialog.Builder infoDialog(int  title, String content) {
            return new MaterialDialog.Builder(context)
                    .title(title)
                    .content(content);
        }

        public MaterialDialog.Builder missingInternetContent() {
            return infoDialog(R.string.message_no_internet_connection, "");
        }

        public MaterialDialog.Builder indeterminate() {
            return indeterminate(context.getString(R.string.working), context.getString(R.string.please_wait));
        }

        public MaterialDialog.Builder indeterminate(int content) {
            return indeterminate(context.getString(R.string.working), context.getString(content));
        }

        public MaterialDialog.Builder indeterminate(int title, int content) {
            return indeterminate(context.getString(title), context.getString(content));
        }

        public MaterialDialog.Builder indeterminate(String title, int content) {
            return indeterminate(title, context.getString(content));
        }

        public MaterialDialog.Builder indeterminate(String title, String content) {
            return new MaterialDialog.Builder(context)
                    .title(title)
                    .content(content)
                    .progress(true, 0);
        }



    }
}
