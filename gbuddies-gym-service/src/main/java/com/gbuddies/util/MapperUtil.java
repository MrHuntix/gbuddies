package com.gbuddies.util;

import com.gbuddies.dao.BranchDao;
import com.gbuddies.models.Branch;
import com.gbuddies.models.Gym;
import com.gbuddies.protos.GymProto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MapperUtil {
    private static final Logger logger = LoggerFactory.getLogger(MapperUtil.class);

    @Autowired
    private BranchDao branchDao;

    public Gym getGymFromRequest(GymProto.Gym request) {
        logger.info("building gym entity from proto for gym {}", request.getName());
        Gym gym = new Gym();
        gym.setName(request.getName());
        gym.setWebsite(request.getWebsite());
        for (GymProto.Branch branch : request.getBranchesList()) {
            Branch b = new Branch();
            b.setGymId(gym);
            b.setLocality(branch.getLocality());
            b.setCity(branch.getCity());
            b.setLatitude(branch.getLatitude());
            b.setLongitude(branch.getLongitude());
            b.setContact(branch.getContact());
            gym.getBranches().add(b);
        }
        logger.info("built gym entity {}", gym);
        return gym;
    }


    public GymProto.FetchResponse getResponseFromEntity(List<Gym> gyms, GymProto.FetchResponse.Builder builder) {
        logger.info("building fetch response for gyms");
        gyms.forEach(gym -> builder.addGym(getGymProtoFromGymEntity(gym)));
        logger.info("built fetch response");
        return builder.build();
    }

    private GymProto.Gym getGymProtoFromGymEntity(Gym gym) {
        logger.info("building gym proto from entity for gym {}", gym.getName());
        GymProto.Gym.Builder builder = GymProto.Gym.newBuilder();
        int gymId = gym.getId();
        builder.setId(gym.getId())
                .setName(gym.getName())
                .setWebsite(gym.getWebsite());
        gym.getBranches().forEach(branch -> {
            GymProto.Branch.Builder branchBuilder = GymProto.Branch.newBuilder();
            branchBuilder.setId(branch.getId())
                    .setGymId(gymId)
                    .setLocality(branch.getLocality())
                    .setCity(branch.getCity())
                    .setLatitude(branch.getLatitude())
                    .setLongitude(branch.getLongitude())
                    .setContact(branch.getContact());
            builder.addBranches(branchBuilder.build());
        });
        return builder.build();
    }
}
