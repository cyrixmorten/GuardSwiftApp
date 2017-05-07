package com.guardswift.ui.view.card;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.common.collect.Lists;
import com.guardswift.R;
import com.guardswift.ui.view.answer.Answer;
import com.guardswift.ui.view.answer.EditTextFormView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by cyrix on 4/19/15.
 */
public class QuestionCardView extends LinearLayout implements Question {

    private String jsonKey;


    public static class Builder {


        private boolean showButtons = true;
        private QuestionCardView questionCardView;
        private List<Answer> answers;

        public Builder(Context context) {
            this.answers = Lists.newArrayList();
            this.questionCardView = new QuestionCardView(context);
        }

        public Builder jsonKey(String key) {
            questionCardView.setJsonKey(key);
            return this;
        }

        public Builder question(String questionText) {
            questionCardView.setQuestion(questionText);
            return this;
        }

        public Builder showQuestion(boolean show) {
            questionCardView.setShowQuestion(show);
            return this;
        }

        public Builder showButtons(boolean show) {
            showButtons = show;
            questionCardView.setShowButtons(show);
            return this;
        }

        public Builder addAnswers(Answer... answers) {
            this.answers.addAll(Lists.newArrayList(answers));
            return this;
        }

        public Builder showAnswersOnYesClicked(boolean value) {
            questionCardView.setShowAnswersOnYesClicked(value);
            return this;
        }

        public Builder yesClickListener(OnClickListener clickListener) {
            questionCardView.setYesClickedListener(clickListener);
            return this;
        }

        public Builder noClickListener(OnClickListener clickListener) {
            questionCardView.setNoClickedListener(clickListener);
            return this;
        }

        public Builder assumeYes() {
            questionCardView.answerYes();
            return this;
        }

        public Builder assumeNo() {
            questionCardView.answerNo();
            return this;
        }

        public QuestionCardView build(boolean showAnswers) {
            questionCardView.setAnswers(answers);
            if (showAnswers) {
                questionCardView.showAnswers();
            }
            return questionCardView;
        }

        public QuestionCardView build() {
            if (showButtons) {
                return build(false);
            }
            // if there is not yes/no buttons, show answers
            return build(true);
        }



    }

    @BindView(R.id.cardview)
    CardView cardview;

    @BindView(R.id.cardview_question_layout)
    RelativeLayout question_layout;

    @BindView(R.id.cardview_answer_layout)
    LinearLayout answer_layout;

    @BindView(R.id.cardview_question_tv)
    TextView tv_question;

    @BindView(R.id.cardview_buttons)
    LinearLayout buttons;


    @BindView(R.id.cardview_button_yes)
    Button button_yes;

    @BindView(R.id.cardview_button_no)
    Button button_no;

    private boolean showAnswersOnYesClicked = true;

    private boolean yesAnswered = false;

    private OnClickListener yesClickListener;
    private OnClickListener noClickListener;

    private List<Answer> answers = Lists.newArrayList();

    public QuestionCardView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.QuestionCardView, 0, 0);

        setOrientation(LinearLayout.HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.cardview_question, this, true);

        ButterKnife.bind(v);


        try {
            String questionText = a.getString(R.styleable.QuestionCardView_questionText);
            boolean showQuestion = a.getBoolean(R.styleable.QuestionCardView_showQuestion, true);
            boolean showButtons = a.getBoolean(R.styleable.QuestionCardView_showButtons, true);

            setShowQuestion(showQuestion);
            setShowButtons(showButtons);
            setQuestion(questionText);


        } finally {
            a.recycle();
        }
    }

    public QuestionCardView(Context context) {
        this(context, null);
    }


    @OnClick(R.id.cardview_button_yes)
    public void yes(Button v) {

        answerYes();

        if (yesClickListener != null) {
            yesClickListener.onClick(v);
        }

    }

    public void answerYes() {

        if (showAnswersOnYesClicked) {
            showAnswers();
        } else {
            answer_layout.removeAllViews();
        }

        yesAnswered = true;
        button_yes.setBackgroundColor(getResources().getColor(R.color.bootstrap_brand_success));
        button_no.setBackgroundColor(getResources().getColor(R.color.gray_btn_bg_color));
    }

    @OnClick(R.id.cardview_button_no)
    public void no(Button v) {

        answerNo();


        if (noClickListener != null) {
            noClickListener.onClick(v);
        }

    }

    public void setAnswer(boolean answerValue) {
        if (answerValue)
            answerYes();
        else
            answerNo();
    }

    public void answerNo() {

        if (!showAnswersOnYesClicked) {
            showAnswers();
        } else {
            answer_layout.removeAllViews();
        }

        yesAnswered = false;
        button_yes.setBackgroundColor(getResources().getColor(R.color.gray_btn_bg_color));
        button_no.setBackgroundColor(getResources().getColor(R.color.bootstrap_brand_danger));
    }


    private void showAnswers() {
        if (answer_layout.getChildCount() == 0) {
            for (Answer answer : answers) {
//                if (answer.getView().getParent() != null) {
//                    // check if already have parent
//                    ((ViewGroup)answer.getView().getParent()).removeView(answer.getView());
//                }
                answer_layout.addView(answer.getView());
            }
        }

//        if (answers.isEmpty()) {
////            cardview.setCardBackgroundColor(getResources().getColor(R.color.White));
//            questionAnswered();
//        } else {
        if (!answers.isEmpty()) {
            Answer lastAnswer = answers.get(answers.size() - 1);
            if (lastAnswer instanceof EditTextFormView) {
                ((EditTextFormView)lastAnswer).setIMEOptions(EditorInfo.IME_ACTION_DONE);
            }
        }
//        }
    }


//    private void questionAnswered() {
//        if (yesClickListener != null) {
//            yesClickListener.onClick(this);
//        }
//    }


    public String getQuestion() {
        return tv_question.getText().toString();
    }

    public void setShowAnswersOnYesClicked(boolean value) {
        showAnswersOnYesClicked = value;
    }

    public void setQuestion(String question) {
        tv_question.setText(question);
    }

    public void setShowQuestion(boolean showQuestion) {
        int visibility = (showQuestion) ? View.VISIBLE : View.GONE;
        question_layout.setVisibility(visibility);
    }

    public void setShowButtons(boolean showButtons) {
        int visibility = (showButtons) ? View.VISIBLE : View.GONE;
        buttons.setVisibility(visibility);
    }


    public void setJsonKey(String key) {
        this.jsonKey = key;
    }

    public String getJsonKey() {
        return jsonKey;
    }

    public boolean getAnswer() {
        return yesAnswered;
    }

    @Override
    public void setYesClickedListener(OnClickListener clickedListener) {
        this.yesClickListener = clickedListener;
    }

    @Override
    public void setNoClickedListener(OnClickListener clickedListener) {
        this.noClickListener = clickedListener;
    }

    @Override
    public void setAnswers(List<Answer> answers) {
        this.answers = answers;
    }

    @Override
    public List<Answer> getAnswers() {
        return answers;
    }


    @Override
    public View getView() {
        return this;
    }
}
