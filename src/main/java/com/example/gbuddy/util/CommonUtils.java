package com.example.gbuddy.util;

import com.example.gbuddy.models.constants.Role;
import com.example.gbuddy.models.entities.Address;
import com.example.gbuddy.models.entities.Branch;
import com.example.gbuddy.models.entities.Gym;
import com.example.gbuddy.models.entities.User;
import com.example.gbuddy.models.protos.CommonsProto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.LinkedList;

public class CommonUtils {
    private static final Logger LOG = LoggerFactory.getLogger(CommonUtils.class);
    private static final String HOSTNAME_ERROR = "unknown";

    public static Branch buildBranch(String contact, double latitude, double longitude, Gym gym, Address address) {
        Branch b = new Branch();
        b.setContact(contact);
        b.setLatitude(latitude);
        b.setLongitude(longitude);
        b.setGymId(gym);
        b.setAddress(address);
        return b;
    }

    public static Gym buildGym(String name, String website) {
        Gym g = new Gym();
        Date d = new Date();
        g.setName(name);
        g.setWebsite(website);
        g.setHostname(getHostname());
        g.setCreatedDate(d);
        g.setLastUpdatedDate(d);
        g.setBranches(new LinkedList<>());
        return g;
    }

    public static Address buildAddressEntity(String city, String state, long pincode) {
        Address a = new Address();
        a.setCity(city);
        a.setState(state);
        a.setPincode(pincode);
        return a;
    }

    public static User buildUser(String name, String mobile, String password, String picUrl, Address address, Role role, String bio) {
        Date d = new Date();
        User user = new User();
        user.setName(name);
        user.setMobile(mobile);
        user.setPassword(password);
        user.setPicUrl(picUrl);
        user.setAddress(address);
        user.setRole(role);
        user.setBio(bio);
        user.setLastLoginDate(d);
        user.setDeletedDate(null);
        user.setCreatedDate(d);
        user.setLastUpdatedDate(d);
        user.setHostname(getHostname());
        return user;
    }

    public static CommonsProto.Address buildAddressProto(Branch branch) {
        return CommonsProto.Address.newBuilder()
                .setCity(branch.getAddress().getCity())
                .setState(branch.getAddress().getState())
                .setPincode(branch.getAddress().getPincode())
                .build();
    }

    public static CommonsProto.AuthResponse buildAuthResponse(User user) {
        return CommonsProto.AuthResponse.newBuilder()
                .setUserId(user.getId())
                .setName(user.getName())
                .setMobileNo(user.getMobile())
                .setPicUrl(user.getPicUrl())
                .setBio(user.getBio())
                .build();
    }

    private static String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            LOG.error("exception while fetching hostname", e);
            return HOSTNAME_ERROR;
        }
    }

}
