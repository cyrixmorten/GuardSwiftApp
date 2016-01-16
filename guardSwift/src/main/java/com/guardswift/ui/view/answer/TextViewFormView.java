package com.guardswift.ui.view.answer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.guardswift.R;
import com.guardswift.util.Util;

import java.util.Date;

/**
 * Created by cyrix on 4/19/15.
 */
public class TextViewFormView extends TextView implements Answer {

    private String jsonKey;

    public static class Builder {

        private TextViewFormView formView;

        public Builder(Context context) {
            this.formView = new TextViewFormView(context);
        }

        public Builder jsonKey(String jsonKey) {
            formView.setJsonKey(jsonKey);
            return this;
        }


        public Builder text(String text) {
            formView.setText(text);
            return this;
        }

        public Builder largeText() {
            formView.setTextLarge();
            return this;
        }

        public Builder textDateHoursMinutes(Date date, String defaultText) {
            formView.textDateHoursMinutes(date, defaultText);
            return this;
        }

        public Builder onClick(OnClickListener clickListener) {
            formView.setOnClickListener(clickListener);
            return this;
        }

        public TextViewFormView build() {
            return formView;
        }

    }

    private Context context;


    public TextViewFormView(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.context = context;
    }

    public TextViewFormView(Context context) {
        this(context, null);

        this.context = context;
    }

    private void setTextLarge() {
        setTextAppearance(context, android.R.style.TextAppearance_Large);
    }

    public void textDateHoursMinutes(Date date, String defaultText) {
        if (date != null) {
            setTextValue(Util.dateFormatHourMinutes().format(date), "");
        } else {
            setTextValue("", defaultText);
        }
    }

    public void setTextValue(String text, String defaultText) {
        if (!text.isEmpty()) {
            setTextColor(getResources().getColor(R.color.text_color));
            setText(text);
        } else {
            setTextColor(getResources().getColor(R.color.bootstrap_brand_danger));
            setText(defaultText);
        }
    }

    public void setJsonKey(String key) {
        this.jsonKey = key;
    }

    public String getJsonKey() {
        return jsonKey;
    }


    @Override
    public View getView() {
        return this;
    }

    @Override
    public String getAnswer() {
        return getText().toString();
    }
}
