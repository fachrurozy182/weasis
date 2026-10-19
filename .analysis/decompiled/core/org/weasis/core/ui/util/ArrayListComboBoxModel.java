/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.MutableComboBoxModel;

public class ArrayListComboBoxModel<E>
extends AbstractListModel<E>
implements MutableComboBoxModel<E>,
ComboBoxModel<E> {
    private Object selectedItem;
    private final List<E> items;
    private final Comparator<E> comparator;

    public ArrayListComboBoxModel() {
        this(null, null);
    }

    public ArrayListComboBoxModel(List<E> items) {
        this(items, null);
    }

    public ArrayListComboBoxModel(Comparator<E> comparator) {
        this(null, comparator);
    }

    public ArrayListComboBoxModel(List<E> items, Comparator<E> comparator) {
        this.items = items == null ? new ArrayList() : items;
        this.comparator = comparator;
    }

    @Override
    public Object getSelectedItem() {
        return this.selectedItem;
    }

    @Override
    public void setSelectedItem(Object newValue) {
        if (this.selectedItem != null && !this.selectedItem.equals(newValue) || this.selectedItem == null && newValue != null) {
            this.selectedItem = newValue;
            this.fireContentsChanged(this, -1, -1);
        }
    }

    @Override
    public int getSize() {
        return this.items.size();
    }

    @Override
    public E getElementAt(int index) {
        if (index >= 0 && index < this.items.size()) {
            return this.items.get(index);
        }
        return null;
    }

    public int getIndexOf(Object anObject) {
        return this.items.indexOf(anObject);
    }

    @Override
    public void addElement(E anObject) {
        int index = Collections.binarySearch(this.items, anObject, this.comparator);
        if (index < 0) {
            this.insertElementAt(anObject, -(index + 1));
        } else {
            this.insertElementAt(anObject, index);
        }
    }

    @Override
    public void insertElementAt(E anObject, int index) {
        this.items.add(index, anObject);
        this.fireIntervalAdded(this, index, index);
    }

    @Override
    public void removeElementAt(int index) {
        if (this.getElementAt(index) == this.selectedItem) {
            if (index == 0) {
                this.setSelectedItem(this.getSize() == 1 ? null : this.getElementAt(index + 1));
            } else {
                this.setSelectedItem(this.getElementAt(index - 1));
            }
        }
        this.items.remove(index);
        this.fireIntervalRemoved(this, index, index);
    }

    @Override
    public void removeElement(Object anObject) {
        int index = this.items.indexOf(anObject);
        if (index != -1) {
            this.removeElementAt(index);
        }
    }

    public void removeAllElements() {
        if (!this.items.isEmpty()) {
            int firstIndex = 0;
            int lastIndex = this.items.size() - 1;
            this.items.clear();
            this.selectedItem = null;
            this.fireIntervalRemoved(this, firstIndex, lastIndex);
        } else {
            this.selectedItem = null;
        }
    }
}

