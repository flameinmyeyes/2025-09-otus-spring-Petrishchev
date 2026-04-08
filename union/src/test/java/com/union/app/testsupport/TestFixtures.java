package com.union.app.testsupport;

import com.union.app.messaging.model.Event;
import com.union.app.messaging.model.Message;
import com.union.app.messaging.model.Poll;
import com.union.app.messaging.model.PollOption;
import com.union.app.messaging.model.Union;
import com.union.app.messaging.model.Vote;
import com.union.app.user.model.User;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class TestFixtures {
    private TestFixtures() {
    }

    public static User user(long id, String phoneNumber) {
        return user(id, phoneNumber, "Full Name " + id, "Status " + id);
    }

    public static User user(long id, String phoneNumber, String fullName, String status) {
        User user = new User();
        user.setId(id);
        user.setPhoneNumber(phoneNumber);
        user.setPassword("encoded-password-" + id);
        user.setFullName(fullName);
        user.setStatus(status);
        return user;
    }

    public static Union union(long id, String name, User creator, User... members) {
        Union union = new Union();
        union.setId(id);
        union.setName(name);
        union.setDescription("Description " + id);
        union.setCreator(creator);
        Set<User> memberSet = new HashSet<>();
        memberSet.addAll(Arrays.asList(members));
        union.setMembers(memberSet);
        return union;
    }

    public static Message message(long id, String content, User sender, Union union, User receiver, LocalDateTime timestamp) {
        Message message = new Message();
        message.setId(id);
        message.setContent(content);
        message.setSender(sender);
        message.setUnion(union);
        message.setReceiver(receiver);
        message.setTimestamp(timestamp);
        return message;
    }

    public static Vote vote(long id, User user) {
        Vote vote = new Vote();
        vote.setId(id);
        vote.setUser(user);
        return vote;
    }

    public static PollOption option(long id, String text, Vote... votes) {
        PollOption option = new PollOption();
        option.setId(id);
        option.setText(text);
        option.setVotes(new java.util.ArrayList<>(List.of(votes)));
        return option;
    }

    public static Poll poll(long id, String question, Union union, User creator,
                            LocalDateTime createdAt, LocalDateTime expiresAt, PollOption... options) {
        Poll poll = new Poll();
        poll.setId(id);
        poll.setQuestion(question);
        poll.setUnion(union);
        poll.setCreatedBy(creator);
        poll.setCreatedAt(createdAt);
        poll.setOptions(new java.util.ArrayList<>(List.of(options)));
        return poll;
    }

    public static Event event(long id, String title, String description, LocalDateTime eventDate,
                              String location, Union union, User creator, LocalDateTime createdAt) {
        Event event = new Event();
        event.setId(id);
        event.setTitle(title);
        event.setDescription(description);
        event.setLocation(location);
        event.setUnion(union);
        event.setCreatedBy(creator);
        event.setCreatedAt(createdAt);
        return event;
    }
}
