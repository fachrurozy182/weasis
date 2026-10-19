/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.util.StringUtil
 */
package org.weasis.dicom.param;

import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.Optional;
import org.dcm4che3.net.Association;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.util.StringUtil;

public class DicomNode {
    private static final Logger LOGGER = LoggerFactory.getLogger(DicomNode.class);
    private static final int MAX_AET_LENGTH = 16;
    private static final int MIN_PORT = 1;
    private static final int MAX_PORT = 65535;
    private static final String DEFAULT_IP = "127.0.0.1";
    private final String aet;
    private final String hostname;
    private final Integer port;
    private final boolean validateHostname;
    private final Long id;
    private final String description;

    public DicomNode(String aet) {
        this(aet, null, null);
    }

    public DicomNode(String aet, Integer port) {
        this(aet, null, port);
    }

    public DicomNode(String aet, String hostname, Integer port) {
        this(null, aet, hostname, port, false);
    }

    public DicomNode(String aet, String hostname, Integer port, String description) {
        this(null, aet, hostname, port, false, description);
    }

    public DicomNode(Long id, String aet, String hostname, Integer port) {
        this(id, aet, hostname, port, false);
    }

    public DicomNode(Long id, String aet, String hostname, Integer port, boolean validateHostname) {
        this(id, aet, hostname, port, validateHostname, null);
    }

    public DicomNode(Long id, String aet, String hostname, Integer port, boolean validateHostname, String description) {
        DicomNode.validateAet(aet);
        DicomNode.validatePort(port);
        this.id = id;
        this.aet = aet.trim();
        this.hostname = hostname;
        this.port = port;
        this.validateHostname = validateHostname;
        this.description = description;
    }

    private static void validateAet(String aet) {
        if (!StringUtil.hasText((CharSequence)aet)) {
            throw new IllegalArgumentException("Missing AETitle");
        }
        if (aet.length() > 16) {
            throw new IllegalArgumentException("AETitle has more than 16 characters");
        }
    }

    private static void validatePort(Integer port) {
        if (port != null && (port < 1 || port > 65535)) {
            throw new IllegalArgumentException("Port is out of bound");
        }
    }

    public Long getId() {
        return this.id;
    }

    public String getDescription() {
        return this.description;
    }

    public String getAet() {
        return this.aet;
    }

    public String getHostname() {
        return this.hostname;
    }

    public Integer getPort() {
        return this.port;
    }

    public boolean isValidateHostname() {
        return this.validateHostname;
    }

    public boolean equalsHostname(String anotherHostname) {
        if (Objects.equals(this.hostname, anotherHostname)) {
            return true;
        }
        return DicomNode.convertToIP(this.hostname).equals(DicomNode.convertToIP(anotherHostname));
    }

    public static String convertToIP(String hostname) {
        if (!StringUtil.hasText((CharSequence)hostname)) {
            return DEFAULT_IP;
        }
        try {
            return InetAddress.getByName(hostname).getHostAddress();
        }
        catch (UnknownHostException e) {
            LOGGER.debug("Cannot resolve hostname '{}': {}", (Object)hostname, (Object)e.getMessage());
            return hostname;
        }
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DicomNode)) {
            return false;
        }
        DicomNode dicomNode = (DicomNode)o;
        return Objects.equals(this.aet, dicomNode.aet) && Objects.equals(this.hostname, dicomNode.hostname) && Objects.equals(this.port, dicomNode.port);
    }

    public int hashCode() {
        return Objects.hash(this.aet, this.hostname, this.port);
    }

    public String toString() {
        return "DicomNode{aet='%s', hostname='%s', port=%d}".formatted(new Object[]{this.aet, this.hostname, this.port});
    }

    public static DicomNode buildLocalDicomNode(Association as) {
        Socket socket = as.getSocket();
        String ip = DicomNode.extractIpAddress(socket.getLocalAddress());
        return new DicomNode(as.getLocalAET(), ip, socket.getLocalPort());
    }

    public static DicomNode buildRemoteDicomNode(Association as) {
        Socket socket = as.getSocket();
        String ip = DicomNode.extractIpAddress(socket.getInetAddress());
        return new DicomNode(as.getRemoteAET(), ip, socket.getPort());
    }

    private static String extractIpAddress(InetAddress address) {
        return Optional.ofNullable(address).map(InetAddress::getHostAddress).orElse(null);
    }
}

