/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.model.conditional;

import ch.qos.logback.core.model.Model;
import java.util.Objects;

public class IfModel
extends Model {
    private static final long serialVersionUID = 1516046821762377019L;
    String condition;
    BranchState branchState = null;

    @Override
    protected IfModel makeNewInstance() {
        return new IfModel();
    }

    @Override
    protected void mirror(Model that) {
        IfModel actual = (IfModel)that;
        super.mirror(actual);
        this.condition = actual.condition;
        this.branchState = actual.branchState;
    }

    public String getCondition() {
        return this.condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public BranchState getBranchState() {
        return this.branchState;
    }

    public void setBranchState(BranchState state) {
        this.branchState = state;
    }

    public void setBranchState(boolean booleanProxy) {
        if (booleanProxy) {
            this.setBranchState(BranchState.IF_BRANCH);
        } else {
            this.setBranchState(BranchState.ELSE_BRANCH);
        }
    }

    public void resetBranchState() {
        this.setBranchState(null);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " [condition=\"" + this.condition + "\"]";
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = super.hashCode();
        result = 31 * result + Objects.hash(new Object[]{this.branchState, this.condition});
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        IfModel other = (IfModel)obj;
        return this.branchState == other.branchState && Objects.equals(this.condition, other.condition);
    }

    public static enum BranchState {
        IN_ERROR,
        IF_BRANCH,
        ELSE_BRANCH;

    }
}

