package com.tgy.rtls.web.controller.view;

import java.util.HashMap;
import java.util.Map;

public class UserStatistics {

    public static void main(String[] args) {
        int[] userIds = {1, 2, 3, 4, 5, 1, 6, 7, 2, 8};
        long[] loginTimes = {1000, 2000, 1500, 3000, 2500, 1200, 3500, 4000, 2200, 5000};

        Map<Integer, Long> earliestLoginMap = new HashMap<>();

        for (int i = 0; i < userIds.length; i++) {
            int userId = userIds[i];
            long loginTime = loginTimes[i];

            if (!earliestLoginMap.containsKey(userId)) {
                earliestLoginMap.put(userId, loginTime);
            } else {
                long earliestLoginTime = earliestLoginMap.get(userId);
                if (loginTime < earliestLoginTime) {
                    earliestLoginMap.put(userId, loginTime);
                }
            }
        }

        int newUsersCount = earliestLoginMap.size();
        System.out.println("新用户数为：" + newUsersCount);
    }
}
