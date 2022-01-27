package com.idan.teamusup.data;

import java.util.Comparator;

public class SortInstanceByLevel implements Comparator<Instance> {
    @Override
    public int compare(Instance o1, Instance o2) {
        Level l1, l2;

        try {
            l1 = (Level) o1.getAttributes().get(Constants.level.name());
        } catch (Exception e) {
            l1 = Level.Normal;
        }

        try {
            l2 = (Level) o2.getAttributes().get(Constants.level.name());
        } catch (Exception e) {
            l2 = Level.Normal;
        }

        int result;
        try {
            result = l1.name().compareTo(l2.name());
        } catch (Exception e) {
            result = 0;
        }
        return result;
    }
}
