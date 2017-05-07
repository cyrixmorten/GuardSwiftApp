package com.guardswift.ui.view.answer;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.guardswift.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by cyrix on 4/19/15.
 */
public class EditTextFormView extends LinearLayout implements Answer {

    private String jsonKey;

    public static class Builder {

        private EditTextFormView formView;

        public Builder(Context context) {
            this.formView = new EditTextFormView(context);
        }

        public Builder allowScrollInsideScrollview() {
            formView.allowScrollInsideScrollview();
            return this;
        }

        public Builder description(String desc) {
            formView.setFormDescription(desc);
            return this;
        }

        public Builder value(String value) {
            formView.setFormValue(value);
            return this;
        }

        public Builder inputType(int inputType) {
            formView.setInputType(inputType);
            return this;
        }

        public Builder imeOptions(int imeOptions) {
            formView.setIMEOptions(imeOptions);
            return this;
        }

        public EditTextFormView build() {
            return formView;
        }

    }
    @BindView(R.id.tv)
    TextView formDescription;

    @BindView(R.id.edittext)
    EditText editText;

//    @BindView(R.id.submit_button)
//    Button saveButton;

    private OnClickListener saveClickListener;

    public EditTextFormView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.EditTextFormView, 0, 0);

        setOrientation(LinearLayout.HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.view_edittext_form, this, true);

        ButterKnife.bind(v);

        try {
            String descriptionText = a.getString(R.styleable.EditTextFormView_descriptionText);

            setFormDescription(descriptionText);
        } finally {
            a.recycle();
        }


    }

    public EditTextFormView(Context context) {
        this(context, null);
    }

    public void allowScrollInsideScrollview() {
        // allow scroll even when embedded in scrollview
        editText.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (v.getId() == R.id.edittext) {
                    v.getBackground().setColorFilter(Color.YELLOW, PorterDuff.Mode.SRC_ATOP);
//                    v.setBackgroundColor(getResources().getColor(R.color.bright_foreground_material_light));
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    switch (event.getAction() & MotionEvent.ACTION_MASK) {
                        case MotionEvent.ACTION_UP:
                            v.getBackground().setColorFilter(null);
//                            v.setBackground(new ColorDrawable(Color.TRANSPARENT));
                            v.getParent().requestDisallowInterceptTouchEvent(false);
                            break;
                    }
                }
                return false;
            }
        });
    }

    public void setFormValue(String value) {
        if (value != null)
        editText.setText(value);
    }

    public void setIMEOptions(int imeOptions) {
        Log.e("EditTextFormView", "editText.getInputType() " + editText.getInputType());
        if (imeOptions == EditorInfo.IME_ACTION_DONE) {
            if (editText.getInputType() == InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE ||
                    editText.getInputType() == InputType.TYPE_TEXT_FLAG_MULTI_LINE) {
                // we want to be able to enter multiple lines
                return;
            }
        }
        editText.setImeOptions(imeOptions);
    }

    public void setInputType(int inputType) {
        editText.setInputType(inputType);
    }

//    public void setLargeInput(boolean largeInput) {
//        if (largeInput) {
//            editText.setMinLines(6);
//        } else {
//            editText.setLines(1);
//        }
//    }
    public void setFormDescription(String descriptionText) {
        formDescription.setText(descriptionText + ":");
    }

//    @Override
//    public void setSaveClickedListener(OnClickListener clickedListener) {
//        saveButton.setOnClickListener(clickedListener);
//    }
//
//    @Override
//    public void setShowSaveButton(boolean show) {
//        int visibility = (show) ? View.VISIBLE : View.GONE;
//        saveButton.setVisibility(visibility);
//    }

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

    public String getDescription() {
        return formDescription.getText().toString();
    }
    @Override
    public String getAnswer() {
        return editText.getText().toString();
    }
}
