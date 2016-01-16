//package com.guardswift.ui.adapters.parse;
//
//import android.content.Context;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ArrayAdapter;
//import android.widget.TextView;
//
//import com.beardedhen.androidbootstrap.BootstrapButton;
//import com.guardswift.R;
//import com.guardswift.persistence.parse.data.Guard;
//import com.guardswift.persistence.parse.data.Message;
//import com.guardswift.util.Util;
//
//import java.util.Date;
//import java.util.List;
//
//import butterknife.Bind;
//import butterknife.ButterKnife;
//import butterknife.OnClick;
//
//public class MessagesAdapter extends ArrayAdapter<Message> {
//
//    private static final String TAG = MessagesAdapter.class.getSimpleName();
//
//    private final Context context;
//    private final Guard guard;
//    private final List<Message> values;
//
//    public MessagesAdapter(Context context, Guard guard, List<Message> values) {
//        super(context, R.layout.view_adapter_item_message, values);
//        this.context = context;
//        this.values = values;
//        this.guard = guard;
//    }
//
//    @Bind(R.id.relativeTime)
//    TextView relativeTime;
//    @Bind(R.id.author)
//    TextView author;
//    @Bind(R.id.info)
//    TextView info;
//    @Bind(R.id.button_mark_read)
//    BootstrapButton button_mark_read;
//
//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//        LayoutInflater inflater = (LayoutInflater) context
//                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        View rowView = inflater.inflate(
//                R.layout.view_adapter_item_message, parent, false);
//
//        ButterKnife.bind(this, rowView);
//
//        Message message = values.get(position);
//
//        info.setText(message.getMessage());
//        author.setText(message.getAuthor());
//
//        Date timeDate = new Date();
//        if (message.getCreatedAt() != null) {
//            timeDate = message.getCreatedAt();
//        }
//
//        String relativeTimeString = Util.relativeTimeString(timeDate);
////        DateUtils.getRelativeTimeSpanString(
////                timeDate.getTime(), new Date().getTime(), 0L,
////                DateUtils.FORMAT_ABBREV_ALL).toString();
//        relativeTime.setText(relativeTimeString);
//
//        if (message.isReadBy(guard)) {
//            state_read(button_mark_read);
//        } else {
//            state_unread(button_mark_read);
//        }
//        button_mark_read.setTag(message);
//
//        return rowView;
//    }
//
//    private void state_read(BootstrapButton button) {
//        button_mark_read.setType("success");
//        button_mark_read.setEnabled(false);
//    }
//
//    private void state_unread(BootstrapButton button) {
//        button_mark_read.setType("warning");
//        button_mark_read.setEnabled(true);
//    }
//
//    @OnClick(R.id.button_mark_read)
//    public void markAsRead(BootstrapButton button) {
//        Message message = (Message) button.getTag();
//        message.markAsRead(guard);
//        message.pinThenSaveEventually();
////        notifyDataSetChanged();
////        EventBus.getDefault().post(new UpdateUIEvent());
//    }
//}
