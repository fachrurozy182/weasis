/*
 * Decompiled with CFR 0.152.
 */
package eu.essilab.lablib.checkboxtree;

import javax.swing.DefaultButtonModel;

public class QuadristateButtonModel
extends DefaultButtonModel {
    public QuadristateButtonModel() {
        this.setState(State.UNCHECKED);
    }

    public State getState() {
        if (this.isSelected() && !this.isArmed()) {
            return State.CHECKED;
        }
        if (this.isSelected() && this.isArmed()) {
            return State.GREY_CHECKED;
        }
        if (!this.isSelected() && this.isArmed()) {
            return State.GREY_UNCHECKED;
        }
        return State.UNCHECKED;
    }

    public void nextState() {
        switch (this.getState()) {
            case UNCHECKED: {
                this.setState(State.CHECKED);
                break;
            }
            case CHECKED: {
                this.setState(State.GREY_UNCHECKED);
                break;
            }
            case GREY_UNCHECKED: {
                this.setState(State.GREY_CHECKED);
                break;
            }
            case GREY_CHECKED: {
                this.setState(State.UNCHECKED);
            }
        }
    }

    @Override
    public void setArmed(boolean b) {
    }

    public void setState(State state) {
        switch (state) {
            case UNCHECKED: {
                super.setArmed(false);
                this.setPressed(false);
                this.setSelected(false);
                break;
            }
            case CHECKED: {
                super.setArmed(false);
                this.setPressed(false);
                this.setSelected(true);
                break;
            }
            case GREY_UNCHECKED: {
                super.setArmed(true);
                this.setPressed(true);
                this.setSelected(false);
                break;
            }
            case GREY_CHECKED: {
                super.setArmed(true);
                this.setPressed(true);
                this.setSelected(true);
            }
        }
    }

    public static enum State {
        CHECKED,
        GREY_CHECKED,
        GREY_UNCHECKED,
        UNCHECKED;

    }
}

