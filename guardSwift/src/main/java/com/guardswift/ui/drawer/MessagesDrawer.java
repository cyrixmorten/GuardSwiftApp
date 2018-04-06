package com.guardswift.ui.drawer;


import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.PopupMenu;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.common.collect.Lists;
import com.guardswift.R;
import com.guardswift.persistence.parse.data.Guard;
import com.guardswift.persistence.parse.misc.Message;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.ui.dialog.CommonDialogsBuilder;
import com.guardswift.ui.view.drawer.OverflowMenuDrawerItem;
import com.guardswift.util.ToastHelper;
import com.guardswift.util.Util;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.util.Date;
import java.util.List;

import bolts.Continuation;
import bolts.Task;

public class MessagesDrawer {

    private static String TAG = MessagesDrawer.class.getSimpleName();

    private final Context context;
    private final Drawer drawer;

    private String groupId;
    private int newMessages;

    public MessagesDrawer(Context context, Drawer drawer) {
        this.context = context;
        this.drawer = drawer;
        this.groupId = "";

        drawer.removeAllStickyFooterItems();
        drawer.addStickyFooterItem(addMessageItem());

        drawer.getDrawerLayout().setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }


    public void open() {
        drawer.openDrawer();
        GuardSwiftApplication.hasReadGroups.add(groupId);
    }

    public void close() {
        if (drawer.isDrawerOpen()) {
            drawer.closeDrawer();
        }
    }

    public Task<List<Message>> loadMessages(String groupId) {
        this.groupId = groupId;

        if (!enableMessagesDrawer()) {
            List<Message> empty = Lists.newArrayList();
            return Task.forResult(empty);
        }


        return Message.getQueryBuilder(false, groupId).build().addDescendingOrder(Message.createdAt).findInBackground().onSuccess(new Continuation<List<Message>, List<Message>>() {
            @Override
            public List<Message> then(Task<List<Message>> task) {
                List<Message> messages = task.getResult();

                updateMessages(messages);
                countNewMessages(messages);

                return task.getResult();
            }
        });
    }

    public int getNewMessagesCount() {
        return newMessages;
    }

    private void updateMessages(final List<Message> messages) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                drawer.removeAllItems();

                for (Message message : messages) {
                    drawer.addItem(createMessageItem(message));
                }
            }
        });
    }

    private PrimaryDrawerItem addMessageItem() {
        PrimaryDrawerItem addMessageItem = new PrimaryDrawerItem().withName(R.string.add_message);

        addMessageItem.withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
            @Override
            public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {

                addMessageDialog();

                return false;
            }
        });

        return addMessageItem;
    }

    private OverflowMenuDrawerItem createMessageItem(final Message message) {
        final OverflowMenuDrawerItem drawerItem = new OverflowMenuDrawerItem();

        drawerItem.withName(message.getGuard().getName());

        Date createdAt = (message.getCreatedAt() != null) ? message.getCreatedAt() : new Date();
        drawerItem.withBottomEndCaption(Util.relativeTimeString(createdAt));

        drawerItem.withDescription(message.getMessage());
        drawerItem.withSelectable(false);
        drawerItem.withDisabledTextColor(ContextCompat.getColor(context, R.color.md_black_1000));


        if (GuardSwiftApplication.getLoggedIn() == message.getGuard()) {
            drawerItem.withMenu(R.menu.message_options);
        }

        drawerItem.withOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.edit: {
                        editMessageDialog(drawerItem, message);
                        break;
                    }
                    case R.id.delete: {
                        deleteMessageDialog(drawerItem, message);
                        break;
                    }
                }
                return false;
            }
        });

        return drawerItem;
    }

    private void addMessageDialog() {
        openMessageDialog(null, null);
    }

    private void editMessageDialog(OverflowMenuDrawerItem drawerItem, Message message) {
        openMessageDialog(drawerItem, message);
    }

    private void openMessageDialog(final OverflowMenuDrawerItem drawerItem, final Message editMessage) {
        final boolean editMode = drawerItem != null && editMessage != null;

        MaterialDialog dialog = new MaterialDialog.Builder(context)
                .title(R.string.add_message)
                .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)
                .input(context.getString(R.string.message), (editMode) ? editMessage.getMessage() : context.getString(R.string.input_empty), new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {

                        String editTextString = input.toString();
                        if (!editTextString.isEmpty()) {
                            if (editMode) {
                                editMessage.setMessage(editTextString);
                                editMessage.saveEventually();

                                drawer.updateItem(drawerItem.withDescription(editTextString));
                            } else {
                                Message message = Message.newInstance(groupId, editTextString);
                                message.saveEventually();

                                drawer.addItemAtPosition(createMessageItem(message), 0);
                                drawer.closeDrawer();

                                ToastHelper.toast(context, context.getString(R.string.message_saved));
                            }
                        }
                    }
                }).negativeText(android.R.string.cancel).build();

        EditText editText = dialog.getInputEditText();

        if (editText != null) {
            editText.setSingleLine(true);
//          editText.setLines(4); // desired number of lines
            editText.setHorizontallyScrolling(false);
            editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        }

        dialog.show();
    }

    private void deleteMessageDialog(final OverflowMenuDrawerItem drawerItem, final Message message) {

        new CommonDialogsBuilder.MaterialDialogs(context).yesNo(R.string.confirm_delete_message, message.getMessage(), new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                int position = drawer.getPosition(drawerItem);
                drawer.removeItemByPosition(position);

                message.deleteEventually();
            }
        }).show();
    }


    private void countNewMessages(List<Message> messages) {
        if (GuardSwiftApplication.hasReadGroups.contains(groupId)) {
            newMessages = 0;
        }

        int newCount = 0;

        Guard guard = GuardSwiftApplication.getLoggedIn();
        Date lastLogout = guard.getLastLogout();

        for (Message message : messages) {
            if (message.getGuard() == guard) {
                continue;
            }
            if (message.getCreatedAt().after(lastLogout)) {
                newCount++;
            }
        }

        newMessages = newCount;
    }


    private boolean enableMessagesDrawer() {
        return !groupId.isEmpty();
    }


}
