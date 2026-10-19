/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.net.service;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.IOD;
import org.dcm4che3.data.VR;
import org.dcm4che3.data.ValidationResult;
import org.dcm4che3.net.service.DicomServiceException;

public enum QueryRetrieveLevel {
    PATIENT{

        @Override
        protected IOD queryKeysIOD(QueryRetrieveLevel rootLevel, boolean relational) {
            IOD iod = new IOD();
            iod.add(new IOD.DataElement(0x20000D, VR.UI, IOD.DataElementType.TYPE_0, -1, -1, 0));
            iod.add(new IOD.DataElement(0x20000E, VR.UI, IOD.DataElementType.TYPE_0, -1, -1, 0));
            iod.add(new IOD.DataElement(524312, VR.UI, IOD.DataElementType.TYPE_0, -1, -1, 0));
            return iod;
        }

        @Override
        protected IOD retrieveKeysIOD(QueryRetrieveLevel rootLevel, boolean relational) {
            IOD iod = this.queryKeysIOD(rootLevel, relational);
            iod.add(new IOD.DataElement(0x100020, VR.LO, IOD.DataElementType.TYPE_1, 1, 1, 0));
            return iod;
        }
    }
    ,
    STUDY{

        @Override
        protected IOD queryKeysIOD(QueryRetrieveLevel rootLevel, boolean relational) {
            IOD iod = new IOD();
            iod.add(new IOD.DataElement(0x100020, VR.LO, !relational && rootLevel == PATIENT ? IOD.DataElementType.TYPE_1 : IOD.DataElementType.TYPE_3, 1, 1, 0));
            iod.add(new IOD.DataElement(0x20000E, VR.UI, IOD.DataElementType.TYPE_0, -1, -1, 0));
            iod.add(new IOD.DataElement(524312, VR.UI, IOD.DataElementType.TYPE_0, -1, -1, 0));
            return iod;
        }

        @Override
        protected IOD retrieveKeysIOD(QueryRetrieveLevel rootLevel, boolean relational) {
            IOD iod = this.queryKeysIOD(rootLevel, relational);
            iod.add(new IOD.DataElement(0x20000D, VR.UI, IOD.DataElementType.TYPE_1, -1, -1, 0));
            return iod;
        }
    }
    ,
    SERIES{

        @Override
        protected IOD queryKeysIOD(QueryRetrieveLevel rootLevel, boolean relational) {
            IOD iod = new IOD();
            iod.add(new IOD.DataElement(0x100020, VR.LO, !relational && rootLevel == PATIENT ? IOD.DataElementType.TYPE_1 : IOD.DataElementType.TYPE_3, 1, 1, 0));
            iod.add(new IOD.DataElement(0x20000D, VR.UI, !relational ? IOD.DataElementType.TYPE_1 : IOD.DataElementType.TYPE_3, 1, 1, 0));
            iod.add(new IOD.DataElement(524312, VR.UI, IOD.DataElementType.TYPE_0, -1, -1, 0));
            return iod;
        }

        @Override
        protected IOD retrieveKeysIOD(QueryRetrieveLevel rootLevel, boolean relational) {
            IOD iod = this.queryKeysIOD(rootLevel, relational);
            iod.add(new IOD.DataElement(0x20000E, VR.UI, IOD.DataElementType.TYPE_1, -1, -1, 0));
            return iod;
        }
    }
    ,
    IMAGE{

        @Override
        protected IOD queryKeysIOD(QueryRetrieveLevel rootLevel, boolean relational) {
            IOD iod = new IOD();
            iod.add(new IOD.DataElement(0x100020, VR.LO, !relational && rootLevel == PATIENT ? IOD.DataElementType.TYPE_1 : IOD.DataElementType.TYPE_3, 1, 1, 0));
            iod.add(new IOD.DataElement(0x20000D, VR.UI, !relational ? IOD.DataElementType.TYPE_1 : IOD.DataElementType.TYPE_3, 1, 1, 0));
            iod.add(new IOD.DataElement(0x20000E, VR.UI, !relational ? IOD.DataElementType.TYPE_1 : IOD.DataElementType.TYPE_3, 1, 1, 0));
            return iod;
        }

        @Override
        protected IOD retrieveKeysIOD(QueryRetrieveLevel rootLevel, boolean relational) {
            IOD iod = this.queryKeysIOD(rootLevel, relational);
            iod.add(new IOD.DataElement(524312, VR.UI, IOD.DataElementType.TYPE_1, -1, -1, 0));
            return iod;
        }
    }
    ,
    FRAME{

        @Override
        protected IOD queryKeysIOD(QueryRetrieveLevel rootLevel, boolean relational) {
            throw new UnsupportedOperationException();
        }

        @Override
        protected IOD retrieveKeysIOD(QueryRetrieveLevel rootLevel, boolean relational) {
            return IMAGE.retrieveKeysIOD(rootLevel, relational);
        }
    };


    public static QueryRetrieveLevel valueOf(Attributes attrs, String[] qrLevels) throws DicomServiceException {
        ValidationResult result = new ValidationResult();
        attrs.validate(new IOD.DataElement(524370, VR.LO, IOD.DataElementType.TYPE_1, 1, 1, 0).setValues(qrLevels), result);
        QueryRetrieveLevel.check(result);
        return QueryRetrieveLevel.valueOf(attrs.getString(524370));
    }

    public void validateQueryKeys(Attributes attrs, QueryRetrieveLevel rootLevel, boolean relational) throws DicomServiceException {
        QueryRetrieveLevel.check(attrs.validate(this.queryKeysIOD(rootLevel, relational)));
    }

    public void validateRetrieveKeys(Attributes attrs, QueryRetrieveLevel rootLevel, boolean relational) throws DicomServiceException {
        QueryRetrieveLevel.check(attrs.validate(this.retrieveKeysIOD(rootLevel, relational)));
    }

    protected abstract IOD queryKeysIOD(QueryRetrieveLevel var1, boolean var2);

    protected abstract IOD retrieveKeysIOD(QueryRetrieveLevel var1, boolean var2);

    private static void check(ValidationResult result) throws DicomServiceException {
        if (!result.isValid()) {
            throw new DicomServiceException(43264, result.getErrorComment()).setOffendingElements(result.getOffendingElements());
        }
    }
}

