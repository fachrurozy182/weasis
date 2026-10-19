/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.net;

import java.io.IOException;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.AssociationStateException;
import org.dcm4che3.net.pdu.AAbort;
import org.dcm4che3.net.pdu.AAssociateAC;
import org.dcm4che3.net.pdu.AAssociateRJ;
import org.dcm4che3.net.pdu.AAssociateRQ;

public enum State {
    Sta1("Sta1 - Idle"){

        @Override
        void write(Association as, AAbort aa) {
        }

        @Override
        void closeSocket(Association as) {
        }

        @Override
        void closeSocketDelayed(Association as) {
        }
    }
    ,
    Sta2("Sta2 - Transport connection open"){

        @Override
        void onAAssociateRQ(Association as, AAssociateRQ rq) throws IOException {
            as.handle(rq);
        }

        @Override
        void write(Association as, AAbort aa) {
            as.doCloseSocket();
        }
    }
    ,
    Sta3("Sta3 - Awaiting local A-ASSOCIATE response primitive"),
    Sta4("Sta4 - Awaiting transport connection opening to complete"),
    Sta5("Sta5 - Awaiting A-ASSOCIATE-AC or A-ASSOCIATE-RJ PDU"){

        @Override
        void onAAssociateAC(Association as, AAssociateAC ac) throws IOException {
            as.handle(ac);
        }

        @Override
        void onAAssociateRJ(Association as, AAssociateRJ rj) throws IOException {
            as.handle(rj);
        }
    }
    ,
    Sta6("Sta6 - Association established and ready for data transfer"){

        @Override
        void onAReleaseRQ(Association as) throws IOException {
            as.handleAReleaseRQ();
        }

        @Override
        void onPDataTF(Association as) throws IOException {
            as.handlePDataTF();
        }

        @Override
        void writeAReleaseRQ(Association as) throws IOException {
            as.writeAReleaseRQ();
        }

        @Override
        public void writePDataTF(Association as) throws IOException {
            as.doWritePDataTF();
        }
    }
    ,
    Sta7("Sta7 - Awaiting A-RELEASE-RP PDU"){

        @Override
        public void onAReleaseRP(Association as) throws IOException {
            as.handleAReleaseRP();
        }

        @Override
        void onAReleaseRQ(Association as) throws IOException {
            as.handleAReleaseRQCollision();
        }

        @Override
        void onPDataTF(Association as) throws IOException {
            as.handlePDataTF();
        }
    }
    ,
    Sta8("Sta8 - Awaiting local A-RELEASE response primitive"){

        @Override
        public void writePDataTF(Association as) throws IOException {
            as.doWritePDataTF();
        }
    }
    ,
    Sta9("Sta9 - Release collision requestor side; awaiting A-RELEASE response"),
    Sta10("Sta10 - Release collision acceptor side; awaiting A-RELEASE-RP PDU"){

        @Override
        void onAReleaseRP(Association as) throws IOException {
            as.handleAReleaseRPCollision();
        }
    }
    ,
    Sta11("Sta11 - Release collision requestor side; awaiting A-RELEASE-RP PDU"){

        @Override
        void onAReleaseRP(Association as) throws IOException {
            as.handleAReleaseRP();
        }
    }
    ,
    Sta12("Sta12 - Release collision acceptor side; awaiting A-RELEASE response primitive"),
    Sta13("Sta13 - Awaiting Transport Connection Close Indication"){

        @Override
        public void onAReleaseRP(Association as) throws IOException {
        }

        @Override
        void onAReleaseRQ(Association as) throws IOException {
        }

        @Override
        void onPDataTF(Association as) throws IOException {
        }

        @Override
        void write(Association as, AAbort aa) {
        }

        @Override
        void closeSocketDelayed(Association as) {
        }
    };

    private String name;

    private State(String name) {
        this.name = name;
    }

    public String toString() {
        return this.name;
    }

    void onAAssociateRQ(Association as, AAssociateRQ rq) throws IOException {
        as.unexpectedPDU("A-ASSOCIATE-RQ");
    }

    void onAAssociateAC(Association as, AAssociateAC ac) throws IOException {
        as.unexpectedPDU("A-ASSOCIATE-AC");
    }

    void onAAssociateRJ(Association as, AAssociateRJ rj) throws IOException {
        as.unexpectedPDU("A-ASSOCIATE-RJ");
    }

    void onPDataTF(Association as) throws IOException {
        as.unexpectedPDU("P-DATA-TF");
    }

    void onAReleaseRQ(Association as) throws IOException {
        as.unexpectedPDU("A-RELEASE-RQ");
    }

    void onAReleaseRP(Association as) throws IOException {
        as.unexpectedPDU("A-RELEASE-RP");
    }

    void writeAReleaseRQ(Association as) throws IOException {
        throw new AssociationStateException(this);
    }

    void write(Association as, AAbort aa) {
        as.write(aa);
    }

    public void writePDataTF(Association as) throws IOException {
        throw new AssociationStateException(this);
    }

    void closeSocket(Association as) {
        as.doCloseSocket();
    }

    void closeSocketDelayed(Association as) {
        as.doCloseSocketDelayed();
    }
}

