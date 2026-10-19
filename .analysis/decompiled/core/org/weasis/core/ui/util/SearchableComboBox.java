/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.formdev.flatlaf.icons.FlatClearIcon
 *  com.formdev.flatlaf.icons.FlatSearchIcon
 */
package org.weasis.core.ui.util;

import com.formdev.flatlaf.icons.FlatClearIcon;
import com.formdev.flatlaf.icons.FlatSearchIcon;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Objects;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class SearchableComboBox<E>
extends JComboBox<E> {
    private ComboBoxModel<E> baseModel;
    private final AtomicBoolean isFiltering = new AtomicBoolean(false);
    private transient Function<? super E, String> matcher;
    private transient Consumer<String> searchCallback;

    public SearchableComboBox() {
        this(new DefaultComboBoxModel());
    }

    public SearchableComboBox(E[] items) {
        this(new DefaultComboBoxModel<E>(items));
    }

    public SearchableComboBox(Vector<E> items) {
        this(new DefaultComboBoxModel<E>(items));
    }

    public SearchableComboBox(ComboBoxModel<E> comboBoxModel) {
        super(comboBoxModel);
        this.baseModel = comboBoxModel;
        this.addItemListener(e -> {
            if (e.getStateChange() == 1 && !this.isFiltering.get() && this.getModel() != this.baseModel) {
                Object item = this.getSelectedItem();
                super.setModel(this.baseModel);
                this.setSelectedItem(item);
            }
        });
        this.setEditable(true);
        final JTextField editorComponent = (JTextField)this.editor.getEditorComponent();
        JButton clearButton = new JButton((Icon)new FlatClearIcon());
        editorComponent.putClientProperty("JTextField.leadingComponent", new JLabel((Icon)new FlatSearchIcon(true)));
        editorComponent.putClientProperty("JTextField.trailingComponent", clearButton);
        editorComponent.addKeyListener(new KeyAdapter(this){
            final /* synthetic */ SearchableComboBox this$0;
            {
                SearchableComboBox searchableComboBox = this$0;
                Objects.requireNonNull(searchableComboBox);
                this.this$0 = searchableComboBox;
            }

            @Override
            public void keyReleased(KeyEvent ke) {
                this.this$0.filter(editorComponent.getText());
            }
        });
        clearButton.addActionListener(actionEvent -> {
            editorComponent.setText("");
            if (this.getModel() != this.baseModel) {
                super.setModel(this.baseModel);
            }
            this.setSelectedItem(null);
            if (this.searchCallback != null) {
                this.searchCallback.accept("");
            }
        });
    }

    public void setMatcher(Function<? super E, String> matcher) {
        this.matcher = matcher;
    }

    public void setSearchCallback(Consumer<String> searchCallback) {
        this.searchCallback = searchCallback;
    }

    private String matchText(E value) {
        if (value == null) {
            return null;
        }
        return this.matcher != null ? this.matcher.apply(value) : value.toString();
    }

    public boolean isFiltering() {
        return this.isFiltering.get();
    }

    @Override
    public void setModel(ComboBoxModel<E> aModel) {
        this.baseModel = aModel;
        super.setModel(aModel);
    }

    protected void filter(String text) {
        this.isFiltering.set(true);
        Vector entriesFiltered = new Vector();
        String compText = text.trim().toLowerCase();
        if (compText.length() > 1) {
            for (int i = 0; i < this.baseModel.getSize(); ++i) {
                Object val = this.baseModel.getElementAt(i);
                String searchable = this.matchText(val);
                if (searchable == null || !searchable.toLowerCase().contains(compText)) continue;
                entriesFiltered.add(val);
            }
        }
        if (this.searchCallback != null) {
            this.searchCallback.accept(compText);
        }
        SwingUtilities.invokeLater(() -> {
            if (entriesFiltered.isEmpty()) {
                if (this.getModel() != this.baseModel) {
                    super.setModel(this.baseModel);
                }
                this.setSelectedItem(text);
                if (compText.length() > 1) {
                    this.getUI().setPopupVisible(this, false);
                }
            } else {
                DefaultComboBoxModel model = new DefaultComboBoxModel(entriesFiltered);
                model.setSelectedItem(entriesFiltered.getFirst());
                super.setModel(model);
                this.setSelectedItem(text);
                this.getUI().setPopupVisible(this, true);
            }
            this.isFiltering.set(false);
        });
    }
}

