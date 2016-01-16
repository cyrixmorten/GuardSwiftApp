package com.guardswift.ui.dialog;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.codetroopers.betterpickers.numberpicker.NumberPickerBuilder;
import com.codetroopers.betterpickers.numberpicker.NumberPickerDialogFragment;
import com.guardswift.R;
import com.guardswift.persistence.parse.data.client.Client;
import com.guardswift.persistence.parse.data.client.ClientContact;
import com.guardswift.util.Intents;

import java.util.List;

import butterknife.ButterKnife;

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

        public MaterialDialog.Builder okCancel(int title, String content, MaterialDialog.SingleButtonCallback onPositive) {
            return new MaterialDialog.Builder(context)
                    .title(title)
                    .content(content)
                    .positiveText(android.R.string.ok)
                    .negativeText(android.R.string.cancel)
                    .onPositive(onPositive);
        }

        public MaterialDialog.Builder clientContacts(Client client) {

            final List<ClientContact> contacts = client.getContactsWithNames();

            LinearLayout layout = new LinearLayout(context);
            layout.setLayoutParams(new LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            layout.setOrientation(LinearLayout.VERTICAL);

            LayoutInflater li = LayoutInflater.from(context);

            TextView callContactTip = new TextView(context);
            callContactTip.setText(context.getString(R.string.tip_click_contact_to_call));
            layout.addView(callContactTip);

            for (final ClientContact contact: contacts) {
                View contactView = li.inflate(R.layout.gs_view_clientcontact, null);
                TextView name = ButterKnife.findById(contactView, R.id.tvName);
                TextView phone = ButterKnife.findById(contactView, R.id.tvPhoneNumber);
                TextView desc = ButterKnife.findById(contactView, R.id.tvDescription);

                name.setText(contact.getName());
                phone.setText(contact.getPhoneNumber());
                desc.setText(contact.getDesc());

                final String phoneNumber = contact.getPhoneNumber();
                contactView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intents.dialPhoneNumber(context, phoneNumber);
                    }
                });
                layout.addView(contactView);
            }

            return new MaterialDialog.Builder(context)
                    .title(client.getName())
                    .customView(layout, true);

        }
    }
}
