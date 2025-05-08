package com.team6.team6.question.domain;

import com.team6.team6.question.entity.Question;
import lombok.Value;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Value
public class Questions {

    List<Question> values;

    public static Questions of(List<Question> list) {
        return new Questions(List.copyOf(list));
    }

    public List<Question> getRandomSubset(int count) {
        if (values.size() <= count) return values;

        List<Question> copy = new ArrayList<>(values);
        Collections.shuffle(copy);
        return copy.subList(0, count);
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }

    public int size() {
        return values.size();
    }
}

