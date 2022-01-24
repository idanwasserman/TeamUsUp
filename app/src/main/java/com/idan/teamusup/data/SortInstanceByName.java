package com.idan.teamusup.data;

import java.util.Comparator;

public class SortInstanceByName implements Comparator<Instance> {
    @Override
    public int compare(Instance o1, Instance o2) {
        return o1.getName().toLowerCase()
                .compareTo(
                        o2.getName().toLowerCase());
    }
}
