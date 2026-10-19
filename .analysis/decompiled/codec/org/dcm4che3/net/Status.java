/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.net;

public class Status {
    public static final int Success = 0;
    public static final int Pending = 65280;
    public static final int PendingWarning = 65281;
    public static final int Cancel = 65024;
    public static final int NoSuchAttribute = 261;
    public static final int InvalidAttributeValue = 262;
    public static final int AttributeListError = 263;
    public static final int ProcessingFailure = 272;
    public static final int DuplicateSOPinstance = 273;
    public static final int NoSuchObjectInstance = 274;
    public static final int NoSuchEventType = 275;
    public static final int NoSuchArgument = 276;
    public static final int InvalidArgumentValue = 277;
    public static final int AttributeValueOutOfRange = 278;
    public static final int InvalidObjectInstance = 279;
    public static final int NoSuchSOPclass = 280;
    public static final int ClassInstanceConflict = 281;
    public static final int MissingAttribute = 288;
    public static final int MissingAttributeValue = 289;
    public static final int SOPclassNotSupported = 290;
    public static final int NoSuchActionType = 291;
    public static final int NotAuthorized = 292;
    public static final int DuplicateInvocation = 528;
    public static final int UnrecognizedOperation = 529;
    public static final int MistypedArgument = 530;
    public static final int ResourceLimitation = 531;
    public static final int OutOfResources = 42752;
    public static final int UnableToCalculateNumberOfMatches = 42753;
    public static final int UnableToPerformSubOperations = 42754;
    public static final int MoveDestinationUnknown = 43009;
    public static final int IdentifierDoesNotMatchSOPClass = 43264;
    public static final int DataSetDoesNotMatchSOPClassError = 43264;
    public static final int OneOrMoreFailures = 45056;
    public static final int CoercionOfDataElements = 45056;
    public static final int ElementsDiscarded = 45062;
    public static final int DataSetDoesNotMatchSOPClassWarning = 45063;
    public static final int UnableToProcess = 49152;
    public static final int CannotUnderstand = 49152;
    public static final int UPSCreatedWithModifications = 45824;
    public static final int UPSDeletionLockNotGranted = 45825;
    public static final int UPSAlreadyInRequestedStateOfCanceled = 45828;
    public static final int UPSCoercedInvalidValuesToValidValues = 45829;
    public static final int UPSAlreadyInRequestedStateOfCompleted = 45830;
    public static final int UPSMayNoLongerBeUpdated = 49920;
    public static final int UPSTransactionUIDNotCorrect = 49921;
    public static final int UPSAlreadyInProgress = 49922;
    public static final int UPSStateMayNotChangedToScheduled = 49923;
    public static final int UPSNotMetFinalStateRequirements = 49924;
    public static final int UPSDoesNotExist = 49927;
    public static final int UPSUnknownReceivingAET = 49928;
    public static final int UPSNotScheduled = 49929;
    public static final int UPSNotYetInProgress = 49936;
    public static final int UPSAlreadyCompleted = 49937;
    public static final int UPSPerformerCannotBeContacted = 49938;
    public static final int UPSPerformerChoosesNotToCancel = 49939;
    public static final int UPSActionNotAppropriate = 49940;
    public static final int UPSDoesNotSupportEventReports = 49941;
    public static final int ReferencedTransferSyntaxNotSupported = 49426;

    public static boolean isPending(int status) {
        return (status & 0xFF00) == 65280;
    }
}

