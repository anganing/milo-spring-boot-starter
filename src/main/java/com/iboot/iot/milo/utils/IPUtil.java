package com.iboot.iot.milo.utils;

import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;

import java.net.*;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

@Slf4j
public class IPUtil {
    private IPUtil() {
    }

    public static String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException var1) {
            return "localhost";
        }
    }

    public static Set<String> getHostnames(String address) {
        return getHostnames(address, true);
    }

    public static Set<String> getHostnames(String address, boolean includeLoopback) {
        HashSet<String> hostnames = Sets.newHashSet();

        try {
            InetAddress inetAddress = InetAddress.getByName(address);
            if (inetAddress.isAnyLocalAddress()) {
                try {
                    Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();

                    for (NetworkInterface ni : Collections.list(nis)) {
                        Collections.list(ni.getInetAddresses()).forEach((ia) -> {
                            if (ia instanceof Inet4Address) {
                                boolean loopback = ia.isLoopbackAddress();
                                if (!loopback || includeLoopback) {
                                    hostnames.add(ia.getHostName());
                                    hostnames.add(ia.getHostAddress());
                                    hostnames.add(ia.getCanonicalHostName());
                                }
                            }

                        });
                    }
                } catch (SocketException e) {
                    log.warn("Failed to NetworkInterfaces for bind address: {}", address, e);
                }
            } else {
                boolean loopback = inetAddress.isLoopbackAddress();
                if (!loopback || includeLoopback) {
                    hostnames.add(inetAddress.getHostName());
                    hostnames.add(inetAddress.getHostAddress());
                    hostnames.add(inetAddress.getCanonicalHostName());
                }
            }
        } catch (UnknownHostException e) {
            log.warn("Failed to get InetAddress for bind address: {}", address, e);
        }

        return hostnames;
    }
}
