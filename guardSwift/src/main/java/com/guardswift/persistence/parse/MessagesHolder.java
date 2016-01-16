package com.guardswift.persistence.parse;

import com.guardswift.persistence.parse.data.Guard;
import com.guardswift.persistence.parse.data.Message;

import java.util.List;

/**
 * Created by cyrix on 12/28/14.
 */
public interface MessagesHolder {
    public boolean hasUnreadMessagesFor(Guard guard);
    public List<Message> getMessages();
    public void addMessage(Message message);
    public ExtendedParseObject getParseObject();

    public static class Recent {

        private static String TAG = "MessageHolder.Recent";

        private static MessagesHolder selected;

        public static MessagesHolder getSelected() {
            return selected;
        }

        public static void setSelected(MessagesHolder selected) {
            Recent.selected = selected;
        }

    }
}
