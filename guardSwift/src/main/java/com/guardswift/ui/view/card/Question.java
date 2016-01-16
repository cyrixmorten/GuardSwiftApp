package com.guardswift.ui.view.card;

import android.view.View;

import com.guardswift.ui.view.answer.Answer;

import java.util.List;

/**
 * Created by cyrix on 4/21/15.
 */
public interface Question {
    void setYesClickedListener(View.OnClickListener clickedListener);
    void setNoClickedListener(View.OnClickListener clickedListener);
    void setAnswers(List<com.guardswift.ui.view.answer.Answer> answers);
    List<Answer> getAnswers();

    View getView();
}
